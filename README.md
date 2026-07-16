# 教学反馈智能分析平台 (TeachingFeedback)

基于 Spring AI Agent 框架的教学反馈后端平台。

## 项目概述

支持 SSE 流式反馈采集、多轮对话记忆、反馈向量聚类与教学建议自动生成，形成「反馈采集 → 数据分析 → 改进建议」的完整闭环。

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 框架 | Spring Boot | 3.5 |
| AI | Spring AI | 1.1.4 |
| 大模型 | DashScope qwen-max | — |
| 数据库 | MySQL | 8.0 |
| 向量库 | Qdrant | latest |
| 缓存 | Redis | 7-Alpine |
| 构建 | Maven | 3.9+ |
| JDK | Java | 17+ |

## 快速启动

### 前置条件

- Java 17+
- Maven 3.8+
- MySQL 8.0+
- Docker Desktop（运行 Qdrant + Redis）

### 启动步骤

```bash
# 1. 启动中间件
docker compose up -d

# 2. 初始化数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS teaching_feedback DEFAULT CHARSET utf8mb4;"
mysql -u root -p teaching_feedback < sql/sql.sql
mysql -u root -p teaching_feedback < sql/seed_data.sql

# 3. 配置 API Key
# 编辑 src/main/resources/application-local.yml
# 填入你的 DashScope API Key

# 4. 启动项目
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## 项目来源

本项目从 SunnySide（住院陪护管理平台）改造而来，核心技术栈完全复用，业务层已全部重写为教学反馈领域。

## 模块划分

| 模块 | 目录 | 职责 |
|------|------|------|
| 教师端 | controller/Teacher* | 教师登录、AI 聊天 |
| 学生端 | controller/Student* | 学生登录、反馈提交、AI 聊天 |
| AI 工具 | ai/tools/ | Function Calling 工具定义 |
| RAG | ai/rag/ | 知识库灌库与检索 |
| 记忆 | ai/memory/ | 多轮对话记忆 |
| 安全 | security/ | JWT 鉴权 |
| 数据 | pojo/ 下各类 | Entity、DTO、VO |

## 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 教师端 | teacher_zhang | password |
| 教师端 | teacher_li | password |
| 学生端 | stu20260001 | password |
