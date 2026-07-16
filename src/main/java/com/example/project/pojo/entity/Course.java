package com.example.project.pojo.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Course {
    private Long id;
    private Long deptId;
    private String courseName;
    private String courseCode;
    private String teacherName;
    private String semester;
    private Integer studentCount;
    /** ACTIVE / ENDED */
    private String status;
    private String username;
    private String password;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
