package com.example.project.pojo.dto;

import lombok.Data;

/** 登录请求 */
@Data
public class AuthRequest {
    private String username;
    private String password;
}
