# AI 职业规划智能体

全国创新创业大赛参赛项目 -- 基于 Spring Boot + MyBatis + DeepSeek AI + Vue 3 构建的智能职业规划系统。实现结构化简历提取、四维加权人岗匹配、递归岗位图谱遍历和 SSE 流式分析。

## 项目简介

本项目将传统的大模型套壳应用升级为具备自主工具调用、图谱分析和精确量化算法的真正 AI 智能体。系统帮助大学生分析简历、匹配适合的岗位、探索职业发展路径，并生成个性化的职业规划报告。

核心能力：

- **结构化画像提取**: 通过 JSON Mode 强制 LLM 输出结构化数据，从简历中精准拆解出专业技能、证书、创新能力、学习能力、抗压能力、沟通能力、实习能力共 7 个能力维度。
- **四维加权人岗匹配**: 从基础要求、职业技能、职业素养、发展潜力四个维度进行评分，每个维度 0-100 分，结合数据库中各岗位的权重配置，通过后端加权求和公式计算最终匹配度。
- **递归岗位图谱**: 基于 MySQL Recursive CTE 实现多级岗位路径深度遍历，支持垂直晋升和横向转岗路径搜索，返回符合 ECharts/AntV G6 标准的 Nodes + Edges 结构。
- **SSE 流式分析**: 使用 Server-Sent Events 将报告生成过程分四个阶段实时推送到前端，解决同步阻塞超时问题。
- **后端评分引擎**: 完整度评分和竞争力评分由纯 Java 代码计算，不依赖 LLM 臆测，保证评分的一致性和可解释性。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Java 17, Spring Boot 3.5.11 |
| AI 模型 | DeepSeek Chat (Spring AI starter) |
| 数据库 | MySQL 8.0+ (career_db) |
| ORM | MyBatis 3.0.5 (注解 + XML Recursive CTE) |
| 安全认证 | JWT (jjwt 0.11.5)，Filter + Interceptor 双重防护 |
| 文件处理 | Apache PDFBox 2.0.30, OpenCSV 5.7.1 |
| 前端 | Vue 3 (Composition API), Vite 5, Vue Router 4, Pinia, ECharts 5 |
| 文档导出 | poi-tl 1.12.2 (Word), iText 8.0.4 (PDF), html2canvas + jsPDF (客户端降级) |
| 构建工具 | Maven (后端), npm/Vite (前端) |

## 系统架构

```
┌──────────────────────────────────────────────────┐
│           Vue 3 SPA 前端 (端口 5173)               │
│  登录 / 注册 / 仪表盘 (3 个 Tab)                    │
│  ECharts 雷达图 + 关系图谱, SSE 流式推送            │
│                    │  HTTP / SSE                  │
│           Vite proxy -> localhost:8080             │
└────────────────────┬─────────────────────────────┘
                     │
┌────────────────────┴─────────────────────────────┐
│              JWT Filter + Interceptor              │
│       (com.mssj.filter / com.mssj.interceptor)    │
└────────────────────┬─────────────────────────────┘
                     │
┌────────────────────┴─────────────────────────────┐
│                   控制器层                          │
│  AuthController  │  StuAnalysisController          │
│  StuFileController  │  StuAnalysisStreamController │
│  JobProfileController  │  JobRelationController    │
│  JobGraphController                               │
└────────────────────┬─────────────────────────────┘
                     │
┌────────────────────┴─────────────────────────────┐
│                   服务层 (v2.0)                     │
│  AnalysisServiceImpl (4 阶段 Prompt + 重试机制)     │
│  ScoringEngine (完整度/竞争力/加权匹配计算)          │
│  GraphServiceImpl (Recursive CTE -> GraphResponse) │
│  UserService / JobProfileService / JobRelationService│
└────────────────────┬─────────────────────────────┘
                     │
┌────────────────────┴─────────────────────────────┐
│             MyBatis Mappers + MySQL                │
│  user / job_profile (47 行) / job_relation (53 行) │
│  JobRelationMapper.xml (Recursive CTE)            │
└────────────────────┬─────────────────────────────┘
                     │
┌────────────────────┴─────────────────────────────┐
│           DeepSeek AI (Spring AI)                  │
│  阶段一: 简历能力提取 (结构化 JSON)                  │
│  阶段二: 岗位匹配 (四维评分)                         │
│  阶段三: 职业路径规划                               │
│  阶段四: 行动计划生成                               │
└──────────────────────────────────────────────────┘
```

## API 接口

### 认证相关 (`/auth/**`)

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/auth/login` | 无 | 登录，返回 JWT Token |
| POST | `/auth/register` | 无 | 注册新用户 |
| POST | `/auth/logout` | 无 | 登出 |
| POST | `/auth/refresh` | 无 | 刷新 JWT Token |
| GET | `/auth/info` | JWT | 获取当前用户信息 |
| GET | `/auth/username` | JWT | 获取当前用户名 |
| GET | `/auth/check-username` | 无 | 检查用户名是否可用 |

### 学生操作 (`/stu/**`)

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/stu/uploadResume` | JWT | 上传简历文件 (PDF/TXT) |
| POST | `/stu/uploadText` | JWT | 提交纯文本简历 |
| POST | `/stu/analysis` | JWT | 同步 AI 分析 (兼容旧版) |
| GET | `/stu/analysis/stream` | JWT | SSE 流式分析 (v2.0) |
| GET | `/stu/export/report/pdf` | JWT | PDF 导出 (开发中) |
| GET | `/stu/export/report/word` | JWT | Word 导出 (开发中) |

### 岗位画像 (`/job-profile/**`)

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/job-profile` | 无 | 获取所有岗位画像摘要 |
| GET | `/job-profile/{id}` | 无 | 获取单个岗位详细画像 |

### 岗位关系 (`/job-relation/**`)

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/job-relation` | 无 | 获取所有岗位关系 |
| GET | `/job-relation/{jobName}` | 无 | 按岗位名称查询关系 (v2.0: String 参数) |

### 岗位图谱 (`/job-graph/**`) -- v2.0 新增

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/job-graph/full?maxDepth=3` | 无 | 全量岗位图谱 (Recursive CTE) |
| GET | `/job-graph/from/{jobName}?maxDepth=3` | 无 | 从指定岗位出发的多级路径图谱 |

## 四维加权人岗匹配算法 (v2.0)

每个岗位的最终匹配度通过以下加权求和公式计算：

```
Score_total = w_base * S_base + w_skill * S_skill + w_quality * S_quality + w_potential * S_potential
```

| 符号 | 维度名称 | 数据来源 |
|------|----------|----------|
| S_base | 基础要求 | LLM 返回的 JSON (0-100) |
| S_skill | 职业技能 | LLM 返回的 JSON (0-100) |
| S_quality | 职业素养 | LLM 返回的 JSON (0-100) |
| S_potential | 发展潜力 | LLM 返回的 JSON (0-100) |
| w_base, w_skill, w_quality, w_potential | 各维度权重 | `job_profile` 数据库表 |

权重存储在岗位画像表中 (`w_base`, `w_skill`, `w_quality`, `w_potential` 字段)，每个字段 0.000-1.000，四项之和为 1.000。不同岗位类型有差异化的权重配置 -- 例如 Java/C++ 开发岗位侧重技能 (w_skill=0.40)，科研岗位侧重潜力 (w_potential=0.35)，项目经理岗位侧重素养 (w_quality=0.35)。

后端 `ScoringEngine` 还负责计算：
- **完整度评分**: 7 个能力维度中有数据的比例，换算为 0-100 分。
- **竞争力评分**: 所有能力条目总数的对数缩放，结合证书含金量关键词加成 (如 PMP/N1/法考 等)。

## 职业报告结构

AI 生成的结构化 JSON 报告包含四个主要部分：

```json
{
  "studentProfile": {
    "professionalSkills": ["Java", "Spring Boot"],
    "certificates": ["CET-6"],
    "innovationAbilities": [],
    "learningAbilities": [],
    "stressResistance": [],
    "communicationSkills": [],
    "internshipAbilities": [],
    "completenessScore": 85.0,
    "competitivenessScore": 78.0,
    "overallAssessment": "综合评价..."
  },
  "jobMatchAnalysis": {
    "matchedJobs": [
      {
        "jobName": "Java",
        "matchScore": 82.5,
        "dimensions": {
          "baseScore": 80,
          "skillScore": 85,
          "qualityScore": 78,
          "potentialScore": 90
        },
        "weightedTotalScore": 83.2,
        "strengths": [],
        "gaps": []
      }
    ],
    "overallMatchScore": 83.2,
    "gapAnalysis": "差距分析..."
  },
  "careerPathPlan": {
    "careerGoal": "职业目标",
    "industryTrend": "行业趋势分析",
    "careerPath": "初级开发 -> 高级开发 -> 架构师",
    "keyMilestones": [],
    "requiredSkills": []
  },
  "actionPlan": {
    "stages": [
      {"stageName": "短期（1-2年）", "description": "...", "actions": []}
    ],
    "evaluationMetrics": "评估指标",
    "evaluationCycle": "每半年一次",
    "recommendedActions": []
  }
}
```

## 项目结构

```
career-planning-agent/
├── pom.xml
├── README.md
├── frontend/                              # Vue 3 SPA (v2.0)
│   ├── index.html
│   ├── package.json
│   ├── vite.config.js                     # Proxy 配置 -> :8080
│   └── src/
│       ├── main.js
│       ├── App.vue
│       ├── router/index.js                # 路由 + 导航守卫
│       ├── stores/
│       │   ├── auth.js                    # JWT Token + 用户信息
│       │   └── analysis.js                # SSE 流式状态 + 报告缓存
│       ├── api/
│       │   ├── client.js                  # Axios 实例 + JWT 拦截器
│       │   ├── auth.js                    # 登录/注册 API
│       │   ├── jobs.js                    # 岗位画像/关系 API
│       │   ├── graph.js                   # 图谱 API
│       │   ├── analysis.js               # 分析/上传 API
│       │   └── export.js                 # PDF/Word 导出 API
│       ├── composables/
│       │   └── useSSE.js                  # EventSource 封装
│       ├── views/
│       │   ├── LoginView.vue
│       │   ├── RegisterView.vue
│       │   └── DashboardView.vue          # 3-Tab 仪表盘
│       ├── components/
│       │   ├── layout/AppTopbar.vue
│       │   ├── jobs/JobList.vue, JobCard.vue, JobDetailDrawer.vue
│       │   ├── analysis/AnalysisPanel.vue, StreamProgress.vue,
│       │   │          StudentProfile.vue, MatchRadar.vue,
│       │   │          MatchTable.vue, CareerPath.vue, ActionPlan.vue
│       │   ├── graph/GraphPanel.vue
│       │   └── common/KpiCard.vue
│       └── styles/variables.css
└── src/main/
    ├── java/com/mssj/
    │   ├── CareerPlanningAgentApplication.java
    │   ├── config/WebMvcConfig.java
    │   ├── controller/
    │   │   ├── LoginController.java, RegisterController.java
    │   │   ├── StuAnalysisController.java
    │   │   ├── StuFileController.java
    │   │   ├── StuAnalysisStreamController.java    # v2.0 SSE 流式
    │   │   ├── JobProfileController.java
    │   │   ├── JobRelationController.java
    │   │   └── JobGraphController.java              # v2.0 图谱 API
    │   ├── filter/JwtAuthenticationFilter.java
    │   ├── interceptor/JwtInterceptor.java
    │   ├── mapper/
    │   │   ├── UserMapper.java, JobMapper.java, JobRelationMapper.java
    │   ├── pojo/
    │   │   ├── User.java, Result.java
    │   │   ├── JobProfile.java, JobProfileSample.java
    │   │   ├── JobRelation.java, JobRelationPath.java
    │   │   ├── CareerReport.java, MatchDimension.java
    │   │   └── GraphResponse.java
    │   ├── service/
    │   │   ├── UserService.java, AnalysisService.java
    │   │   ├── ScoringEngine.java                   # v2.0 评分引擎
    │   │   ├── JobProfileService.java, JobRelationService.java
    │   │   ├── GraphService.java                    # v2.0 图谱服务
    │   │   └── StudentInputService.java
    │   ├── service/impl/
    │   │   ├── UserServiceImpl.java
    │   │   ├── AnalysisServiceImpl.java             # v2.0 四阶段 Prompt
    │   │   ├── JobProfileServiceImpl.java
    │   │   ├── JobRelationServiceImpl.java
    │   │   ├── GraphServiceImpl.java                # v2.0 Recursive CTE
    │   │   └── StudentInputServiceImpl.java
    │   └── utils/JwtUtils.java
    └── resources/
        ├── application.yml
        ├── schema.sql                              # v2.0 DDL
        ├── mapper/JobRelationMapper.xml             # v2.0 Recursive CTE
        ├── static/                                  # 旧版静态页面
        └── data/
            ├── migration_v2.sql                    # v2.0 数据库迁移
            ├── user.sql, job_profile.sql, job_relation.sql
            └── *.csv
```

## 快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+
- Maven 3.8+
- Node.js 18+ 和 npm
- DeepSeek API Key

### 后端启动

1. 配置数据库连接，编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/career_db
    username: root
    password: your_password
```

2. 创建数据库并导入种子数据：

```sql
CREATE DATABASE IF NOT EXISTS career_db DEFAULT CHARACTER SET utf8mb4;
-- 数据表由 JPA ddl-auto:update 自动管理
-- 可选执行: src/main/resources/schema.sql (DDL)
-- 种子数据: src/main/resources/data/job_profile.sql 和 job_relation.sql
-- v2.0 迁移: src/main/resources/data/migration_v2.sql
```

3. 设置环境变量并启动：

```bash
export DEEPSEEK_API_KEY=your_key_here
mvn spring-boot:run
```

后端启动在 `http://localhost:8080`。

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端启动在 `http://localhost:5173`，API 请求通过 Vite proxy 转发到 `localhost:8080`。

### 生产构建

```bash
# 前端生产构建 -> frontend/dist/
cd frontend && npm run build

# 将 dist 文件复制到 Spring Boot 静态资源目录
# 或配置 Spring Boot 直接 serve frontend/dist/
```

## v2.0 更新日志

与初始版本相比的主要变更：

- **架构升级**: 将单一巨型 Prompt 拆分为 4 个独立阶段，每个阶段使用结构化 JSON 输出，并加入重试机制 (最多 3 次，指数退避)。
- **评分引擎**: 用确定性的 `ScoringEngine` 替代 LLM 生成的评分，实现完整度评分、竞争力评分和四维加权匹配总分计算，评分结果可解释可复现。
- **数据库改动**: `job_profile` 表新增四个权重字段 (`w_base`, `w_skill`, `w_quality`, `w_potential`)；修复 `job_relation` 表 `matching_score` 类型错误 (String -> INT)；新增完整 DDL (`schema.sql`) 和迁移脚本 (`migration_v2.sql`)。
- **图谱查询**: 用 MyBatis XML Recursive CTE 替代简单的 `SELECT *` 查询，实现多级岗位路径深度遍历和防环检测；新增 `/job-graph/full` 和 `/job-graph/from/{jobName}` 接口。
- **流式响应**: 新增 SSE (`SseEmitter`) 接口 `/stu/analysis/stream`，将报告生成过程分阶段实时推送到前端。
- **前端重构**: 从 3724 行单文件静态 HTML 重构为 Vue 3 SPA，组件化架构，Pinia 状态管理，Vue Router 路由，完整对接 v2.0 API (四维评分表格、双雷达图、SSE 流式进度、递归图谱可视化)。
- **导出支持**: 新增 poi-tl 和 iText 依赖，预留 Word/PDF 服务端导出接口桩。
