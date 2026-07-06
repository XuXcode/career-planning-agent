-- ============================================================
-- 职业规划智能体 — 数据库 DDL Schema
-- 版本: v2.0
-- 数据库: career_db
-- ============================================================

CREATE DATABASE IF NOT EXISTS career_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE career_db;

-- ----------------------------
-- Table: user
-- ----------------------------
CREATE TABLE IF NOT EXISTS user (
    id       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(64)  NOT NULL COMMENT '用户名',
    password VARCHAR(128) NOT NULL COMMENT '密码',
    role     VARCHAR(32)  NOT NULL DEFAULT 'user' COMMENT '角色: user / admin',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- Table: job_profile
-- v2.0: 新增四维权重字段 (w_base, w_skill, w_quality, w_potential)
-- ----------------------------
CREATE TABLE IF NOT EXISTS job_profile (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
    job_name      VARCHAR(128)  NOT NULL COMMENT '岗位名称',
    skills        LONGTEXT      COMMENT '技能要求描述',
    cert          LONGTEXT      COMMENT '证书要求描述',
    innovation    LONGTEXT      COMMENT '创新能力描述',
    learning      LONGTEXT      COMMENT '学习能力描述',
    pressure      LONGTEXT      COMMENT '抗压能力描述',
    communication LONGTEXT      COMMENT '沟通能力描述',
    practical     LONGTEXT      COMMENT '实践能力描述',
    description   LONGTEXT      COMMENT '岗位综合描述',
    score         INT           DEFAULT 0 COMMENT '岗位综合分数',

    -- 四维人岗匹配权重 (v2.0 新增)
    -- 四个权重之和应为 1.000
    w_base       DECIMAL(4,3)  NOT NULL DEFAULT 0.250 COMMENT '基础要求权重',
    w_skill      DECIMAL(4,3)  NOT NULL DEFAULT 0.250 COMMENT '职业技能权重',
    w_quality    DECIMAL(4,3)  NOT NULL DEFAULT 0.250 COMMENT '职业素养权重',
    w_potential  DECIMAL(4,3)  NOT NULL DEFAULT 0.250 COMMENT '发展潜力权重',

    create_time   DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_job_name (job_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位画像表';

-- ----------------------------
-- Table: job_relation
-- v2.0: 修正列名 (source_job/target_job 使用岗位名称而非ID),
--       matching_score 改为 INT, transition_path 改为 JSON 类型
-- ----------------------------
CREATE TABLE IF NOT EXISTS job_relation (
    id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '关系ID',
    source_job        VARCHAR(128) NOT NULL COMMENT '源岗位名称',
    target_job        VARCHAR(128) NOT NULL COMMENT '目标岗位名称',
    relation_type     VARCHAR(32)  NOT NULL COMMENT '关系类型: PROMOTION(垂直晋升) / TRANSITION(横向转岗)',
    relation_category VARCHAR(64)  COMMENT '关系类别: 垂直晋升 / 横向转岗 / 跨领域',
    skill_gap         VARCHAR(512) COMMENT '技能差距描述',
    matching_score    INT          DEFAULT 0 COMMENT '匹配分数 (0-100)',
    transition_path   JSON         COMMENT '转岗路径 JSON 数组，如 ["岗位A","岗位B","岗位C"]',
    reason            VARCHAR(512) COMMENT '关系原因说明',
    created_at        DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_source_job (source_job),
    KEY idx_target_job (target_job),
    KEY idx_relation_type (relation_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位关系图谱表';
