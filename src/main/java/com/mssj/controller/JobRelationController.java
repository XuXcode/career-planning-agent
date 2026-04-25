package com.mssj.controller;


import com.mssj.pojo.JobRelation;
import com.mssj.pojo.Result;
import com.mssj.service.JobRelationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/job-relation")
public class JobRelationController {

    @Autowired
    private JobRelationService jobRelationService;

    @GetMapping
    public Result findAllRelations() {
        log.info("查询所有岗位关系图谱");
        List<JobRelation> jobRelations = jobRelationService.findAllRelations();
        return Result.success(jobRelations);
}

    @GetMapping("/{jobId}")
    public Result findRelationsByJobId(@PathVariable Long jobId) {
        log.info("查询第" + jobId + "岗位关系图谱");
        List<JobRelation> jobRelations = jobRelationService.findRelationsByJobId(jobId);
        return Result.success(jobRelations);
    }
}
