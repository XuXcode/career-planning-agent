package com.mssj.mapper;

import com.mssj.pojo.JobProfile;
import com.mssj.pojo.JobProfileSample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface JobMapper {

    @Select("SELECT id, job_name FROM job_profile")
    @Results({
            @Result(property = "jobName", column = "job_name")
    })
    List<JobProfileSample> findJob();

    @Select("SELECT * FROM job_profile WHERE id = #{id}")
    JobProfile findById(Long id);

    @Select("SELECT * FROM job_profile")
    List<JobProfile> getAllJobProfiles();

    /**
     * 根据岗位名称查询岗位画像 (v2.0 新增，用于 ScoringEngine 按名称查权重)
     */
    @Select("SELECT * FROM job_profile WHERE job_name = #{jobName}")
    JobProfile findByJobName(String jobName);
}

