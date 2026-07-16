package com.example.project.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Enrollment {
    private Long id;
    private Long studentId;
    private Long courseId;
    private LocalDateTime createdAt;
}
