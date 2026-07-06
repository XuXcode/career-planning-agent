package com.mssj.service;

import com.mssj.pojo.CareerReport;
import com.mssj.pojo.JobProfile;
import com.mssj.pojo.MatchDimension;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 后端评分引擎 (v2.0)
 * 纯 Java 计算，不依赖 LLM，确保评分的客观性和一致性
 *
 * 核心功能:
 * 1. computeCompleteness — 简历完整度评分（7 个能力维度的覆盖比例）
 * 2. computeCompetitiveness — 简历竞争力评分（基于条目数量和质量关键词）
 * 3. computeWeightedTotal — 四维人岗匹配加权总分
 *    Score_total = w_base × S_base + w_skill × S_skill + w_quality × S_quality + w_potential × S_potential
 */
@Slf4j
@Component
public class ScoringEngine {

    /**
     * 计算简历完整度评分 (0-100)
     * 统计 StudentProfile 的 7 个能力维度中有多少非空，按比例计算
     *
     * @param profile 学生能力画像
     * @return 完整度评分 (0-100)
     */
    public double computeCompleteness(CareerReport.StudentProfile profile) {
        int filledDimensions = 0;
        int totalDimensions = 7;

        if (isNotEmpty(profile.getProfessionalSkills())) filledDimensions++;
        if (isNotEmpty(profile.getCertificates())) filledDimensions++;
        if (isNotEmpty(profile.getInnovationAbilities())) filledDimensions++;
        if (isNotEmpty(profile.getLearningAbilities())) filledDimensions++;
        if (isNotEmpty(profile.getStressResistance())) filledDimensions++;
        if (isNotEmpty(profile.getCommunicationSkills())) filledDimensions++;
        if (isNotEmpty(profile.getInternshipAbilities())) filledDimensions++;

        double score = ((double) filledDimensions / totalDimensions) * 100.0;
        log.debug("完整度评分: {}/{} 维度有内容 → {:.1f}", filledDimensions, totalDimensions, score);
        return Math.round(score * 10.0) / 10.0;
    }

    /**
     * 计算简历竞争力评分 (0-100)
     * 基于各维度的条目总数（对数缩放）和关键含金量指标加成
     *
     * @param profile 学生能力画像
     * @return 竞争力评分 (0-100)
     */
    public double computeCompetitiveness(CareerReport.StudentProfile profile) {
        int totalItems = 0;
        double qualityBonus = 0;

        totalItems += countAndScore(profile.getProfessionalSkills());
        totalItems += countAndScore(profile.getCertificates());
        totalItems += countAndScore(profile.getInnovationAbilities());
        totalItems += countAndScore(profile.getLearningAbilities());
        totalItems += countAndScore(profile.getStressResistance());
        totalItems += countAndScore(profile.getCommunicationSkills());
        totalItems += countAndScore(profile.getInternshipAbilities());

        // 证书含金量加成
        qualityBonus += scoreCertificates(profile.getCertificates());

        // 基础分: 条目数的对数缩放 (0-80)
        double baseScore = Math.min(80.0, Math.log1p(totalItems) * 25.0);

        // 质量加成 (0-20)
        double cappedBonus = Math.min(20.0, qualityBonus);

        double score = Math.min(100.0, baseScore + cappedBonus);
        log.debug("竞争力评分: {} 条目 + {} 质量加成 → {:.1f}", totalItems, qualityBonus, score);
        return Math.round(score * 10.0) / 10.0;
    }

    /**
     * 计算四维人岗匹配加权总分
     * Score_total = w_base × S_base + w_skill × S_skill + w_quality × S_quality + w_potential × S_potential
     *
     * @param dimensions LLM 返回的四维原始得分
     * @param jobProfile 数据库中的岗位画像（含四维权重）
     * @return 加权总分 (0-100)
     */
    public double computeWeightedTotal(MatchDimension dimensions, JobProfile jobProfile) {
        BigDecimal wBase = jobProfile.getWBase() != null ? jobProfile.getWBase() : BigDecimal.valueOf(0.25);
        BigDecimal wSkill = jobProfile.getWSkill() != null ? jobProfile.getWSkill() : BigDecimal.valueOf(0.25);
        BigDecimal wQuality = jobProfile.getWQuality() != null ? jobProfile.getWQuality() : BigDecimal.valueOf(0.25);
        BigDecimal wPotential = jobProfile.getWPotential() != null ? jobProfile.getWPotential() : BigDecimal.valueOf(0.25);

        BigDecimal sBase = BigDecimal.valueOf(dimensions.getBaseScore() != null ? dimensions.getBaseScore() : 0);
        BigDecimal sSkill = BigDecimal.valueOf(dimensions.getSkillScore() != null ? dimensions.getSkillScore() : 0);
        BigDecimal sQuality = BigDecimal.valueOf(dimensions.getQualityScore() != null ? dimensions.getQualityScore() : 0);
        BigDecimal sPotential = BigDecimal.valueOf(dimensions.getPotentialScore() != null ? dimensions.getPotentialScore() : 0);

        BigDecimal total = wBase.multiply(sBase)
                .add(wSkill.multiply(sSkill))
                .add(wQuality.multiply(sQuality))
                .add(wPotential.multiply(sPotential));

        double result = total.setScale(1, RoundingMode.HALF_UP).doubleValue();
        log.debug("加权总分: w={}/{}/{}/{} × s={}/{}/{}/{} = {}",
                wBase, wSkill, wQuality, wPotential,
                sBase, sSkill, sQuality, sPotential, result);
        return result;
    }

    // ─── 辅助方法 ──────────────────────────────────────────────

    private boolean isNotEmpty(List<String> list) {
        return list != null && !list.isEmpty();
    }

    private int countAndScore(List<String> list) {
        return (list != null) ? list.size() : 0;
    }

    /**
     * 证书含金量评分
     * 识别高价值证书关键词并累加分数
     */
    private double scoreCertificates(List<String> certificates) {
        if (certificates == null || certificates.isEmpty()) return 0;

        double bonus = 0;
        for (String cert : certificates) {
            String lower = cert.toLowerCase();
            // 高含金量证书
            if (lower.contains("pmp") || lower.contains("项目管理")) bonus += 3;
            if (lower.contains("cpa") || lower.contains("注册会计师")) bonus += 3;
            if (lower.contains("法考") || lower.contains("律师") || lower.contains("司法")) bonus += 3;
            if (lower.contains("n1") || lower.contains("n2") || lower.contains("日语")) bonus += 2;
            if (lower.contains("六级") || lower.contains("cet-6") || lower.contains("tem-8")) bonus += 2;
            if (lower.contains("专利代理") || lower.contains("专利")) bonus += 2;
            if (lower.contains("建造师") || lower.contains("造价")) bonus += 2;
            if (lower.contains("六西格玛") || lower.contains("黑带")) bonus += 2;
            if (lower.contains("aws") || lower.contains("云计算") || lower.contains("架构师")) bonus += 2;
            // 普通加分
            if (lower.contains("四级") || lower.contains("cet-4") || lower.contains("四级")) bonus += 1;
            if (lower.contains("计算机") || lower.contains("软考")) bonus += 1;
            if (lower.contains("普通话")) bonus += 0.5;
        }
        return bonus;
    }
}
