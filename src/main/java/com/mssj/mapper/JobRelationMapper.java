package com.mssj.mapper;

import com.mssj.pojo.JobRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface JobRelationMapper {

    @Select("SELECT * FROM job_relation")
    List<JobRelation> findAllRelations();

    @Select("SELECT * FROM job_relation WHERE source_job_id = #{jobId} OR target_job_id = #{jobId}")
    List<JobRelation> findRelationsByJobId(Long jobId);

    @Select("SELECT * FROM job_relation")
    List<JobRelation> getAllJobRelations();
}
