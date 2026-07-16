---
title: 教学反馈智能分析平台 — 改造设计方案
created: 2026-07-06
status: draft
---

# 教学反馈智能分析平台 设计方案

> 基于 SunnySide（住院陪护管理平台）改造
> 核心技术栈：Spring Boot 3.5 + Spring AI 1.1.4 + Java 17 + qwen-max

---

## 一、改造策略

**原则：只改业务层，不动基础设施层。**

| 层 | 内容 | 改造策略 |
|----|------|---------|
| 基础设施 | SSE、ChatClient、Advisor、RAG灌库、JWT鉴权、Docker | **不动** |
| 业务层 | Entity、Mapper、Service、@Tool、Prompt | **全部重写** |
| 配置层 | application.yml、application-local.yml | 改数据源和业务参数 |

本项目采用**分阶段渐进式改造**，保留可运行状态。

---

## 二、技术栈

| 组件 | 技术选型 | 版本 | 用途 |
|------|---------|------|------|
| 框架 | Spring Boot | 3.5 | 后端基础 |
| AI 框架 | Spring AI | 1.1.4 | Function Calling、RAG、Memory |
| AI 模型 | DashScope qwen-max | — | 大语言模型 |
| 数据库 | MySQL | 8.0 | 业务数据 |
| 向量库 | Qdrant | latest | RAG 知识库 + 反馈向量 |
| 缓存 | Redis | 7-Alpine | 会话缓存 |
| 消息推送 | SSE | — | 流式对话 |
| 安全 | JWT (jjwt) | 0.12.6 | 鉴权 |
| 密码 | BCrypt | — | 密码加密 |

---

## 三、角色体系

### 双账号（复用现有设计）

```
教师端（原家属端）
  ├── 登录注册：POST /teacher/login, /teacher/register
  ├── 聊天：GET /teacher/chat（SSE 流式）
  ├── 可查询：名下所有课程 + 课程反馈 + 分析
  └── 需指定 courseId（对应原 patientId）

学生端（原患者端）
  ├── 登录注册：POST /student/login, /student/register
  ├── 聊天：GET /student/chat（SSE 流式）
  ├── 可查询：本人的反馈记录、课程信息
  └── 身份自动解析，不需传 courseId
```

### 数据隔离

- 教师只能看自己课程的反馈
- 学生只能看/提交自己的反馈
- 工具方法通过 JWT 自动解析身份，防止越权

---

## 四、数据库设计

### 4.1 核心表概览（11 张 → 8 张）

| 表名 | 说明 | 来源 |
|------|------|------|
| **department** | 院系（原 hospital_department） | 改造 |
| **course** | 课程（原 patient） | 新建 |
| **teacher** | 教师（原 relative_user） | 改造 |
| **student** | 学生（原 patient 的身份部分） | 新建 |
| **enrollment** | 选课关系（原 relative_patient_relation） | 改造 |
| **feedback** | 学生反馈记录（原 treatment_plan） | 新建 |
| **suggestion** | 教学改进建议（原 dietary_advice） | 新建 |
| **faq** | 常见问答 | 改内容 |

**去掉的表：** vital_signs、medical_team_duty、nearby_facility、hospital_announcement

### 4.2 department（院系）

```sql
CREATE TABLE department (
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    dept_name     VARCHAR(128) NOT NULL COMMENT '院系名称',
    contact_phone VARCHAR(32)  NULL COMMENT '办公电话',
    location      VARCHAR(255) NULL COMMENT '办公地点',
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL
) COMMENT '院系信息表';
```

### 4.3 course（课程）

```sql
CREATE TABLE course (
    id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    dept_id        BIGINT UNSIGNED NOT NULL COMMENT '所属院系ID',
    course_name    VARCHAR(128)    NOT NULL COMMENT '课程名称',
    course_code    VARCHAR(64)     NOT NULL UNIQUE COMMENT '课程编号',
    teacher_name   VARCHAR(64)     NOT NULL COMMENT '授课教师姓名',
    semester       VARCHAR(32)     NOT NULL COMMENT '学期（如 2026春）',
    student_count  INT             DEFAULT 0 COMMENT '选课人数',
    status         ENUM('ACTIVE', 'ENDED') DEFAULT 'ACTIVE' NOT NULL COMMENT '课程状态',
    username       VARCHAR(64)      NULL COMMENT '课程账号（用于学生端登录）',
    password       VARCHAR(128)     NULL COMMENT '账号密码（BCrypt）',
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (dept_id) REFERENCES department(id) ON DELETE CASCADE
) COMMENT '课程信息表';
```

### 4.4 teacher（教师）

```sql
CREATE TABLE teacher (
    id         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(64)  NOT NULL UNIQUE COMMENT '登录账号',
    password   VARCHAR(128) NOT NULL COMMENT '登录密码（BCrypt）',
    full_name  VARCHAR(64)  NOT NULL COMMENT '姓名',
    phone      VARCHAR(32)  NOT NULL COMMENT '手机号',
    dept_id    BIGINT UNSIGNED NULL COMMENT '所属院系',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL
) COMMENT '教师信息表';
```

### 4.5 student（学生）

```sql
CREATE TABLE student (
    id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    student_no   VARCHAR(64)  NOT NULL UNIQUE COMMENT '学号',
    full_name    VARCHAR(64)  NOT NULL COMMENT '姓名',
    username     VARCHAR(64)  NULL UNIQUE COMMENT '登录账号',
    password     VARCHAR(128) NULL COMMENT '登录密码（BCrypt）',
    dept_id      BIGINT UNSIGNED NULL COMMENT '所属院系',
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL
) COMMENT '学生信息表';
```

### 4.6 enrollment（选课关系）

```sql
CREATE TABLE enrollment (
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    student_id  BIGINT UNSIGNED NOT NULL COMMENT '学生ID',
    course_id   BIGINT UNSIGNED NOT NULL COMMENT '课程ID',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE KEY uk_student_course (student_id, course_id),
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE
) COMMENT '选课关系表';
```

### 4.7 feedback（反馈记录）

```sql
CREATE TABLE feedback (
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    course_id     BIGINT UNSIGNED NOT NULL COMMENT '课程ID',
    student_id    BIGINT UNSIGNED NOT NULL COMMENT '学生ID',
    feedback_date DATE           NOT NULL COMMENT '反馈日期',
    dimension     VARCHAR(64)    NOT NULL COMMENT '反馈维度（如 KNOWLEDGE / PACE / INTERACTION / CLARITY）',
    score         TINYINT UNSIGNED NOT NULL COMMENT '评分（1-5）',
    comment       TEXT           NULL COMMENT '文字反馈',
    is_analyzed   TINYINT(1) DEFAULT 0 NOT NULL COMMENT '是否已纳入聚类分析',
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE
) COMMENT '学生反馈记录表';

CREATE INDEX idx_feedback_course_date ON feedback (course_id, feedback_date);
```

反馈维度（`dimension`）枚举：

| 维度 | 说明 | 示例问题 |
|------|------|---------|
| KNOWLEDGE | 知识点掌握度 | "这节课的知识点你听懂了吗？" |
| PACE | 教学节奏 | "教学进度是否合适？" |
| INTERACTION | 课堂互动性 | "你有机会提问或参与讨论吗？" |
| CLARITY | 讲解清晰度 | "老师对难点的解释是否清楚？" |
| ENGAGEMENT | 学习投入度 | "你对这节课的注意力集中程度？" |
| MATERIAL | 教学资料 | "课件和参考资料对你有帮助吗？" |

### 4.8 suggestion（教学改进建议）

```sql
CREATE TABLE suggestion (
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    course_id     BIGINT UNSIGNED NOT NULL COMMENT '课程ID',
    suggestion_date DATE         NOT NULL COMMENT '建议生成日期',
    dimension     VARCHAR(64)    NOT NULL COMMENT '对应反馈维度',
    content       TEXT           NOT NULL COMMENT '改进建议内容',
    priority      ENUM('HIGH', 'MEDIUM', 'LOW') DEFAULT 'MEDIUM' NOT NULL,
    generated_by  ENUM('AI', 'TEACHER') DEFAULT 'AI' NOT NULL COMMENT '来源',
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE
) COMMENT '教学改进建议表';
```

---

## 五、API 接口设计

### 教师端

| 方法 | 路径 | 说明 | JWT |
|------|------|------|-----|
| POST | `/teacher/login` | 登录 | ❌ |
| POST | `/teacher/register` | 注册 | ❌ |
| GET | `/teacher/chat` | AI 聊天（SSE 流式） | ✅ |
| DELETE | `/teacher/chat/memory` | 删除对话记忆 | ✅ |

### 学生端

| 方法 | 路径 | 说明 | JWT |
|------|------|------|-----|
| POST | `/student/login` | 登录 | ❌ |
| POST | `/student/register` | 注册（需验证学号） | ❌ |
| POST | `/student/feedback` | 提交反馈（JSON） | ✅ |
| GET | `/student/chat` | AI 聊天（SSE 流式） | ✅ |
| DELETE | `/student/chat/memory` | 删除对话记忆 | ✅ |

---

## 六、Function Calling 工具设计

### 教师端工具（15 个）

```java
@Tool(description = "根据 JWT 解析当前教师身份，返回教师ID、姓名、
                    所授课程列表 courseIds。用于对话开始时自动确认身份。无参数。")
public String getCurrentTeacherContext() { ... }

@Tool(description = "查询指定课程的基础信息：课程名称、编号、学期、
                    授课教师、选课人数。参数 courseId：课程主键 ID。")
public String queryCourseInfo(String courseId) { ... }

@Tool(description = "按课程和日期查询学生反馈汇总：各维度平均分、
                    反馈数量、文字反馈列表。参数 courseId；feedbackDate：
                    YYYY-MM-DD，缺省为当日。")
public String queryFeedbackByDate(String courseId, String feedbackDate) { ... }

@Tool(description = "查询指定课程在日期范围内的反馈趋势：按日期返回
                    各维度的评分变化。参数 courseId；startDate；endDate。")
public String queryFeedbackTrend(String courseId, String startDate, String endDate) { ... }

@Tool(description = "对某门课程的反馈进行知识点聚类分析：识别学生对各
                    知识点的掌握程度，返回掌握率、薄弱环节列表。
                    参数 courseId。")
public String analyzeKnowledgeClustering(String courseId) { ... }

@Tool(description = "根据反馈数据自动生成教学改进建议：针对薄弱维度
                    生成3-5条可操作建议。参数 courseId。")
public String generateSuggestions(String courseId) { ... }

@Tool(description = "查询某门课程的历史改进建议列表。参数 courseId；
                    dateSince：可选，YYYY-MM-DD。")
public String querySuggestionsHistory(String courseId, String dateSince) { ... }

@Tool(description = "查询所有课程的列表（当前教师名下）。无参数。")
public String listMyCourses() { ... }

@Tool(description = "对比分析两门或多门课程的反馈数据：比较各维度评分
                    差异，识别优势与不足。参数 courseIds：逗号分隔的ID列表。")
public String compareCourses(String courseIds) { ... }

@Tool(description = "查询院系信息。参数 deptId。")
public String queryDepartmentInfo(String deptId) { ... }

// 以下为「综合分析」工具

@Tool(description = "汇总某门课程当前学期的整体教学质量报告：包含
                    反馈统计、知识点分析、改进建议、趋势。参数 courseId。")
public String generateCourseReport(String courseId) { ... }

@Tool(description = "查询某门课程的常见问答。参数 category：可选筛选分类。")
public String queryFAQ(String category) { ... }

@Tool(description = "识别反馈中的异常模式：某维度评分骤降、某知识点
                    大面积不理解等。参数 courseId。")
public String detectAnomalies(String courseId) { ... }

@Tool(description = "查询过去N周内反馈参与率变化。参数 courseId；weeks：正整数，缺省4。")
public String queryParticipationTrend(String courseId, String weeks) { ... }

@Tool(description = "按关键字搜索历史反馈内容。参数 courseId；keyword：搜索词。")
public String searchFeedbackContent(String courseId, String keyword) { ... }
```

### 学生端工具（6 个）

```java
@Tool(description = "根据 JWT 解析当前学生身份，返回姓名、已选课程列表。无参数。")
public String getCurrentStudentContext() { ... }

@Tool(description = "查询本人的历史反馈记录。参数 courseId：可选筛选课程。")
public String queryMyFeedbackHistory(String courseId) { ... }

@Tool(description = "查询本人所在课程的信息。参数 courseId。")
public String queryMyCourseInfo(String courseId) { ... }

@Tool(description = "提交一条新的课程反馈。参数 courseId；dimension；score；comment。")
public String submitFeedback(String courseId, String dimension, String score, String comment) { ... }

@Tool(description = "查询本人已提交反馈的统计摘要：各课程各维度的平均分。无参数。")
public String queryMyFeedbackSummary() { ... }

@Tool(description = "查询院系信息。无参数（自动关联本人所在院系）。")
public String queryMyDepartmentInfo() { ... }
```

---

## 七、RAG 知识库设计

### 文件名：`RAG.txt`

内容方向：
- 反馈维度定义和评分标准
- 教学改进建议模板库
- 各维度典型问题与改进策略
- 教学反馈分析专业知识

示例内容：

```
教学反馈分析知识库

一、反馈维度定义
1. KNOWLEDGE（知识点掌握度）：评估学生对本次课程知识点的理解程度。
   1分：完全没听懂；5分：完全掌握并能够运用。
   改进方向：增加例题讲解、放慢难点节奏、补充前置知识。

2. PACE（教学节奏）：评估教学进度是否合适。
   1分：太快跟不上；5分：节奏恰到好处。
   改进方向：增加课堂停顿和互动、设置答疑环节。

3. INTERACTION（课堂互动性）：评估学生参与程度。
   1分：完全没有互动；5分：充分参与讨论。
   改进方向：增加提问环节、小组讨论、实时投票。

4. CLARITY（讲解清晰度）：评估老师对概念的阐述能力。
   1分：含糊不清；5分：清晰易懂。
   改进方向：多用类比和图示、课前预习材料准备。

5. ENGAGEMENT（学习投入度）：评估学生注意力集中程度。
   1分：完全无法集中；5分：全程投入。
   改进方向：增加案例教学、交替不同教学形式。

6. MATERIAL（教学资料）：评估课件和参考资料质量。
   1分：无任何资料；5分：资料完备且有用。
   改进方向：提前发放课件、补充参考阅读。

二、改进建议模板

针对 KNOWLEDGE 维度低分（≤2分）：
  "建议在第X节课程结束后设置5分钟快速测验，
   实时评估学生掌握情况；对薄弱知识点安排
   专题辅导或录制微课供复习。"

针对 PACE 维度低分（≤2分）：
  "建议在讲授复杂概念时增加停顿，每15分钟
   设置一个互动环节（提问/投票），让学生
   有消化时间。"

针对 INTERACTION 维度低分（≤2分）：
  "建议引入课堂实时投票或弹幕系统，增加
   '每课一问'环节，鼓励学生在课堂上提问。"
```

---

## 八、对话上下文构建

复用 `MedicalSystemPromptTemplate` 的机制，只改内容：

```
教师端发消息时，注入的上下文：

【会话上下文】
teacherId: 3
服务端当日: 2026-07-02

【关联课程】
1) courseId=5 课程名=数据结构 学期=2026春 选课人数=86
   deptId=2 院系=计算机学院

【工具参数】当前账号下有1门课程，凡工具要求 courseId，
必须传入字符串 "5"（与上列课程ID一致）。禁止留空或编造。

【用户消息】
这学期数据结构课的学生反馈怎么样？


学生端发消息时，注入的上下文：

【会话上下文】
studentId: 12
服务端当日: 2026-07-02

【已选课程】
1) courseId=5 课程名=数据结构 授课教师=张教授
2) courseId=8 课程名=操作系统 授课教师=李教授

【用户消息】
我数据结构的反馈提交了吗？
```

---

## 九、改造步骤（分阶段可运行）

### 阶段 1：数据库迁移（1 天）

1. 创建新分支 `feature/teaching-feedback`
2. 编写 DDL（新建 8 张表）
3. 编写 Seed 测试数据（2 门课程、3 位教师、10 名学生、50 条反馈）
4. 执行建表 + 导入

### 阶段 2：Entity + Mapper 层（1 天）

1. 创建 Course、Teacher、Student、Feedback、Suggestion、Enrollment 的 Entity 类
2. 创建对应的 Mapper 接口 + XML
3. 删除不再需要的 Entity/Mapper

### 阶段 3：Service + InpatientAiDataService（1 天）

1. 重写各 Service 的业务逻辑
2. 重写 `InpatientAiDataServiceImpl` 为 `TeachingAiDataServiceImpl`
3. 实现反馈统计、聚类分析、趋势计算

### 阶段 4：AI 工具 + Prompt（1 天）

1. 重写 `AITool` 为 `TeacherAITool`（15 个工具）
2. 重写 `PatientAITool` 为 `StudentAITool`（6 个工具）
3. 改写 Prompt 模板（`teacher-system.st`、`student-system.st`）
4. 替换 RAG 知识库内容

### 阶段 5：Controller 层 + 测试（1 天）

1. 重写 Controller（`/teacher/**`、`/student/**`）
2. 更新 JWT 放行路径
3. 端到端测试

---

## 十、复用清单（不需要改的文件）

```
pom.xml                           ← 依赖完全不变
docker-compose.yml                ← Docker 服务不变
application.yml (主配置)           ← 数据源配置不改
application-local.yml             ← 仅改 api-key（已有）

TeachingFeedbackApplication.java   ← 主入口已改名

security/
  ├── JwtUtil.java                ← JWT 逻辑不改
  ├── JwtInterceptor.java         ← 拦截逻辑不改
  └── WebConfig.java              ← 仅改放行路径（替换路径名）

ai/
  ├── client/AIChat.java          ← ChatClient 调用逻辑不改
  ├── client/PatientAIChat.java   ← 改名但结构不改
  ├── prompt/MedicalSystemPromptTemplate.java  ← 改内容不改结构
  ├── memory/InMemoryChatMemoryConfiguration.java  ← 不改
  ├── rag/RagModularAdvisorConfig.java  ← 不改
  └── rag/RagIngestService.java   ← 不改（RAG.txt 内容改）

config/
  ├── CorsConfig.java             ← 不改
  └── CorsProperties.java         ← 不改

common/Result.java                ← 不改
```

---

## 十一、文件命名变更对照

```
src/main/java/com/example/project/
  ├── controller/
  │   ├── relativeController.java     → TeacherController.java
  │   ├── aiController.java           → TeacherChatController.java
  │   ├── PatientController.java      → StudentController.java
  │   ├── PatientChatController.java  → StudentChatController.java
  │   └── BindPatientController.java  → 去掉
  │
  ├── ai/tools/
  │   ├── AITool.java                 → TeacherAITool.java
  │   └── PatientAITool.java          → StudentAITool.java
  │
  ├── ai/client/
  │   ├── AIChat.java                 → TeacherAIChat.java（或保留原名）
  │   └── PatientAIChat.java          → StudentAIChat.java
  │
  ├── ai/prompt/
  │   ├── MedicalSystemPromptTemplate.java → TeachingPromptTemplate.java
  │   ├── MedicalChatSystemPrompt.java     → TeacherChatSystemPrompt.java
  │   └── PatientChatSystemPrompt.java     → StudentChatSystemPrompt.java
  │
  ├── service/
  │   ├── relativeService.java        → TeacherService.java
  │   ├── PatientAuthService.java     → StudentAuthService.java
  │   └── InpatientAiDataService.java → TeachingAiDataService.java
  │
  └── pojo/
      ├── entity/*                    → Course, Teacher, Student, Feedback...
      ├── dto/*                       → LoginData, FeedbackSubmitRequest...
      └── vo/*                        → CourseInfoVo, FeedbackSummaryVo...

resources/
  ├── prompt/medical-system.st        → teacher-system.st
  ├── prompt/patient-system.st        → student-system.st
  ├── mapper/*                        → 对应新表名
  └── rag/RAG.txt                     → 替换内容
```

---

## 十二、测试数据建议

### 院系

```sql
INSERT INTO department (id, dept_name, contact_phone, location) VALUES
(1, '计算机学院', '010-88880001', '教学楼A座3层'),
(2, '数学学院',  '010-88880002', '教学楼B座2层'),
(3, '外国语学院','010-88880003', '教学楼C座5层');
```

### 教师

```sql
INSERT INTO teacher (username, password, full_name, phone, dept_id) VALUES
('teacher_zhang', '$2a$10$...', '张明', '13800000001', 1),
('teacher_li',    '$2a$10$...', '李华', '13800000002', 1),
('teacher_wang',  '$2a$10$...', '王芳', '13800000003', 2);
```

### 课程

```sql
INSERT INTO course (dept_id, course_name, course_code, teacher_name, semester, student_count) VALUES
(1, '数据结构', 'CS201', '张明', '2026春', 86),
(1, '操作系统', 'CS301', '李华', '2026春', 72),
(2, '高等数学', 'MATH101', '王芳', '2026春', 120);
```

### 注册账号

```
教师端登录：teacher_zhang / password
学生端注册：学号 STU20260001，设置用户名密码
```

---

## 十三、面试亮点关键词

```
Spring AI Agent 框架
Function Calling 工具调用（15个教学分析工具）
RAG 检索增强生成（Qdrant 向量库）
SSE 流式多轮对话
MessageWindowChatMemory 窗口记忆 + JDBC 持久化
JWT 双账号体系 + RequestContextHolder 线程身份解析
反馈聚类分析 + 自动改进建议生成
```
