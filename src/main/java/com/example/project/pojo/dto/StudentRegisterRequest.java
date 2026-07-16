package com.example.project.pojo.dto;

import lombok.Data;

/** 学生注册请求 */
@Data
public class StudentRegisterRequest {
    private String studentNo;  // 学号，用于验证身份
    private String username;
    private String password;
}
