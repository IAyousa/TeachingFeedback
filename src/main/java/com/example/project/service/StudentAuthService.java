package com.example.project.service;

import com.example.project.common.Result;
import com.example.project.pojo.dto.AuthRequest;
import com.example.project.pojo.dto.StudentLoginData;
import com.example.project.pojo.dto.StudentRegisterRequest;

public interface StudentAuthService {
    Result<StudentLoginData> login(AuthRequest request);
    Result<StudentLoginData> register(StudentRegisterRequest request);
    Long resolveStudentUserId(String jwtSubject);
}
