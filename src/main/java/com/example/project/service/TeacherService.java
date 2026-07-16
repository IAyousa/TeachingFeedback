package com.example.project.service;

import com.example.project.common.Result;
import com.example.project.pojo.dto.AuthRequest;
import com.example.project.pojo.dto.LoginData;

public interface TeacherService {
    Result<LoginData> login(AuthRequest request);
    Result<Void> register(AuthRequest request);
    Long resolveTeacherUserId(String jwtSubject);
    String getInfo(String username);
    String updateInfo(String username, String oldPassword, String newPassword);
    String deleteInfo(String username, String password);
}
