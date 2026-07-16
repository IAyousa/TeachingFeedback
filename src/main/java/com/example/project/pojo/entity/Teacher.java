package com.example.project.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Teacher {
    private Long id;
    private String username;
    private String password;
    private String fullName;
    private String phone;
    private Long deptId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
