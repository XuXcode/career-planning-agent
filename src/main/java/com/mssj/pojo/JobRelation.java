package com.mssj.pojo;

import lombok.Data;

@Data
public class JobRelation {
    private Long id;
    private String sourceJob; // 源岗位ID
    private String targetJob; // 目标岗位ID
    private String relationType; // 关系类型：PROMOTION 或 TRANSITION
    private String relationCategory; // 关系类别
    private String skillGap; // 技能差距
    private String matchingScore; // 匹配分数
    private String reason; // 关系原因
}
