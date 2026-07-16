-- =============================================================================
-- TeachingFeedback 数据库建表脚本
-- 数据库名：teaching_feedback
-- 字符集：utf8mb4
-- =============================================================================

-- 院系表
CREATE TABLE department (
    id            BIGINT UNSIGNED AUTO_INCREMENT COMMENT '主键ID'
        PRIMARY KEY,
    dept_name     VARCHAR(128)                       NOT NULL COMMENT '院系名称',
    contact_phone VARCHAR(32)                        NULL     COMMENT '办公电话',
    location      VARCHAR(255)                       NULL     COMMENT '办公地点',
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL
) COMMENT '院系信息表';

-- 课程表
CREATE TABLE course (
    id             BIGINT UNSIGNED AUTO_INCREMENT COMMENT '主键ID'
        PRIMARY KEY,
    dept_id        BIGINT UNSIGNED                                                 NOT NULL COMMENT '所属院系ID',
    course_name    VARCHAR(128)                                                    NOT NULL COMMENT '课程名称',
    course_code    VARCHAR(64)                                                     NOT NULL COMMENT '课程编号',
    teacher_name   VARCHAR(64)                                                     NOT NULL COMMENT '授课教师姓名',
    semester       VARCHAR(32)                                                     NOT NULL COMMENT '学期（如 2026春）',
    student_count  INT                   DEFAULT 0                                 NOT NULL COMMENT '选课人数',
    status         ENUM ('ACTIVE', 'ENDED') DEFAULT 'ACTIVE'                       NOT NULL COMMENT '课程状态',
    username       VARCHAR(64)                                                     NULL     COMMENT '课程账号（用于学生端登录）',
    password       VARCHAR(128)                                                    NULL     COMMENT '账号密码（BCrypt）',
    created_at     DATETIME              DEFAULT CURRENT_TIMESTAMP                 NOT NULL,
    updated_at     DATETIME              DEFAULT CURRENT_TIMESTAMP                 NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_course_code
        UNIQUE (course_code),
    CONSTRAINT fk_course_dept
        FOREIGN KEY (dept_id) REFERENCES department (id)
            ON DELETE CASCADE
) COMMENT '课程信息表';

CREATE INDEX idx_course_dept
    ON course (dept_id);

-- 教师表
CREATE TABLE teacher (
    id         BIGINT UNSIGNED AUTO_INCREMENT COMMENT '主键ID'
        PRIMARY KEY,
    username   VARCHAR(64)                        NOT NULL COMMENT '登录账号',
    password   VARCHAR(128)                       NOT NULL COMMENT '登录密码（BCrypt）',
    full_name  VARCHAR(64)                        NOT NULL COMMENT '姓名',
    phone      VARCHAR(32)                        NOT NULL COMMENT '手机号',
    dept_id    BIGINT UNSIGNED                    NULL     COMMENT '所属院系ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_teacher_username
        UNIQUE (username),
    CONSTRAINT fk_teacher_dept
        FOREIGN KEY (dept_id) REFERENCES department (id)
            ON DELETE SET NULL
) COMMENT '教师信息表';

-- 学生表
CREATE TABLE student (
    id           BIGINT UNSIGNED AUTO_INCREMENT COMMENT '主键ID'
        PRIMARY KEY,
    student_no   VARCHAR(64)                        NOT NULL COMMENT '学号',
    full_name    VARCHAR(64)                        NOT NULL COMMENT '姓名',
    username     VARCHAR(64)                        NULL     COMMENT '登录账号',
    password     VARCHAR(128)                       NULL     COMMENT '登录密码（BCrypt）',
    dept_id      BIGINT UNSIGNED                    NULL     COMMENT '所属院系ID',
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_student_no
        UNIQUE (student_no),
    CONSTRAINT uk_student_username
        UNIQUE (username),
    CONSTRAINT fk_student_dept
        FOREIGN KEY (dept_id) REFERENCES department (id)
            ON DELETE SET NULL
) COMMENT '学生信息表';

-- 选课关系表
CREATE TABLE enrollment (
    id          BIGINT UNSIGNED AUTO_INCREMENT COMMENT '主键ID'
        PRIMARY KEY,
    student_id  BIGINT UNSIGNED                      NOT NULL COMMENT '学生ID',
    course_id   BIGINT UNSIGNED                      NOT NULL COMMENT '课程ID',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP   NOT NULL,
    CONSTRAINT uk_student_course
        UNIQUE (student_id, course_id),
    CONSTRAINT fk_enrollment_student
        FOREIGN KEY (student_id) REFERENCES student (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_enrollment_course
        FOREIGN KEY (course_id) REFERENCES course (id)
            ON DELETE CASCADE
) COMMENT '选课关系表';

-- 反馈记录表
CREATE TABLE feedback (
    id            BIGINT UNSIGNED AUTO_INCREMENT COMMENT '主键ID'
        PRIMARY KEY,
    course_id     BIGINT UNSIGNED                                              NOT NULL COMMENT '课程ID',
    student_id    BIGINT UNSIGNED                                              NOT NULL COMMENT '学生ID',
    feedback_date DATE                                                         NOT NULL COMMENT '反馈日期',
    dimension     ENUM ('KNOWLEDGE', 'PACE', 'INTERACTION', 'CLARITY', 'ENGAGEMENT', 'MATERIAL')
                                                                               NOT NULL COMMENT '反馈维度',
    score         TINYINT UNSIGNED                                             NOT NULL COMMENT '评分（1-5）',
    comment       TEXT                                                         NULL     COMMENT '文字反馈',
    is_analyzed   TINYINT(1) DEFAULT 0                                         NOT NULL COMMENT '是否已纳入聚类分析',
    created_at    DATETIME   DEFAULT CURRENT_TIMESTAMP                         NOT NULL,
    CONSTRAINT fk_feedback_course
        FOREIGN KEY (course_id) REFERENCES course (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_feedback_student
        FOREIGN KEY (student_id) REFERENCES student (id)
            ON DELETE CASCADE
) COMMENT '学生反馈记录表';

CREATE INDEX idx_feedback_course_date
    ON feedback (course_id, feedback_date);

-- 教学改进建议表
CREATE TABLE suggestion (
    id              BIGINT UNSIGNED AUTO_INCREMENT COMMENT '主键ID'
        PRIMARY KEY,
    course_id       BIGINT UNSIGNED                                                    NOT NULL COMMENT '课程ID',
    suggestion_date DATE                                                               NOT NULL COMMENT '建议生成日期',
    dimension       ENUM ('KNOWLEDGE', 'PACE', 'INTERACTION', 'CLARITY', 'ENGAGEMENT', 'MATERIAL')
                                                                                       NOT NULL COMMENT '对应反馈维度',
    content         TEXT                                                               NOT NULL COMMENT '改进建议内容',
    priority        ENUM ('HIGH', 'MEDIUM', 'LOW') DEFAULT 'MEDIUM'                    NOT NULL COMMENT '优先级',
    generated_by    ENUM ('AI', 'TEACHER')         DEFAULT 'AI'                        NOT NULL COMMENT '来源',
    created_at      DATETIME                      DEFAULT CURRENT_TIMESTAMP            NOT NULL,
    CONSTRAINT fk_suggestion_course
        FOREIGN KEY (course_id) REFERENCES course (id)
            ON DELETE CASCADE
) COMMENT '教学改进建议表';

CREATE INDEX idx_suggestion_course
    ON suggestion (course_id);

-- FAQ 表（保留，内容改为教学相关）
CREATE TABLE faq (
    id         BIGINT UNSIGNED AUTO_INCREMENT
        PRIMARY KEY,
    category   VARCHAR(64)                        NOT NULL COMMENT '分类（如：COURSE, FEEDBACK, ACCOUNT, GENERAL）',
    question   VARCHAR(255)                       NOT NULL COMMENT '问题',
    answer     TEXT                               NOT NULL COMMENT '回答',
    sort_order INT      DEFAULT 0                 NOT NULL COMMENT '排序权重',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL
) COMMENT '常见问答表';
