# AI职业规划系统 (AI Career Planning System )

基于 Spring Boot + DeepSeek AI 构建的智能职业规划助手，帮助大学生分析简历、匹配合适岗位并生成个性化职业发展报告。

## 项目简介

本项目是一个全栈 Web 应用，利用 AI 大模型提供职业规划服务。学生可上传简历（PDF/TXT）或输入文本，系统通过 DeepSeek 大语言模型分析其技能，与数据库中的岗位画像进行匹配，生成全面的职业报告，涵盖：

- **学生画像评估** — 专业技能、证书、创新能力、竞争力评分
- **岗位匹配分析** — 多岗位匹配度、优势与差距
- **职业路径规划** — 职业目标、行业趋势、关键里程碑
- **行动计划** — 短/中/长期可执行步骤与评估指标

## 技术栈

| 层级 | 技术 |
|------|------|
| **后端** | Java 17, Spring Boot 3.5, Spring AI |
| **AI 模型** | DeepSeek Chat（Spring AI DeepSeek starter） |
| **数据库** | MySQL, MyBatis, JPA |
| **安全认证** | JWT（jjwt），自定义 Filter + Interceptor |
| **文件处理** | Apache PDFBox（PDF 解析）, OpenCSV |
| **前端** | HTML/CSS/JS（ECharts 可视化, jsPDF 报告导出） |
| **构建** | Maven |

## 功能特性

### 🔐 用户系统
- 用户注册（用户名唯一性校验、密码强度校验）
- 登录/登出，JWT Token 认证（24 小时有效期）
- Token 刷新机制
- 双重安全防护：Filter（`OncePerRequestFilter`）+ Interceptor（`HandlerInterceptor`）

### 📄 简历上传
- 支持 PDF / TXT 文件上传（单文件最大 10MB，单次请求最大 100MB）
- 支持直接输入文本作为简历内容
- 按用户隔离文件存储目录
- UUID 文件名防止冲突

### 🤖 AI 职业分析
- 通过 PDFBox 提取 PDF 文本，支持 TXT 文件
- 合并多个上传文件进行综合分析
- 将学生画像与数据库中岗位画像进行智能匹配
- 结合岗位关系图谱（晋升路径、职业转换）
- 生成结构化 JSON 报告，包含评分、匹配度、规划建议

### 📊 岗位数据管理
- 岗位画像数据库：技能要求、证书、创新能力、学习能力等维度
- 岗位关系图谱：展示职业发展路径（晋升/转换）
- RESTful API 查询岗位画像及关系

### 📈 前端仪表板
- 可折叠侧边栏导航
- ECharts 可视化图表（雷达图、仪表盘、柱状图）
- 职业报告 PDF 导出（jsPDF + html2canvas）
- AI 对话交互界面
- 毛玻璃卡片设计 + 深色侧边栏主题

## 系统架构

```
┌─────────────────────────────────────────────────────┐
│                   前端（静态 HTML）                    │
│   index.html（仪表板 + ECharts + jsPDF）              │
│   login.html / register.html                         │
│   privacy_policy.html / user_agreement.html          │
└───────────────────────┬─────────────────────────────┘
                        │ HTTP / REST
┌───────────────────────┴─────────────────────────────┐
│              JWT Filter + Interceptor                 │
│         (com.mssj.filter / .interceptor)             │
└───────────────────────┬─────────────────────────────┘
                        │
┌───────────────────────┴─────────────────────────────┐
│                   控制器层                              │
│  ┌──────────┐ ┌──────────┐ ┌──────────────────┐    │
│  │  认证    │ │  学生    │ │  岗位画像/关系    │    │
│  │(登录/注册)│ │(分析/上传)│ │                  │    │
│  └──────────┘ └──────────┘ └──────────────────┘    │
└───────────────────────┬─────────────────────────────┘
                        │
┌───────────────────────┴─────────────────────────────┐
│                   服务层                                │
│  UserService  AnalysisService  JobProfileService     │
│  StudentInputService  JobRelationService             │
└───────────────────────┬─────────────────────────────┘
                        │
┌───────────────────────┴─────────────────────────────┐
│            MyBatis Mappers + MySQL 数据库              │
│  user / job_profile / job_relation 数据表           │
└───────────────────────┬─────────────────────────────┘
                        │
┌───────────────────────┴─────────────────────────────┐
│              DeepSeek AI（Spring AI）                  │
│              大模型生成职业规划报告                      │
└─────────────────────────────────────────────────────┘
```

## API 接口

### 认证相关（`/auth/**`）
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/auth/login` | 用户登录（返回 JWT） |
| POST | `/auth/register` | 用户注册 |
| POST | `/auth/logout` | 用户登出 |
| POST | `/auth/refresh` | 刷新 JWT Token |
| GET | `/auth/info` | 获取当前用户信息 |
| GET | `/auth/username` | 获取当前用户名 |
| GET | `/auth/check-username` | 检查用户名是否可用 |

### 学生相关（`/stu/**`）
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/stu/uploadResume` | 上传简历文件（PDF/TXT） |
| POST | `/stu/uploadText` | 上传简历文本内容 |
| POST | `/stu/analysis` | 触发 AI 分析（生成职业报告） |

### 岗位画像（`/job-profile/**`）
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/job-profile` | 获取所有岗位画像（简要） |
| GET | `/job-profile/{id}` | 获取单个岗位详细画像 |

### 岗位关系（`/job-relation/**`）
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/job-relation` | 获取所有岗位关系图谱 |
| GET | `/job-relation/{jobId}` | 获取指定岗位的关系 |

## 项目结构

```
src/main/java/com/mssj/
├── CareerPlanningAgentApplication.java   # Spring Boot 启动类
├── config/
│   └── WebMvcConfig.java                 # 拦截器与过滤器注册
├── controller/
│   ├── LoginController.java              # 登录/登出/刷新 Token
│   ├── RegisterController.java           # 用户注册
│   ├── StuAnalysisController.java        # AI 分析触发
│   ├── StuFileController.java            # 文件/文本上传
│   ├── JobProfileController.java         # 岗位画像查询
│   └── JobRelationController.java        # 岗位关系查询
├── filter/
│   └── JwtAuthenticationFilter.java      # JWT 认证过滤器
├── interceptor/
│   └── JwtInterceptor.java               # JWT 认证拦截器
├── mapper/
│   ├── UserMapper.java                   # 用户 CRUD（MyBatis）
│   ├── JobMapper.java                    # 岗位画像 + 关系查询
│   └── JobRelationMapper.java            # 岗位关系查询
├── pojo/
│   ├── User.java                         # 用户实体
│   ├── Result.java                       # 统一 API 响应封装
│   ├── JobProfile.java                   # 岗位画像实体
│   ├── JobProfileSample.java             # 岗位摘要实体
│   ├── JobRelation.java                  # 岗位关系实体
│   └── CareerReport.java                 # AI 报告数据模型（嵌套类）
├── service/
│   ├── UserService.java
│   ├── AnalysisService.java
│   ├── JobProfileService.java
│   ├── JobRelationService.java
│   └── StudentInputService.java
├── service/impl/
│   ├── UserServiceImpl.java
│   ├── AnalysisServiceImpl.java          # AI 分析核心逻辑
│   ├── JobProfileServiceImpl.java
│   ├── JobRelationServiceImpl.java
│   └── StudentInputServiceImpl.java
└── utils/
    └── JwtUtils.java                     # JWT 生成与验证工具

src/main/resources/
├── application.yml                       # 项目配置（数据库、AI、文件上传等）
├── static/
│   ├── index.html                        # 主仪表板（ECharts 图表、AI 对话）
│   ├── login.html                        # 登录页
│   ├── register.html                     # 注册页
│   ├── privacy_policy.html               # 隐私政策
│   └── user_agreement.html               # 用户协议
└── data/
    └── job_original_data.csv             # 岗位原始数据
```

## 快速开始

### 环境要求
- JDK 17+
- MySQL 8.0+
- Maven
- DeepSeek API Key

### 配置

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/career_db
    username: root
    password: your_password
  ai:
    deepseek:
      api-key: ${DEEPSEEK_API_KEY}   # 通过环境变量设置
```

### 数据库

数据表（`user`、`job_profile`、`job_relation`）由 JPA 自动管理。岗位数据可从 `src/main/resources/data/job_original_data.csv` 导入。

### 启动

```bash
export DEEPSEEK_API_KEY=your_key_here
./mvnw spring-boot:run
```

访问 `http://localhost:8080` 即可使用。

## 职业报告结构

AI 生成的结构化 JSON 报告包含四个主要部分：

```json
{
  "studentProfile": {
    "professionalSkills": ["Java", "Spring Boot", "..."],
    "certificates": ["CET-6", "..."],
    "innovationAbilities": ["..."],
    "learningAbilities": ["..."],
    "stressResistance": ["..."],
    "communicationSkills": ["..."],
    "internshipAbilities": ["..."],
    "completenessScore": 85.0,
    "competitivenessScore": 78.0,
    "overallAssessment": "综合评价..."
  },
  "jobMatchAnalysis": {
    "matchedJobs": [
      {
        "jobName": "Java后端开发",
        "matchScore": 88.5,
        "strengths": ["..."],
        "gaps": ["..."]
      }
    ],
    "overallMatchScore": 82.0,
    "gapAnalysis": "差距分析..."
  },
  "careerPathPlan": {
    "careerGoal": "职业目标",
    "industryTrend": "行业趋势分析",
    "careerPath": "初级 → 高级 → 架构师",
    "keyMilestones": ["..."],
    "requiredSkills": ["需要培养的能力..."]
  },
  "actionPlan": {
    "stages": [
      { "stageName": "短期（1-2年）", "description": "...", "actions": ["..."] }
    ],
    "evaluationMetrics": "评估指标",
    "evaluationCycle": "每半年一次",
    "recommendedActions": ["建议..."]
  }
}
```