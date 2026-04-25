package com.mssj.service;

import com.mssj.pojo.JobRelation;

import java.util.List;

public interface JobRelationService {
    List<JobRelation> findAllRelations();
    List<JobRelation> findRelationsByJobId(Long jobId);
}
