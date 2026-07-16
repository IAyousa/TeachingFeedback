package com.example.project.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Student {
    private Long id;
    private String studentNo;
    private String fullName;
    private String username;
    private String password;
    private Long deptId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
