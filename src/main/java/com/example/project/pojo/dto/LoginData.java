package com.example.project.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 登录返回数据 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginData {
    private String token;
    private String username;
}
