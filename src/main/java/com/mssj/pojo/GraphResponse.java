package com.mssj.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 岗位关联图谱响应 DTO (v2.0)
 * 返回符合前端图可视化库（ECharts Graph / AntV G6）标准的 Nodes + Edges JSON 结构
 */
@Data
public class GraphResponse {
    private List<GraphNode> nodes = new ArrayList<>();
    private List<GraphEdge> edges = new ArrayList<>();

    // ─── 内部类 ──────────────────────────────────────────────

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GraphNode {
        /** 节点唯一标识（岗位名称） */
        private String id;

        /** 显示标签 */
        private String label;

        /** 岗位综合分数（用于节点大小映射） */
        private Integer score;

        /** 节点类别: anchor(锚点/起点) / intermediate(中间节点) / leaf(叶子节点) */
        private String category;

        /** 附加信息：岗位描述摘要（可选） */
        private String description;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GraphEdge {
        /** 源节点 ID（岗位名称） */
        private String source;

        /** 目标节点 ID（岗位名称） */
        private String target;

        /** 关系类型: PROMOTION / TRANSITION */
        private String type;

        /** 关系分数（用于边粗细映射） */
        private Integer score;

        /** 关系类别标签 */
        private String label;

        /** 技能差距描述 */
        private String skillGap;
    }

    // ─── 工厂方法 ──────────────────────────────────────────────

    public static GraphResponse empty() {
        return new GraphResponse();
    }

    public void addNode(GraphNode node) {
        if (nodes == null) nodes = new ArrayList<>();
        // 去重
        if (nodes.stream().noneMatch(n -> n.getId().equals(node.getId()))) {
            nodes.add(node);
        }
    }

    public void addEdge(GraphEdge edge) {
        if (edges == null) edges = new ArrayList<>();
        edges.add(edge);
    }
}
