package com.mssj.service;

import com.mssj.pojo.JobProfile;
import com.mssj.pojo.JobProfileSample;

import java.util.List;

public interface JobProfileService {

    List<JobProfileSample> findJob();

    JobProfile findById(Long id);
}
