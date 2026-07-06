package com.mssj.service;

import com.mssj.pojo.CareerReport;
import com.mssj.pojo.JobProfile;
import com.mssj.pojo.JobRelation;
import com.mssj.pojo.Result;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface AnalysisService {

    /**
     * 同步全流程分析（兼容旧接口）
     */
    Result processFileWithModel(File file) throws IOException;

    /**
     * Phase 1: 从简历文本中提取结构化能力画像 (v2.0)
     */
    CareerReport.StudentProfile extractProfile(String resumeContent);

    /**
     * Phase 2: 基于能力画像进行岗位匹配与四维评分 (v2.0)
     */
    CareerReport.JobMatchAnalysis matchJobs(CareerReport.StudentProfile profile);

    /**
     * Phase 3: 生成职业目标与路径规划 (v2.0)
     */
    CareerReport.CareerPathPlan planCareerPath(CareerReport.StudentProfile profile,
                                                CareerReport.JobMatchAnalysis matchAnalysis);

    /**
     * Phase 4: 生成行动计划 (v2.0)
     */
    CareerReport.ActionPlan generateActionPlan(CareerReport.StudentProfile profile,
                                                CareerReport.JobMatchAnalysis matchAnalysis,
                                                CareerReport.CareerPathPlan pathPlan);

    List<JobProfile> getAllJobProfiles();

    List<JobRelation> getAllJobRelations();
}
