-- ============================================================
-- 职业规划智能体 — 数据库迁移脚本 v1.0 → v2.0
-- 执行前提: 已有 career_db 数据库及 job_profile / job_relation 表
-- ============================================================

USE career_db;

-- 1. job_profile: 新增四维权重列
ALTER TABLE job_profile
    ADD COLUMN IF NOT EXISTS w_base      DECIMAL(4,3) NOT NULL DEFAULT 0.250 COMMENT '基础要求权重',
    ADD COLUMN IF NOT EXISTS w_skill     DECIMAL(4,3) NOT NULL DEFAULT 0.250 COMMENT '职业技能权重',
    ADD COLUMN IF NOT EXISTS w_quality   DECIMAL(4,3) NOT NULL DEFAULT 0.250 COMMENT '职业素养权重',
    ADD COLUMN IF NOT EXISTS w_potential DECIMAL(4,3) NOT NULL DEFAULT 0.250 COMMENT '发展潜力权重';

-- 2. job_relation: 修正 matching_score 类型为 INT
ALTER TABLE job_relation
    MODIFY COLUMN matching_score INT DEFAULT 0 COMMENT '匹配分数 (0-100)';

-- 3. job_relation: 确保 transition_path 列为 JSON 类型
ALTER TABLE job_relation
    MODIFY COLUMN transition_path JSON COMMENT '转岗路径 JSON 数组';

-- 4. job_relation: 确保 created_at 列存在
ALTER TABLE job_relation
    ADD COLUMN IF NOT EXISTS created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';

-- 验证迁移结果
SELECT 'migration_v2 completed successfully' AS status;
