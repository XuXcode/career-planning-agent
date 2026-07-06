package com.mssj.pojo;

import lombok.Data;

/**
 * 四维人岗匹配得分 DTO (v2.0)
 * LLM 对每个匹配岗位返回的 4 个维度原始得分（均为 100 分制）
 * 后端 ScoringEngine 结合岗位权重计算加权总分
 */
@Data
public class MatchDimension {
    /**
     * S_base — 基础要求得分 (0-100)
     * 学历、专业背景、基础素质等与岗位基本门槛的匹配度
     */
    private Double baseScore;

    /**
     * S_skill — 职业技能得分 (0-100)
     * 专业技能、技术栈、工具使用等与岗位技能要求的匹配度
     */
    private Double skillScore;

    /**
     * S_quality — 职业素养得分 (0-100)
     * 沟通、抗压、团队协作、责任心等软素质的匹配度
     */
    private Double qualityScore;

    /**
     * S_potential — 发展潜力得分 (0-100)
     * 学习能力、创新能力、成长空间等未来发展潜力的匹配度
     */
    private Double potentialScore;
}
