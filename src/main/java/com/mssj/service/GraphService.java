package com.mssj.service;

import com.mssj.pojo.GraphResponse;

/**
 * 岗位关系图谱服务接口 (v2.0)
 */
public interface GraphService {

    /**
     * 获取全量图谱（从所有锚点岗位出发，递归展开 maxDepth 层）
     *
     * @param maxDepth 最大递归深度（默认建议 3）
     * @return 包含 nodes 和 edges 的图谱响应
     */
    GraphResponse getFullGraph(int maxDepth);

    /**
     * 从指定岗位出发的局部图谱
     *
     * @param jobName  起始岗位名称
     * @param maxDepth 最大递归深度
     * @return 包含 nodes 和 edges 的图谱响应
     */
    GraphResponse getGraphFromJob(String jobName, int maxDepth);
}
