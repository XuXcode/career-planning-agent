package com.mssj.service.impl;

import com.mssj.mapper.JobMapper;
import com.mssj.mapper.JobRelationMapper;
import com.mssj.pojo.*;
import com.mssj.service.GraphService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 岗位关系图谱服务实现 (v2.0)
 *
 * 核心流程:
 * 1. 从数据库获取所有锚点岗位（在关系图中出现的岗位名称）
 * 2. 对每个锚点岗位执行 Recursive CTE，获取多级下行路径
 * 3. 合并去重所有路径中的节点和边
 * 4. 输出 GraphResponse（Nodes + Edges），直接适配 ECharts Graph / AntV G6
 */
@Slf4j
@Service
public class GraphServiceImpl implements GraphService {

    private final JobRelationMapper jobRelationMapper;
    private final JobMapper jobMapper;

    public GraphServiceImpl(JobRelationMapper jobRelationMapper, JobMapper jobMapper) {
        this.jobRelationMapper = jobRelationMapper;
        this.jobMapper = jobMapper;
    }

    @Override
    public GraphResponse getFullGraph(int maxDepth) {
        // 获取所有锚点岗位名称
        List<String> anchorJobs = jobRelationMapper.findAnchorJobNames();
        if (anchorJobs.isEmpty()) {
            log.warn("未找到任何锚点岗位");
            return GraphResponse.empty();
        }

        log.info("开始构建全量图谱: {} 个锚点, maxDepth={}", anchorJobs.size(), maxDepth);

        // 预加载所有岗位画像（用于补全节点信息）
        List<JobProfile> allProfiles = jobMapper.getAllJobProfiles();
        Map<String, JobProfile> profileMap = allProfiles.stream()
                .collect(Collectors.toMap(JobProfile::getJobName, j -> j, (a, b) -> a));

        GraphResponse graph = new GraphResponse();
        Set<String> visitedEdges = new HashSet<>();

        for (String anchor : anchorJobs) {
            try {
                List<JobRelationPath> paths = jobRelationMapper.findDescendantPaths(anchor, maxDepth);
                for (JobRelationPath path : paths) {
                    // 添加边（去重）
                    String edgeKey = path.getSourceJob() + "->" + path.getTargetJob();
                    if (visitedEdges.add(edgeKey)) {
                        graph.addEdge(new GraphResponse.GraphEdge(
                                path.getSourceJob(),
                                path.getTargetJob(),
                                path.getRelationType(),
                                path.getMatchingScore(),
                                path.getRelationCategory(),
                                path.getSkillGap()
                        ));
                    }

                    // 添加源节点
                    addNodeFromProfile(graph, path.getSourceJob(), profileMap,
                            path.getDepth() != null && path.getDepth() == 1 ? "anchor" : "intermediate");

                    // 添加目标节点
                    String targetCategory = "leaf";
                    addNodeFromProfile(graph, path.getTargetJob(), profileMap, targetCategory);
                }
            } catch (Exception e) {
                log.warn("锚点 [{}] 递归查询失败: {}", anchor, e.getMessage());
            }
        }

        // 重新计算节点类别（某节点若作为多条边的源，则不是 leaf）
        recategorizeNodes(graph);

        log.info("图谱构建完成: {} 个节点, {} 条边", graph.getNodes().size(), graph.getEdges().size());
        return graph;
    }

    @Override
    public GraphResponse getGraphFromJob(String jobName, int maxDepth) {
        List<JobProfile> allProfiles = jobMapper.getAllJobProfiles();
        Map<String, JobProfile> profileMap = allProfiles.stream()
                .collect(Collectors.toMap(JobProfile::getJobName, j -> j, (a, b) -> a));

        List<JobRelationPath> paths = jobRelationMapper.findDescendantPaths(jobName, maxDepth);

        if (paths.isEmpty()) {
            log.info("岗位 [{}] 无下游关系", jobName);
            GraphResponse empty = GraphResponse.empty();
            // 至少返回查询岗位本身
            JobProfile profile = profileMap.get(jobName);
            empty.addNode(new GraphResponse.GraphNode(
                    jobName, jobName,
                    profile != null ? profile.getScore() : 0,
                    "anchor",
                    profile != null ? truncateDescription(profile.getDescription()) : null
            ));
            return empty;
        }

        GraphResponse graph = new GraphResponse();
        Set<String> visitedEdges = new HashSet<>();

        for (JobRelationPath path : paths) {
            String edgeKey = path.getSourceJob() + "->" + path.getTargetJob();
            if (visitedEdges.add(edgeKey)) {
                graph.addEdge(new GraphResponse.GraphEdge(
                        path.getSourceJob(),
                        path.getTargetJob(),
                        path.getRelationType(),
                        path.getMatchingScore(),
                        path.getRelationCategory(),
                        path.getSkillGap()
                ));
            }
            addNodeFromProfile(graph, path.getSourceJob(), profileMap,
                    path.getDepth() == 1 ? "anchor" : "intermediate");
            addNodeFromProfile(graph, path.getTargetJob(), profileMap, "leaf");
        }

        recategorizeNodes(graph);

        log.info("岗位 [{}] 图谱: {} 个节点, {} 条边", jobName, graph.getNodes().size(), graph.getEdges().size());
        return graph;
    }

    // ─── 辅助方法 ──────────────────────────────────────────────

    private void addNodeFromProfile(GraphResponse graph, String jobName,
                                     Map<String, JobProfile> profileMap, String defaultCategory) {
        if (graph.getNodes().stream().anyMatch(n -> n.getId().equals(jobName))) {
            return;
        }
        JobProfile profile = profileMap.get(jobName);
        graph.addNode(new GraphResponse.GraphNode(
                jobName,
                jobName,
                profile != null ? profile.getScore() : 0,
                defaultCategory,
                profile != null ? truncateDescription(profile.getDescription()) : null
        ));
    }

    /**
     * 重新计算节点类别：若某节点作为 source 出现在任何边中，则标记为 intermediate
     */
    private void recategorizeNodes(GraphResponse graph) {
        Set<String> sourceNodes = graph.getEdges().stream()
                .map(GraphResponse.GraphEdge::getSource)
                .collect(Collectors.toSet());

        Set<String> targetNodes = graph.getEdges().stream()
                .map(GraphResponse.GraphEdge::getTarget)
                .collect(Collectors.toSet());

        for (GraphResponse.GraphNode node : graph.getNodes()) {
            if (sourceNodes.contains(node.getId()) && targetNodes.contains(node.getId())) {
                node.setCategory("intermediate");
            } else if (sourceNodes.contains(node.getId())) {
                // 作为源但不在目标中 → 可能仍是 anchor
                // 保持默认
            } else if (targetNodes.contains(node.getId()) && !sourceNodes.contains(node.getId())) {
                node.setCategory("leaf");
            }
        }
    }

    private String truncateDescription(String desc) {
        if (desc == null) return null;
        return desc.length() > 100 ? desc.substring(0, 97) + "..." : desc;
    }
}
