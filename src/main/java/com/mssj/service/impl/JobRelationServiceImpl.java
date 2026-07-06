package com.mssj.service.impl;


import com.mssj.mapper.JobRelationMapper;
import com.mssj.pojo.JobRelation;
import com.mssj.service.JobRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobRelationServiceImpl implements JobRelationService {

    @Autowired
    private JobRelationMapper jobRelationMapper;

    @Override
    public List<JobRelation> findAllRelations() {
        return jobRelationMapper.getAllJobRelations();
    }

    @Override
    public List<JobRelation> findRelationsByJobName(String jobName) {
        return jobRelationMapper.findRelationsByJobName(jobName);
    }
}
