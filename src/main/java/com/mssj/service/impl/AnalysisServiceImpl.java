package com.mssj.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mssj.mapper.JobMapper;
import com.mssj.mapper.JobRelationMapper;
import com.mssj.pojo.CareerReport;
import com.mssj.pojo.JobProfile;
import com.mssj.pojo.JobRelation;
import com.mssj.pojo.Result;
import com.mssj.service.AnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;


@Slf4j
@Service
public class AnalysisServiceImpl implements AnalysisService {

    private final DeepSeekChatModel deepSeekChatModel; // DeepSeek 模型
    private final JobRelationMapper jobRelationMapper; // 岗位关系 Mapper
    private final JobMapper jobMapper; // 岗位画像 Mapper

    public AnalysisServiceImpl(DeepSeekChatModel deepSeekChatModel, JobRelationMapper jobRelationMapper, JobMapper jobMapper) {
        this.deepSeekChatModel = deepSeekChatModel;
        this.jobRelationMapper = jobRelationMapper;
        this.jobMapper = jobMapper;
    }

    @Override
    public Result processFileWithModel(File file) throws IOException {
        // 如果是目录，则处理目录下所有文件
        if (file.isDirectory()) {
            return processDirectory(file);
        } else {
            return processSingleFile(file);
        }
    }
    /**
     * 处理单个文件（支持 .txt 和 .pdf）
     */
    private Result processSingleFile(File file) throws IOException {
        String content = extractTextFromFile(file);
        return analyzeContent(content);
    }

    /**
     * 处理目录下所有文件，合并内容后分析
     */
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
                    // 记录错误但继续处理其他文件
                    log.warn("解析文件失败: {} - {}", file.getName(), e.getMessage());
                }
            }
        }

        if (combinedContent.isEmpty()) {
            return Result.fail("没有成功提取到任何文件内容");
        }

        return analyzeContent(combinedContent.toString());
    }
    /**
     * 根据文件扩展名提取文本内容
     */
    private String extractTextFromFile(File file) throws IOException {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".txt")) {
            return new String(Files.readAllBytes(file.toPath()));
        } else if (fileName.endsWith(".pdf")) {
            return extractTextFromPdf(file);
        } else {
            // 可根据需要扩展其他格式
            throw new IOException("不支持的文件类型: " + fileName);
        }
    }
    /**
     * 使用 PDFBox 提取 PDF 文本
     */
    private String extractTextFromPdf(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
    /**
     * 通用的分析逻辑：调用 AI 模型并解析结果
     */
    private Result analyzeContent(String content) {
        List<JobProfile> jobProfiles = getAllJobProfiles();
        List<JobRelation> jobRelations = getAllJobRelations();

        String prompt = generatePrompt(content, jobProfiles, jobRelations);
        String aiAnalysisResult = deepSeekChatModel.call(prompt);
        return parseAnalysisResult(aiAnalysisResult);
    }

    private String generatePrompt(String content, List<JobProfile> jobProfiles, List<JobRelation> jobRelations) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位资深的职业规划顾问，专注于为大学生提供一对一的深度职业发展报告。请基于以下学生简历和岗位数据，生成一份专业、具体、个性化的分析报告。\n\n")
                .append("### 学生简历信息：\n").append(content).append("\n\n")
                .append("### 岗位画像数据：\n").append(jobProfiles.toString()).append("\n\n")
                .append("### 岗位之间的关系数据：\n").append(jobRelations.toString()).append("\n\n")
                .append("请严格按照以下 JSON 结构输出，字段名必须完全一致，不要包含任何额外文本或 Markdown 标记。\n\n")
                .append("报告结构如下：\n")
                .append("{\n")
                .append("  \"studentProfile\": {\n")
                .append("    \"professionalSkills\": [\"技能1\", \"技能2\", ...],      // 专业技能（6-8项）\n")
                .append("    \"certificates\": [\"证书1\", \"证书2\", ...],            // 已获证书\n")
                .append("    \"innovationAbilities\": [\"创新点1\", ...],             // 创新能力体现（项目、比赛等）\n")
                .append("    \"learningAbilities\": [\"学习能力体现1\", ...],         // 学习能力体现（GPA、自学成果等）\n")
                .append("    \"stressResistance\": [\"抗压事例1\", ...],               // 抗压能力体现\n")
                .append("    \"communicationSkills\": [\"沟通事例1\", ...],            // 沟通能力体现\n")
                .append("    \"internshipAbilities\": [\"实习经历/能力1\", ...],      // 实习能力体现\n")
                .append("    \"completenessScore\": 85.0,                            // 完整度评分（0-100）\n")
                .append("    \"competitivenessScore\": 78.0,                        // 竞争力评分（0-100）\n")
                .append("    \"overallAssessment\": \"综合评价，包含优势总结与改进方向\"\n")
                .append("  },\n")
                .append("  \"jobMatchAnalysis\": {\n")
                .append("    \"matchedJobs\": [\n")
                .append("      {\n")
                .append("        \"jobName\": \"岗位名称\",\n")
                .append("        \"matchScore\": 88.5,\n")
                .append("        \"strengths\": [\"与该岗位匹配的优势1\", ...],\n")
                .append("        \"gaps\": [\"与该岗位存在的差距1\", ...]\n")
                .append("      },\n")
                .append("      ... (3-5个岗位)\n")
                .append("    ],\n")
                .append("    \"overallMatchScore\": 82.0,                         // 总体匹配度\n")
                .append("    \"gapAnalysis\": \"综合差距分析，指出学生与目标岗位之间的主要差距及改进方向\"\n")
                .append("  },\n")
                .append("  \"careerPathPlan\": {\n")
                .append("    \"careerGoal\": \"明确的职业目标，如：成为一名优秀的C++后端架构师\",\n")
                .append("    \"industryTrend\": \"行业趋势分析（100-150字）\",\n")
                .append("    \"careerPath\": \"发展路径（如：初级开发 → 高级开发 → 技术专家/架构师）\",\n")
                .append("    \"keyMilestones\": [\"关键里程碑1\", \"关键里程碑2\", ...],\n")
                .append("    \"requiredSkills\": [\"需要培养的能力1\", \"需要培养的能力2\", ...]\n")
                .append("  },\n")
                .append("  \"actionPlan\": {\n")
                .append("    \"stages\": [\n")
                .append("      {\n")
                .append("        \"stageName\": \"短期（1-2年）\",\n")
                .append("        \"description\": \"阶段描述（50-80字）\",\n")
                .append("        \"actions\": [\"具体行动1\", \"具体行动2\", ...]\n")
                .append("      },\n")
                .append("      {\n")
                .append("        \"stageName\": \"中期（3-5年）\",\n")
                .append("        \"description\": \"阶段描述（50-80字）\",\n")
                .append("        \"actions\": [\"具体行动1\", \"具体行动2\", ...]\n")
                .append("      }\n")
                .append("      // 可根据需要增加长期阶段\n")
                .append("    ],\n")
                .append("    \"evaluationMetrics\": \"评估指标，如：技能掌握程度、项目参与度、技术分享次数\",\n")
                .append("    \"evaluationCycle\": \"评估周期，如：每半年一次\",\n")
                .append("    \"recommendedActions\": [\"额外建议1\", \"额外建议2\", ...]\n")
                .append("  }\n")
                .append("}\n\n")
                .append("**生成要求**：\n")
                .append("- 所有数组字段长度需符合上述提示（如技能6-8项，匹配岗位3-5个），内容要具体、个性化，结合学生简历中的实际信息（项目名、奖项、成绩等）。\n")
                .append("- 评分（completenessScore、competitivenessScore、overallMatchScore、matchScore）需基于简历内容合理给出，体现专业判断。\n")
                .append("- 职业路径、行业趋势、评估周期等描述要专业、有指导性，避免空泛。\n")
                .append("- 输出的 JSON 必须是一个完整的对象，不要包含任何注释或额外文本。\n\n")
                .append("请开始输出 JSON：");
        return prompt.toString();
    }

    private Result parseAnalysisResult(String aiAnalysisResult) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String cleaned = cleanJson(aiAnalysisResult);
            CareerReport report = objectMapper.readValue(cleaned, CareerReport.class);
            return Result.success(report);
        } catch (IOException e) {
            log.error("解析 AI 返回的 JSON 失败，原始内容：{}", aiAnalysisResult);
            return Result.fail(500, "AI 返回结果格式错误: " + e.getMessage());
        }
    }

    private String cleanJson(String input) {
        // 去除前后空白
        String cleaned = input.trim();
        // 去除可能的 Markdown 代码块标记
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        // 提取第一个 '{' 和最后一个 '}' 之间的内容
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            cleaned = cleaned.substring(start, end + 1);
        }
        return cleaned.trim();
    }

    @Override
    public List<JobProfile> getAllJobProfiles() {
        return jobMapper.getAllJobProfiles();
    }

    @Override
    public List<JobRelation> getAllJobRelations() {
        return jobRelationMapper.getAllJobRelations();
    }
}
