package com.example.project.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 学生端登录返回（含 studentId） */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentLoginData {
    private String token;
    private String username;
    private Long studentId;
}
