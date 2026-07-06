package com.mssj.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mssj.mapper.JobMapper;
import com.mssj.mapper.JobRelationMapper;
import com.mssj.pojo.*;
import com.mssj.service.AnalysisService;
import com.mssj.service.ScoringEngine;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI 职业规划分析服务实现 (v2.0)
 *
 * 重大重构:
 * - 从单一大 Prompt 拆分为 4 个分阶段 Prompt（结构化输出）
 * - LLM 返回严格的 JSON，通过 Jackson 反序列化为 Java DTO
 * - 引入重试机制（最多 3 次，指数退避）
 * - 后端 ScoringEngine 替代 LLM 臆测评分
 * - 结构化岗位数据注入（JSON 序列化而非 toString()）
 */
@Slf4j
@Service
public class AnalysisServiceImpl implements AnalysisService {

    private final DeepSeekChatModel deepSeekChatModel;
    private final JobRelationMapper jobRelationMapper;
    private final JobMapper jobMapper;
    private final ScoringEngine scoringEngine;
    private final ObjectMapper objectMapper;

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_BACKOFF_MS = 1000;

    public AnalysisServiceImpl(DeepSeekChatModel deepSeekChatModel,
                               JobRelationMapper jobRelationMapper,
                               JobMapper jobMapper,
                               ScoringEngine scoringEngine) {
        this.deepSeekChatModel = deepSeekChatModel;
        this.jobRelationMapper = jobRelationMapper;
        this.jobMapper = jobMapper;
        this.scoringEngine = scoringEngine;
        this.objectMapper = new ObjectMapper();
    }

    // ═══════════════════════════════════════════════════════════════
    // 同步全流程分析（兼容旧接口）
    // ═══════════════════════════════════════════════════════════════

    @Override
    public Result processFileWithModel(File file) throws IOException {
        if (file.isDirectory()) {
            return processDirectory(file);
        } else {
            return processSingleFile(file);
        }
    }

    private Result processSingleFile(File file) throws IOException {
        String content = extractTextFromFile(file);
        return fullAnalysis(content);
    }

    private Result processDirectory(File directory) {
        if (!directory.isDirectory()) {
            return Result.fail("提供的路径不是目录");
        }
        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            return Result.fail("目录下没有文件");
        }

        StringBuilder combinedContent = new StringBuilder();
        for (File file : files) {
            if (file.isFile()) {
                try {
                    String text = extractTextFromFile(file);
                    combinedContent.append("--- 文件: ").append(file.getName()).append(" ---\n");
                    combinedContent.append(text).append("\n\n");
                } catch (Exception e) {
                    log.warn("解析文件失败: {} - {}", file.getName(), e.getMessage());
                }
            }
        }

        if (combinedContent.isEmpty()) {
            return Result.fail("没有成功提取到任何文件内容");
        }

        return fullAnalysis(combinedContent.toString());
    }

    /**
     * 聚合分析：依次执行 4 个阶段
     */
    private Result fullAnalysis(String content) {
        try {
            CareerReport.StudentProfile profile = extractProfile(content);
            CareerReport.JobMatchAnalysis matchAnalysis = matchJobs(profile);
            CareerReport.CareerPathPlan pathPlan = planCareerPath(profile, matchAnalysis);
            CareerReport.ActionPlan actionPlan = generateActionPlan(profile, matchAnalysis, pathPlan);

            CareerReport report = new CareerReport();
            report.setStudentProfile(profile);
            report.setJobMatchAnalysis(matchAnalysis);
            report.setCareerPathPlan(pathPlan);
            report.setActionPlan(actionPlan);

            return Result.success(report);
        } catch (Exception e) {
            log.error("AI 分析失败", e);
            return Result.fail(500, "AI 分析失败: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Phase 1: 结构化简历能力提取
    // ═══════════════════════════════════════════════════════════════

    @Override
    public CareerReport.StudentProfile extractProfile(String resumeContent) {
        String prompt = buildProfileExtractionPrompt(resumeContent);
        String rawJson = callLLMWithRetry(prompt, "简历能力提取");

        CareerReport.StudentProfile profile = parseJson(rawJson,
                CareerReport.StudentProfile.class, "StudentProfile");

        if (profile == null) {
            profile = createEmptyProfile();
        }

        // 后端覆盖评分（不信任 LLM 的打分）
        double completeness = scoringEngine.computeCompleteness(profile);
        double competitiveness = scoringEngine.computeCompetitiveness(profile);
        profile.setCompletenessScore(completeness);
        profile.setCompetitivenessScore(competitiveness);

        log.info("Phase 1 完成: 完整度={}, 竞争力={}", completeness, competitiveness);
        return profile;
    }

    private CareerReport.StudentProfile createEmptyProfile() {
        CareerReport.StudentProfile p = new CareerReport.StudentProfile();
        p.setProfessionalSkills(Collections.emptyList());
        p.setCertificates(Collections.emptyList());
        p.setInnovationAbilities(Collections.emptyList());
        p.setLearningAbilities(Collections.emptyList());
        p.setStressResistance(Collections.emptyList());
        p.setCommunicationSkills(Collections.emptyList());
        p.setInternshipAbilities(Collections.emptyList());
        p.setCompletenessScore(0.0);
        p.setCompetitivenessScore(0.0);
        p.setOverallAssessment("无法从简历中提取有效信息，建议补充更详细的个人履历。");
        return p;
    }

    private String buildProfileExtractionPrompt(String resumeContent) {
        return new StringBuilder()
                .append("你是一位资深职业规划顾问。请从以下学生简历文本中，提取结构化的能力信息。\n\n")
                .append("### 学生简历：\n").append(resumeContent).append("\n\n")
                .append("请严格按照以下 JSON 结构输出，字段名必须完全一致，仅输出 JSON 对象，不要包含 Markdown 标记或额外文本。\n\n")
                .append("{\n")
                .append("  \"professionalSkills\": [\"专业技能1\", \"专业技能2\"],\n")
                .append("  \"certificates\": [\"证书1\", \"证书2\"],\n")
                .append("  \"innovationAbilities\": [\"创新能力体现1\"],\n")
                .append("  \"learningAbilities\": [\"学习能力体现1\"],\n")
                .append("  \"stressResistance\": [\"抗压能力体现1\"],\n")
                .append("  \"communicationSkills\": [\"沟通能力体现1\"],\n")
                .append("  \"internshipAbilities\": [\"实习经历1\"],\n")
                .append("  \"overallAssessment\": \"综合评价（100-150字）\"\n")
                .append("}\n\n")
                .append("要求：\n")
                .append("- professionalSkills 至少 3 项，至多 8 项，必须来源于简历中的实际技能\n")
                .append("- 每个数组字段若非空则至少包含 1 个具体条目\n")
                .append("- 若简历中确实没有某维度信息，该数组返回空 []\n")
                .append("- overallAssessment 需包含优势总结和主要改进方向\n")
                .append("- 输出纯 JSON，不要包含 ```json 标记\n")
                .toString();
    }

    // ═══════════════════════════════════════════════════════════════
    // Phase 2: 四维人岗匹配评分
    // ═══════════════════════════════════════════════════════════════

    @Override
    public CareerReport.JobMatchAnalysis matchJobs(CareerReport.StudentProfile profile) {
        List<JobProfile> allJobs = getAllJobProfiles();
        String jobsJson = buildStructuredJobsJson(allJobs);

        String prompt = buildJobMatchPrompt(profile, jobsJson);
        String rawJson = callLLMWithRetry(prompt, "岗位匹配评分");

        CareerReport.JobMatchAnalysis analysis = parseJson(rawJson,
                CareerReport.JobMatchAnalysis.class, "JobMatchAnalysis");

        if (analysis == null || analysis.getMatchedJobs() == null) {
            analysis = new CareerReport.JobMatchAnalysis();
            analysis.setMatchedJobs(Collections.emptyList());
            analysis.setOverallMatchScore(0.0);
            analysis.setGapAnalysis("匹配分析失败，请重试。");
            return analysis;
        }

        // 后端重新计算每个匹配岗位的加权总分
        Map<String, JobProfile> jobProfileMap = allJobs.stream()
                .collect(Collectors.toMap(JobProfile::getJobName, j -> j, (a, b) -> a));

        double sumWeighted = 0;
        int count = 0;

        for (CareerReport.JobMatchDetail detail : analysis.getMatchedJobs()) {
            JobProfile matchedJob = jobProfileMap.get(detail.getJobName());
            if (matchedJob != null && detail.getDimensions() != null) {
                double weightedTotal = scoringEngine.computeWeightedTotal(
                        detail.getDimensions(), matchedJob);
                detail.setWeightedTotalScore(weightedTotal);
                detail.setMatchScore(weightedTotal); // 最终匹配分 = 加权总分
                sumWeighted += weightedTotal;
                count++;
            } else {
                // 降级: 用原始 matchScore
                if (detail.getMatchScore() == null) {
                    detail.setMatchScore(50.0);
                }
                detail.setWeightedTotalScore(detail.getMatchScore());
                sumWeighted += detail.getMatchScore();
                count++;
                log.warn("岗位 [{}] 未在数据库中找到匹配画像，使用 LLM 原始评分", detail.getJobName());
            }
        }

        // 更新总体匹配分
        double overall = count > 0 ? sumWeighted / count : 50.0;
        analysis.setOverallMatchScore(Math.round(overall * 10.0) / 10.0);

        // 验证 4 维得分范围
        validateDimensions(analysis);

        log.info("Phase 2 完成: 匹配 {} 个岗位, 总体匹配度={}", analysis.getMatchedJobs().size(), analysis.getOverallMatchScore());
        return analysis;
    }

    /**
     * 将岗位列表序列化为结构化 JSON（替代 toString()）
     */
    private String buildStructuredJobsJson(List<JobProfile> jobs) {
        try {
            List<Map<String, Object>> simplified = new ArrayList<>();
            for (JobProfile j : jobs) {
                Map<String, Object> jobMap = new LinkedHashMap<>();
                jobMap.put("jobName", j.getJobName());
                jobMap.put("skills", j.getSkills());
                jobMap.put("score", j.getScore());
                simplified.add(jobMap);
            }
            return objectMapper.writeValueAsString(simplified);
        } catch (JsonProcessingException e) {
            log.warn("岗位 JSON 序列化失败，降级为 toString()", e);
            return jobs.toString();
        }
    }

    private String buildJobMatchPrompt(CareerReport.StudentProfile profile, String jobsJson) {
        String profileJson;
        try {
            profileJson = objectMapper.writeValueAsString(profile);
        } catch (JsonProcessingException e) {
            profileJson = profile.toString();
        }

        return new StringBuilder()
                .append("你是一位资深职业规划顾问。请基于学生的能力画像和岗位数据，进行人岗匹配分析。\n\n")
                .append("### 学生能力画像（JSON）：\n").append(profileJson).append("\n\n")
                .append("### 岗位画像数据（JSON 数组）：\n").append(jobsJson).append("\n\n")
                .append("### 任务：\n")
                .append("从岗位列表中选出 3-5 个最适合该学生的岗位，对每个岗位从以下四个维度给出评分（均为 0-100 分）：\n\n")
                .append("1. **基础要求 (baseScore)**: 学历、专业背景等与岗位基本门槛的匹配度\n")
                .append("2. **职业技能 (skillScore)**: 专业技能、技术栈与岗位技能要求的匹配度\n")
                .append("3. **职业素养 (qualityScore)**: 沟通、抗压、团队协作等软素质匹配度\n")
                .append("4. **发展潜力 (potentialScore)**: 学习能力、创新能力、成长空间匹配度\n\n")
                .append("请严格按照以下 JSON 结构输出，仅输出 JSON，不要包含 Markdown 标记：\n\n")
                .append("{\n")
                .append("  \"matchedJobs\": [\n")
                .append("    {\n")
                .append("      \"jobName\": \"岗位名称\",\n")
                .append("      \"matchScore\": 85.0,\n")
                .append("      \"dimensions\": {\n")
                .append("        \"baseScore\": 80,\n")
                .append("        \"skillScore\": 75,\n")
                .append("        \"qualityScore\": 85,\n")
                .append("        \"potentialScore\": 90\n")
                .append("      },\n")
                .append("      \"strengths\": [\"优势1\", \"优势2\"],\n")
                .append("      \"gaps\": [\"差距1\", \"差距2\"]\n")
                .append("    }\n")
                .append("  ],\n")
                .append("  \"gapAnalysis\": \"综合差距分析（100-150字）\"\n")
                .append("}\n\n")
                .append("要求：\n")
                .append("- 每个维度的分数必须反映学生的真实匹配情况，不可随意给分\n")
                .append("- matchedJobs 必须是岗位列表中存在的 jobName，不可臆造\n")
                .append("- 输出纯 JSON，不要包含 ```json 标记\n")
                .toString();
    }

    // ═══════════════════════════════════════════════════════════════
    // Phase 3: 职业路径规划
    // ═══════════════════════════════════════════════════════════════

    @Override
    public CareerReport.CareerPathPlan planCareerPath(CareerReport.StudentProfile profile,
                                                       CareerReport.JobMatchAnalysis matchAnalysis) {
        List<JobRelation> relations = getAllJobRelations();

        String profileJson;
        String matchJson;
        String relationsJson;
        try {
            profileJson = objectMapper.writeValueAsString(profile);
            matchJson = objectMapper.writeValueAsString(matchAnalysis);
            relationsJson = objectMapper.writeValueAsString(relations);
        } catch (JsonProcessingException e) {
            profileJson = profile.toString();
            matchJson = matchAnalysis.toString();
            relationsJson = relations.toString();
        }

        String prompt = new StringBuilder()
                .append("你是一位资深职业规划顾问。请基于学生画像和岗位匹配结果，制定职业发展路径。\n\n")
                .append("### 学生画像：\n").append(profileJson).append("\n\n")
                .append("### 岗位匹配结果：\n").append(matchJson).append("\n\n")
                .append("### 岗位关系图谱：\n").append(relationsJson).append("\n\n")
                .append("请严格按照以下 JSON 结构输出，仅输出 JSON：\n\n")
                .append("{\n")
                .append("  \"careerGoal\": \"明确的职业目标（如：3年内成为Java高级开发工程师）\",\n")
                .append("  \"industryTrend\": \"行业趋势分析（100-150字）\",\n")
                .append("  \"careerPath\": \"发展路径（如：初级开发 → 高级开发 → 技术专家）\",\n")
                .append("  \"keyMilestones\": [\"里程碑1\", \"里程碑2\", \"里程碑3\"],\n")
                .append("  \"requiredSkills\": [\"需要培养的技能1\", \"需要培养的技能2\"]\n")
                .append("}\n")
                .toString();

        String rawJson = callLLMWithRetry(prompt, "职业路径规划");

        CareerReport.CareerPathPlan plan = parseJson(rawJson,
                CareerReport.CareerPathPlan.class, "CareerPathPlan");

        if (plan == null) {
            plan = new CareerReport.CareerPathPlan();
            plan.setCareerGoal("请补充更多信息以生成职业目标");
            plan.setIndustryTrend("请上传更详细的简历");
            plan.setCareerPath("待分析");
            plan.setKeyMilestones(Collections.emptyList());
            plan.setRequiredSkills(Collections.emptyList());
        }

        log.info("Phase 3 完成: 职业目标={}", plan.getCareerGoal());
        return plan;
    }

    // ═══════════════════════════════════════════════════════════════
    // Phase 4: 行动计划生成
    // ═══════════════════════════════════════════════════════════════

    @Override
    public CareerReport.ActionPlan generateActionPlan(CareerReport.StudentProfile profile,
                                                       CareerReport.JobMatchAnalysis matchAnalysis,
                                                       CareerReport.CareerPathPlan pathPlan) {
        String profileJson;
        String matchJson;
        String pathJson;
        try {
            profileJson = objectMapper.writeValueAsString(profile);
            matchJson = objectMapper.writeValueAsString(matchAnalysis);
            pathJson = objectMapper.writeValueAsString(pathPlan);
        } catch (JsonProcessingException e) {
            profileJson = profile.toString();
            matchJson = matchAnalysis.toString();
            pathJson = pathPlan.toString();
        }

        String prompt = new StringBuilder()
                .append("你是一位资深职业规划顾问。请基于学生画像、匹配结果和职业路径，制定具体的行动计划。\n\n")
                .append("### 学生画像：\n").append(profileJson).append("\n\n")
                .append("### 岗位匹配：\n").append(matchJson).append("\n\n")
                .append("### 职业路径：\n").append(pathJson).append("\n\n")
                .append("请严格按照以下 JSON 结构输出，仅输出 JSON：\n\n")
                .append("{\n")
                .append("  \"stages\": [\n")
                .append("    {\n")
                .append("      \"stageName\": \"短期（1-2年）\",\n")
                .append("      \"description\": \"阶段描述（50-80字）\",\n")
                .append("      \"actions\": [\"行动1\", \"行动2\", \"行动3\"]\n")
                .append("    },\n")
                .append("    {\n")
                .append("      \"stageName\": \"中期（3-5年）\",\n")
                .append("      \"description\": \"阶段描述（50-80字）\",\n")
                .append("      \"actions\": [\"行动1\", \"行动2\", \"行动3\"]\n")
                .append("    }\n")
                .append("  ],\n")
                .append("  \"evaluationMetrics\": \"评估指标（如：技能掌握度、项目经验数、认证通过率）\",\n")
                .append("  \"evaluationCycle\": \"评估周期（如：每半年一次全面复盘）\",\n")
                .append("  \"recommendedActions\": [\"额外建议1\", \"额外建议2\"]\n")
                .append("}\n")
                .toString();

        String rawJson = callLLMWithRetry(prompt, "行动计划生成");

        CareerReport.ActionPlan plan = parseJson(rawJson,
                CareerReport.ActionPlan.class, "ActionPlan");

        if (plan == null) {
            plan = new CareerReport.ActionPlan();
            plan.setStages(Collections.emptyList());
            plan.setEvaluationMetrics("请补充信息后重新生成");
            plan.setEvaluationCycle("每半年一次");
            plan.setRecommendedActions(Collections.emptyList());
        }

        log.info("Phase 4 完成: {} 个阶段计划", plan.getStages() != null ? plan.getStages().size() : 0);
        return plan;
    }

    // ═══════════════════════════════════════════════════════════════
    // LLM 调用与重试
    // ═══════════════════════════════════════════════════════════════

    /**
     * 调用 DeepSeek LLM 并内置重试机制
     */
    private String callLLMWithRetry(String prompt, String phaseName) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                log.info("[{}] 第 {} 次 LLM 调用...", phaseName, attempt);
                String result = deepSeekChatModel.call(prompt);

                if (result == null || result.trim().isEmpty()) {
                    throw new IOException("LLM 返回空响应");
                }

                return result;
            } catch (Exception e) {
                lastException = e;
                log.warn("[{}] 第 {} 次调用失败: {}", phaseName, attempt, e.getMessage());

                if (attempt < MAX_RETRIES) {
                    long backoff = INITIAL_BACKOFF_MS * (1L << (attempt - 1)); // 1s, 2s, 4s
                    try {
                        Thread.sleep(backoff);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        throw new RuntimeException(
                String.format("[%s] LLM 调用在 %d 次重试后仍失败: %s",
                        phaseName, MAX_RETRIES,
                        lastException != null ? lastException.getMessage() : "未知错误"),
                lastException);
    }

    // ═══════════════════════════════════════════════════════════════
    // JSON 解析
    // ═══════════════════════════════════════════════════════════════

    private <T> T parseJson(String rawJson, Class<T> clazz, String label) {
        try {
            String cleaned = cleanJson(rawJson);
            T result = objectMapper.readValue(cleaned, clazz);
            log.debug("[{}] JSON 解析成功", label);
            return result;
        } catch (IOException e) {
            log.error("[{}] JSON 解析失败: {} — 原始内容前 300 字符: {}",
                    label, e.getMessage(),
                    rawJson != null ? rawJson.substring(0, Math.min(300, rawJson.length())) : "null");
            return null;
        }
    }

    private String cleanJson(String input) {
        if (input == null) return "{}";
        String cleaned = input.trim();
        // 去除 Markdown 代码块标记
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        // 提取第一个 { 到最后一个 }
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            cleaned = cleaned.substring(start, end + 1);
        }
        return cleaned.trim();
    }

    // ═══════════════════════════════════════════════════════════════
    // 验证
    // ═══════════════════════════════════════════════════════════════

    private void validateDimensions(CareerReport.JobMatchAnalysis analysis) {
        if (analysis.getMatchedJobs() == null) return;
        for (CareerReport.JobMatchDetail detail : analysis.getMatchedJobs()) {
            if (detail.getDimensions() != null) {
                MatchDimension dim = detail.getDimensions();
                dim.setBaseScore(clampScore(dim.getBaseScore()));
                dim.setSkillScore(clampScore(dim.getSkillScore()));
                dim.setQualityScore(clampScore(dim.getQualityScore()));
                dim.setPotentialScore(clampScore(dim.getPotentialScore()));
            }
        }
    }

    private Double clampScore(Double score) {
        if (score == null) return 0.0;
        return Math.max(0.0, Math.min(100.0, score));
    }

    // ═══════════════════════════════════════════════════════════════
    // 文件提取
    // ═══════════════════════════════════════════════════════════════

    private String extractTextFromFile(File file) throws IOException {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".txt")) {
            return new String(Files.readAllBytes(file.toPath()));
        } else if (fileName.endsWith(".pdf")) {
            return extractTextFromPdf(file);
        } else {
            throw new IOException("不支持的文件类型: " + fileName);
        }
    }

    private String extractTextFromPdf(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 数据访问
    // ═══════════════════════════════════════════════════════════════

    @Override
    public List<JobProfile> getAllJobProfiles() {
        return jobMapper.getAllJobProfiles();
    }

    @Override
    public List<JobRelation> getAllJobRelations() {
        return jobRelationMapper.getAllJobRelations();
    }
}
