package com.example.project.service.impl;

import com.example.project.common.Result;
import com.example.project.mapper.StudentMapper;
import com.example.project.pojo.dto.AuthRequest;
import com.example.project.pojo.dto.StudentLoginData;
import com.example.project.pojo.dto.StudentRegisterRequest;
import com.example.project.pojo.entity.Student;
import com.example.project.security.JwtUtil;
import com.example.project.service.StudentAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class StudentAuthServiceImpl implements StudentAuthService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private StudentMapper studentMapper;

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }

    @Override
    public Result<StudentLoginData> login(AuthRequest request) {
        if (request == null) return Result.fail("请求体不能为空");
        String u = request.getUsername() == null ? null : request.getUsername().trim();
        String p = request.getPassword();
        if (isBlank(u) || isBlank(p)) return Result.fail("用户名或密码不能为空");
        String hash = studentMapper.selectPasswordByUsername(u);
        if (hash == null || !passwordEncoder.matches(p, hash))
            return Result.fail("用户名或密码错误");
        Long sid = studentMapper.selectIdByUsername(u);
        String token = JwtUtil.generateToken(u, JwtUtil.ROLE_STUDENT);
        return Result.ok("登录成功", new StudentLoginData(token, u, sid));
    }

    @Override
    public Result<StudentLoginData> register(StudentRegisterRequest request) {
        if (request == null) return Result.fail("请求体不能为空");
        String sno = request.getStudentNo() == null ? null : request.getStudentNo().trim();
        String u = request.getUsername() == null ? null : request.getUsername().trim();
        String p = request.getPassword();
        if (isBlank(sno)) return Result.fail("学号不能为空");
        if (isBlank(u) || isBlank(p)) return Result.fail("用户名或密码不能为空");

        Student student = studentMapper.selectByStudentNo(sno);
        if (student == null) return Result.fail("学号不存在，请核实");
        if (!isBlank(student.getUsername())) return Result.fail("该学生已注册账号");
        if (studentMapper.selectPasswordByUsername(u) != null)
            return Result.fail("用户名已被占用");

        String hash = passwordEncoder.encode(p);
        int rows = studentMapper.updateUsernameAndPassword(student.getId(), u, hash);
        if (rows > 0) {
            String token = JwtUtil.generateToken(u, JwtUtil.ROLE_STUDENT);
            return Result.ok("注册成功", new StudentLoginData(token, u, student.getId()));
        }
        return Result.fail("注册失败");
    }

    @Override
    public Long resolveStudentUserId(String jwtSubject) {
        if (jwtSubject == null || jwtSubject.isBlank()) return null;
        String s = jwtSubject.trim();
        Long id = studentMapper.selectIdByUsername(s);
        if (id != null) return id;
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; }
    }
}
