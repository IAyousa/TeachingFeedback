package com.example.project.pojo.vo;

import lombok.Data;
import java.util.List;

/** 教师会话上下文（供 AI 工具使用） */
@Data
public class TeacherSessionVo {
    private Long teacherId;
    private String loginUsername;
    private String fullName;
    private String phone;
    private List<Long> boundCourseIds;
    private List<CourseInfoVo> courses;
}
