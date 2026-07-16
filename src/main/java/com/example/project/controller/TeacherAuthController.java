package com.example.project.controller;

import com.example.project.common.Result;
import com.example.project.pojo.dto.AuthRequest;
import com.example.project.pojo.dto.LoginData;
import com.example.project.service.TeacherService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teacher")
public class TeacherAuthController {

    private final TeacherService teacherService;

    public TeacherAuthController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @PostMapping(value = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<LoginData> login(@RequestBody AuthRequest body) {
        return teacherService.login(body);
    }

    @PostMapping(value = "/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<Void> register(@RequestBody AuthRequest body) {
        return teacherService.register(body);
    }
}
