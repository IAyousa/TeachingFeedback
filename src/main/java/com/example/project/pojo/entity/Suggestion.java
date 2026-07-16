package com.example.project.pojo.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Suggestion {
    private Long id;
    private Long courseId;
    private LocalDate suggestionDate;
    /** KNOWLEDGE / PACE / INTERACTION / CLARITY / ENGAGEMENT / MATERIAL */
    private String dimension;
    private String content;
    /** HIGH / MEDIUM / LOW */
    private String priority;
    /** AI / TEACHER */
    private String generatedBy;
    private LocalDateTime createdAt;
}
