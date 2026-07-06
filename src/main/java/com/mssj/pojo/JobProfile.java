package com.mssj.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobProfile {

    private Long id;

    private String jobName;

    private String skills;

    private String cert;

    private String innovation;

    private String learning;

    private String pressure;

    private String communication;

    private String practical;

    private String description;

    private Integer score;

    /**
     * 四维人岗匹配权重 (v2.0)
     * 四个权重之和应为 1.000，用于加权匹配算法
     */
    private BigDecimal wBase;      // 基础要求权重
    private BigDecimal wSkill;     // 职业技能权重
    private BigDecimal wQuality;   // 职业素养权重
    private BigDecimal wPotential; // 发展潜力权重

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
