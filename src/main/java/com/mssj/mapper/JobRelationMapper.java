package com.mssj.mapper;

import com.mssj.pojo.JobRelation;
import com.mssj.pojo.JobRelationPath;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface JobRelationMapper {

    /**
     * 查询所有岗位关系
     */
    @Select("SELECT * FROM job_relation")
    List<JobRelation> getAllJobRelations();

    /**
     * 根据岗位名称查询关联关系（源岗位或目标岗位匹配）
     * v2.0 修复: 列名 source_job_id → source_job, target_job_id → target_job
     */
    @Select("SELECT * FROM job_relation WHERE source_job = #{jobName} OR target_job = #{jobName}")
    List<JobRelation> findRelationsByJobName(String jobName);

    /**
     * 获取所有锚点岗位名称（在关系图中出现过的所有岗位）
     */
    @Select("SELECT DISTINCT source_job FROM job_relation " +
            "UNION " +
            "SELECT DISTINCT target_job FROM job_relation")
    List<String> findAnchorJobNames();

    /**
     * 递归查询多级岗位路径（在 JobRelationMapper.xml 中实现 Recursive CTE）
     * @param startJobName 起始岗位名称
     * @param maxDepth     最大递归深度
     * @return 多级路径结果列表
     */
    List<JobRelationPath> findDescendantPaths(@Param("startJobName") String startJobName,
                                              @Param("maxDepth") Integer maxDepth);
}
