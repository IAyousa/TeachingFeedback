package com.example.project.pojo.dto;

import lombok.Data;

/** 学生提交反馈请求 */
@Data
public class FeedbackSubmitRequest {
    private Long courseId;
    private String dimension;
    private Integer score;
    private String comment;
}
