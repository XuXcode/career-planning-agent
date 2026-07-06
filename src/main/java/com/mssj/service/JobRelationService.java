package com.mssj.service;

import com.mssj.pojo.JobRelation;

import java.util.List;

public interface JobRelationService {
    List<JobRelation> findAllRelations();

    /**
     * 根据岗位名称查询关联关系 (v2.0: 参数类型 Long → String)
     */
    List<JobRelation> findRelationsByJobName(String jobName);
}
