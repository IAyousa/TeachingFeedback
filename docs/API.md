# TeachingFeedback 后端 HTTP 接口说明

面向前端对接。内容依据当前 Spring Boot 控制器与配置整理；默认 **无** `context-path`，端口以 `application.yml` 中 `server.port` 为准（常见为 `8080`）。

系统中有两类终端用户：**教师端**（`/teacher/**` 路径）与 **学生端**（`/student/**` 路径）。二者均使用 `Authorization: Bearer <JWT>`，JWT 的 subject 均为**用户名**字符串，但业务数据隔离，请使用对应角色的登录接口获取 Token，并调用对应功能模块。

---

## 1. 基础约定

| 项 | 说明 |
|----|------|
| Base URL | `http://<host>:<port>`，例如 `http://localhost:8080` |
| 统一 JSON 封装 | 多数接口返回 `Result<T>`：`{ "code": number, "message": string, "data": T \| null }`。业务成功时 `code` 一般为 **200** |
| 鉴权 | 除下方 **JWT 白名单** 中的路径外，请求需带：`Authorization: Bearer <JWT>` |
| 预检 | `OPTIONS` 请求由拦截器直接放行 |
| 跨域 CORS | 后端已配置全局 CORS，允许的来源、是否带 Cookie 等见 `application.yml` 中 `app.cors`（默认放行本机 `localhost` / `127.0.0.1` 任意端口） |

**JWT 白名单（不校验 Token）：**

- `POST /teacher/login`
- `POST /teacher/register`
- `POST /student/login`
- `POST /student/register`
- Spring 默认 `/error`

**其余路径均需有效 Bearer Token**（含 `/teacher/chat`、`/student/chat` 等）。

---

## 2. 教师端 `/teacher`

### 2.1 登录（无需 Bearer）

| 项 | 值 |
|----|-----|
| 方法 / 路径 | `POST /teacher/login` |
| Content-Type | `application/json` |

**请求体 `AuthRequest`：**

| 字段 | 类型 | 说明 |
|------|------|------|
| username | string | 登录名 |
| password | string | 密码 |

**响应：** `Result<LoginData>`

| 字段（位于 `data`） | 类型 | 说明 |
|---------------------|------|------|
| token | string | JWT（subject 为亲属用户名） |
| username | string | 登录名 |

失败时 `code` 多为 **400**，`message` 含业务说明（如「用户名或密码错误」）。

---

### 2.2 注册（无需 Bearer）

| 项 | 值 |
|----|-----|
| 方法 / 路径 | `POST /relative/register` |
| Content-Type | `application/json` |
| 请求体 | 同 `AuthRequest` |

**响应：** `Result<Void>`，成功时 `data` 常为 `null`。

---

### 2.3 查询账户信息（需 Bearer：亲属 Token）

| 项 | 值 |
|----|-----|
| 方法 / 路径 | `GET /relative/getInfo` |
| Query | `username`（必填） |

**响应：** **纯字符串**（非 `Result` 包装）。

- 成功：内容为 **JSON 字符串**，字段大致包括：`id`、`username`、`full_name`、`phone`、`open_id`、`created_at`、`updated_at`
- 失败：中文提示，如「用户不存在」「用户名不能为空」

---

### 2.4 修改密码（需 Bearer：亲属 Token）

| 项 | 值 |
|----|-----|
| 方法 / 路径 | `POST /relative/updateInfo` |
| Query | `username`、`oldPassword`、`newPassword`（均必填） |

**响应：** 纯字符串，如「密码修改成功」或错误说明。

---

### 2.5 注销账号（需 Bearer：亲属 Token）

| 项 | 值 |
|----|-----|
| 方法 / 路径 | `POST /relative/deleteInfo` |
| Query | `username`、`password` |

**响应：** 纯字符串，如「账号已注销」或错误说明。

---

## 3. 学生端 `/student`

学生通过**学号**匹配已有记录后注册；登录使用学生自有用户名、密码（存于 `student` 表）。

### 3.1 登录（无需 Bearer）

| 项 | 值 |
|----|-----|
| 方法 / 路径 | `POST /student/login` |
| Content-Type | `application/json` |

**请求体：** 同 `AuthRequest`（`username`、`password` 为学生端用户名与密码）。

**响应：** `Result<StudentLoginData>`

| 字段（位于 `data`） | 类型 | 说明 |
|---------------------|------|------|
| token | string | JWT（subject 为学生用户名） |
| username | string | 登录名 |
| studentId | number | 学生主键 ID |

---

### 3.2 注册（无需 Bearer）

| 项 | 值 |
|----|-----|
| 方法 / 路径 | `POST /student/register` |
| Content-Type | `application/json` |

**请求体 `StudentRegisterRequest`：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| studentNo | string | 是 | 学号，用于匹配已有学生记录 |
| username | string | 是 | 自助注册的用户名 |
| password | string | 是 | 密码 |

**响应：** `Result<StudentLoginData>`（成功时同样返回 `token`、`username`、`studentId`，与登录结构一致）。

常见失败原因（`message`）：学号不存在、该学号对应学生已注册、用户名已被占用等。

---

## 4. AI 聊天（SSE，均需对应角色 Token）

### 4.1 教师端 `GET /teacher/chat`

| 项 | 值 |
|----|-----|
| 鉴权 | Bearer：**教师** Token |
| Query | `timeId`：会话/记忆维度 ID；`message`：用户输入 |
| 响应类型 | `text/event-stream`（SSE） |

后端会将 JWT 解析为教师用户，并加载该教师名下课程信息后调用模型。**超时：** 约 **600000 ms**（10 分钟）。

---

### 4.2 学生端 `GET /student/chat`

| 项 | 值 |
|----|-----|
| 鉴权 | Bearer：**学生** Token |
| Query | `timeId`、`message`（含义同教师端） |
| 响应类型 | `text/event-stream`（SSE） |

控制器内使用 `StudentAIChat` 流式输出；**超时：** 约 **600000 ms**（10 分钟）。

---

**SSE 通用说明：** 每个 SSE `data` 事件为一段 **UTF-8 纯文本**（模型输出片段）。浏览器原生 `EventSource` 无法设置 `Authorization`，若必须用 Bearer，请使用支持自定义头的 SSE 客户端或代理。

---

## 5. HTTP 状态与业务码

| 场景 | 表现 |
|------|------|
| 未带 `Authorization` 或格式非 `Bearer ` | HTTP **401**，响应体为拦截器文案（如「未登录」），**非** `Result` JSON |
| Token 解析失败 | HTTP **401**，「Token无效」 |
| 业务失败（多数 JSON 接口） | HTTP 可能仍为 200，以 `Result.code`、`Result.message` 为准（如 400、401、403） |

---

## 7. 对接顺序建议

1. **教师端：** `POST /teacher/login` → 使用返回的 `token` 调用 `GET /teacher/chat`。
2. **学生端：** `POST /student/login` 或 `POST /student/register` → 使用返回的 `token` 调用 `GET /student/chat`。
3. 后续请求统一增加：`Authorization: Bearer <JWT>`（Token 调错模块将返回 401）。
4. 对返回 `Result` 的接口：以 `code === 200` 作为业务成功判断，并处理 `message`。
5. 聊天使用 SSE；`timeId` 由前端与会话状态一致即可（与后端会话记忆关联）。

---

*文档与仓库代码同步维护；端口以 `application.yml` 与实际部署为准。*
