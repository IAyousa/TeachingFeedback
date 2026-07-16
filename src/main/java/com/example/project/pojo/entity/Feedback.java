package com.example.project.pojo.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
public class Feedback {
    private Long id;
    private Long courseId;
    private Long studentId;
    private LocalDate feedbackDate;
    /** KNOWLEDGE / PACE / INTERACTION / CLARITY / ENGAGEMENT / MATERIAL */
    private String dimension;
    private Integer score;
    private String comment;
    private Boolean isAnalyzed;
    private LocalDateTime createdAt;
}
