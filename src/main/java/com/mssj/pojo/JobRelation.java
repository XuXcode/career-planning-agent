package com.mssj.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobRelation {
    private Long id;
    private String sourceJob;       // 源岗位名称
    private String targetJob;       // 目标岗位名称
    private String relationType;    // 关系类型：PROMOTION 或 TRANSITION
    private String relationCategory; // 关系类别：垂直晋升 / 横向转岗 / 跨领域
    private String skillGap;        // 技能差距描述
    private Integer matchingScore;  // 匹配分数 (0-100) — v2.0 修正为 INT
    private String transitionPath;  // 转岗路径 JSON 数组 — v2.0 新增
    private String reason;          // 关系原因说明
    private LocalDateTime createdAt; // 创建时间 — v2.0 新增
}
