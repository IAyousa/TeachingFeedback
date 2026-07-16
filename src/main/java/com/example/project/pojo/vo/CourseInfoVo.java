package com.example.project.pojo.vo;

import lombok.Data;

/** 课程基础信息 + 院系信息（供 AI 工具使用） */
@Data
public class CourseInfoVo {
    private Long courseId;
    private String courseName;
    private String courseCode;
    private String teacherName;
    private String semester;
    private Integer studentCount;
    private String status;
    private Long deptId;
    private String deptName;
    private String deptContactPhone;
    private String deptLocation;
}
