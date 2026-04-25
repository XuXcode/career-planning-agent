package com.mssj.service.impl;

import com.mssj.mapper.JobMapper;
import com.mssj.pojo.JobProfile;
import com.mssj.pojo.JobProfileSample;
import com.mssj.service.JobProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobProfileServiceImpl implements JobProfileService {
    @Autowired
    private JobMapper jobMapper;
    @Override
    public List<JobProfileSample> findJob() {
        return jobMapper.findJob();
    }

    @Override
    public JobProfile findById(Long id) {
        return jobMapper.findById(id);
    }
}
