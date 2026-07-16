package com.example.project.pojo.vo;

import lombok.Data;

/** 教师名下课程简略信息 */
@Data
public class CourseBriefVo {
    private Long courseId;
    private String courseName;
    private String courseCode;
    private String semester;
    private String teacherName;
    private Integer studentCount;
    private String status;
}
