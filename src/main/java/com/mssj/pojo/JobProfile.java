package com.mssj.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
