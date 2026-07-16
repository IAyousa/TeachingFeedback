package com.example.project.pojo.vo;

import lombok.Data;
import java.util.List;

/** 学生会话上下文（供 AI 工具使用） */
@Data
public class StudentSessionVo {
    private Long studentId;
    private String loginUsername;
    private String fullName;
    private String studentNo;
    private List<CourseInfoVo> courses;
}
