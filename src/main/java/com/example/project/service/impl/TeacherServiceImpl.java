package com.example.project.service.impl;

import com.example.project.common.Result;
import com.example.project.mapper.TeacherMapper;
import com.example.project.pojo.dto.AuthRequest;
import com.example.project.pojo.dto.LoginData;
import com.example.project.security.JwtUtil;
import com.example.project.service.TeacherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TeacherServiceImpl implements TeacherService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }

    @Override
    public Result<LoginData> login(AuthRequest request) {
        if (request == null) return Result.fail("请求体不能为空");
        String u = request.getUsername() == null ? null : request.getUsername().trim();
        String p = request.getPassword();
        if (isBlank(u) || isBlank(p)) return Result.fail("用户名或密码不能为空");
        String hash = teacherMapper.selectPasswordByUsername(u);
        if (hash == null || !passwordEncoder.matches(p, hash))
            return Result.fail("用户名或密码错误");
        String token = JwtUtil.generateToken(u, JwtUtil.ROLE_TEACHER);
        return Result.ok("登录成功", new LoginData(token, u));
    }

    @Override
    public Result<Void> register(AuthRequest request) {
        if (request == null) return Result.fail("请求体不能为空");
        String u = request.getUsername() == null ? null : request.getUsername().trim();
        String p = request.getPassword();
        if (isBlank(u) || isBlank(p)) return Result.fail("用户名或密码不能为空");
        if (teacherMapper.selectPasswordByUsername(u) != null)
            return Result.fail("用户名已存在");
        String hash = passwordEncoder.encode(p);
        teacherMapper.insertUser(u, hash);
        return Result.ok("注册成功", null);
    }

    @Override
    public Long resolveTeacherUserId(String jwtSubject) {
        if (jwtSubject == null || jwtSubject.isBlank()) return null;
        String s = jwtSubject.trim();
        Long id = teacherMapper.selectIdByUsername(s);
        if (id != null) return id;
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; }
    }

    @Override
    public String getInfo(String username) {
        String u = username == null ? null : username.trim();
        if (isBlank(u)) return "用户名不能为空";
        Map<String, Object> row = teacherMapper.selectAccountByUsername(u);
        if (row == null || row.isEmpty()) return "用户不存在";
        try { return objectMapper.writeValueAsString(row); }
        catch (Exception e) { return "查询失败"; }
    }

    @Override
    public String updateInfo(String username, String oldPassword, String newPassword) {
        String u = username == null ? null : username.trim();
        if (isBlank(u) || isBlank(oldPassword) || isBlank(newPassword))
            return "用户名、原密码、新密码均不能为空";
        if (oldPassword.equals(newPassword)) return "新密码不能与原密码相同";
        String hash = teacherMapper.selectPasswordByUsername(u);
        if (hash == null) return "用户不存在";
        if (!passwordEncoder.matches(oldPassword, hash)) return "原密码错误";
        teacherMapper.updatePasswordHash(u, passwordEncoder.encode(newPassword));
        return "密码修改成功";
    }

    @Override
    public String deleteInfo(String username, String password) {
        String u = username == null ? null : username.trim();
        if (isBlank(u) || isBlank(password)) return "用户名或密码不能为空";
        String hash = teacherMapper.selectPasswordByUsername(u);
        if (hash == null || !passwordEncoder.matches(password, hash))
            return "用户名或密码错误";
        teacherMapper.deleteByUsername(u);
        return "账号已注销";
    }
}
