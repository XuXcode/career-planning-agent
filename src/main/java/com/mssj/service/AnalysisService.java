package com.mssj.service;

import com.mssj.pojo.JobProfile;
import com.mssj.pojo.JobRelation;
import com.mssj.pojo.Result;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface AnalysisService {
    Result processFileWithModel(File file) throws IOException;

    List<JobProfile> getAllJobProfiles();

    List<JobRelation> getAllJobRelations();
}
