package com.mssj.mapper;

import com.mssj.pojo.JobProfile;
import com.mssj.pojo.JobProfileSample;
import com.mssj.pojo.JobRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface JobMapper {
    @Select("select id, job_name from job_profile")
    @Results({
            @Result(property = "jobName", column = "job_name")
    })
    List<JobProfileSample> findJob();

    @Select("SELECT * FROM job_profile WHERE id = #{id}")
    JobProfile findById(Long id);

    @Select("SELECT * FROM job_relation")
    List<JobRelation> findAllRelations();

    @Select("SELECT * FROM job_relation WHERE source_job = #{jobId} OR target_job = #{jobId}")
    List<JobRelation> findRelationsByJobId(Long jobId);

    @Select("SELECT * FROM job_profile")
    List<JobProfile> getAllJobProfiles();
}

