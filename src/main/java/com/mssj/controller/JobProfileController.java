package com.mssj.controller;

import com.mssj.pojo.JobProfileSample;
import com.mssj.pojo.Result;
import com.mssj.service.JobProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/job-profile")
public class JobProfileController {
    @Autowired
    private JobProfileService jobService;
    @GetMapping
    public Result findJob() {
        log.info("查询岗位画像信息");
        List<JobProfileSample> ProfileList = jobService.findJob();
        return Result.success(ProfileList);
    }
    @GetMapping("/{id}")
    public Result findById(@PathVariable Long id) {
        log.info("查询第"+id+"岗位的画像信息");
        return Result.success(jobService.findById(id));

    }

}
