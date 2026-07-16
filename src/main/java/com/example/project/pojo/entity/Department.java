package com.example.project.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Department {
    private Long id;
    private String deptName;
    private String contactPhone;
    private String location;
    private LocalDateTime createdAt;
}
