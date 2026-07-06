package com.mssj.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 递归岗位路径查询结果 DTO (v2.0)
 * 用于接收 MyBatis Recursive CTE 的查询结果
 */
@Data
public class JobRelationPath {
    private Long id;
    private String sourceJob;
    private String targetJob;
    private String relationType;
    private String relationCategory;
    private String skillGap;
    private Integer matchingScore;
    private String transitionPath;
    private String reason;
    private LocalDateTime createdAt;

    /** 递归深度（从起点算起，1 表示直接关系） */
    private Integer depth;

    /** 完整路径字符串，如 "Java开发->高级Java->架构师" */
    private String fullPath;
}
