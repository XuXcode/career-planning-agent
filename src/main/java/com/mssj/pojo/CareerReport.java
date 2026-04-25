package com.mssj.pojo;

import lombok.Data;
import java.util.List;

@Data
public class CareerReport {
    private StudentProfile studentProfile; // 学生就业能力画像
    private JobMatchAnalysis jobMatchAnalysis; // 职业探索与岗位匹配
    private CareerPathPlan careerPathPlan; // 职业目标设定与职业路径
    private ActionPlan actionPlan; // 行动计划与成果展示

    @Data
    public static class StudentProfile {
        private List<String> professionalSkills; // 专业技能
        private List<String> certificates; // 证书
        private List<String> innovationAbilities; // 创新能力体现
        private List<String> learningAbilities; // 学习能力体现
        private List<String> stressResistance; // 抗压能力体现
        private List<String> communicationSkills; // 沟通能力体现
        private List<String> internshipAbilities; // 实习能力体现
        private Double completenessScore; // 完整度评分 (0-100)
        private Double competitivenessScore; // 竞争力评分 (0-100)
        private String overallAssessment; // 综合评价
    }

    @Data
    public static class JobMatchAnalysis {
        private List<JobMatchDetail> matchedJobs; // 匹配的岗位列表
        private Double overallMatchScore; // 总体匹配度
        private String gapAnalysis; // 差距分析
    }

    @Data
    public static class JobMatchDetail {
        private String jobName; // 岗位名称
        private Double matchScore; // 匹配度分数
        private List<String> strengths; // 与该岗位匹配的优势
        private List<String> gaps; // 与该岗位存在的差距
    }

    @Data
    public static class CareerPathPlan {
        private String careerGoal; // 职业目标
        private String industryTrend; // 行业趋势
        private String careerPath; // 职业发展路径
        private List<String> keyMilestones; // 关键里程碑
        private List<String> requiredSkills; // 需要培养的能力
    }

    @Data
    public static class ActionPlan {
        private List<StagePlan> stages; // 分阶段计划
        private String evaluationMetrics; // 评估指标
        private String evaluationCycle; // 评估周期
        private List<String> recommendedActions; // 推荐行动
    }

    @Data
    public static class StagePlan {
        private String stageName; // 阶段名称（短期/中期/长期）
        private String description; // 阶段描述
        private List<String> actions; // 具体行动项
    }
}

