package com.example.project.controller;

import com.example.project.common.Result;
import com.example.project.pojo.dto.AuthRequest;
import com.example.project.pojo.dto.StudentLoginData;
import com.example.project.pojo.dto.StudentRegisterRequest;
import com.example.project.service.StudentAuthService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student")
public class StudentAuthController {

    private final StudentAuthService studentAuthService;

    public StudentAuthController(StudentAuthService studentAuthService) {
        this.studentAuthService = studentAuthService;
    }

    @PostMapping(value = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<StudentLoginData> login(@RequestBody AuthRequest body) {
        return studentAuthService.login(body);
    }

    @PostMapping(value = "/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<StudentLoginData> register(@RequestBody StudentRegisterRequest body) {
        return studentAuthService.register(body);
    }
}
