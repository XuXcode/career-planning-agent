package com.mssj.controller;

import com.mssj.pojo.GraphResponse;
import com.mssj.pojo.Result;
import com.mssj.service.GraphService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 岗位关系图谱控制器 (v2.0)
 * 返回符合 ECharts Graph / AntV G6 标准的 Nodes + Edges JSON 结构
 */
@Slf4j
@RestController
@RequestMapping("/job-graph")
public class JobGraphController {

    @Autowired
    private GraphService graphService;

    /**
     * 获取全量岗位关系图谱
     * GET /job-graph/full?maxDepth=3
     */
    @GetMapping("/full")
    public Result getFullGraph(@RequestParam(defaultValue = "3") int maxDepth) {
        log.info("查询全量图谱, maxDepth={}", Math.min(maxDepth, 5));
        int depth = Math.min(maxDepth, 5); // 安全限制: 最大深度 5
        GraphResponse graph = graphService.getFullGraph(depth);
        return Result.success(graph);
    }

    /**
     * 从指定岗位出发的局部图谱
     * GET /job-graph/from/{jobName}?maxDepth=3
     */
    @GetMapping("/from/{jobName}")
    public Result getGraphFromJob(@PathVariable String jobName,
                                  @RequestParam(defaultValue = "3") int maxDepth) {
        log.info("查询岗位 [{}] 的图谱, maxDepth={}", jobName, Math.min(maxDepth, 5));
        int depth = Math.min(maxDepth, 5);
        GraphResponse graph = graphService.getGraphFromJob(jobName, depth);
        return Result.success(graph);
    }
}
