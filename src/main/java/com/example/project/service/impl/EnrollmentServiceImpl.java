package com.example.project.service.impl;

import com.example.project.mapper.EnrollmentMapper;
import com.example.project.pojo.entity.Enrollment;
import com.example.project.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    @Autowired
    private EnrollmentMapper enrollmentMapper;

    @Override
    public List<Enrollment> listByStudentId(Long studentId) {
        return enrollmentMapper.selectByStudentId(studentId);
    }

    @Override
    public List<Enrollment> listByCourseId(Long courseId) {
        return enrollmentMapper.selectByCourseId(courseId);
    }

    @Override
    public List<Long> listCourseIdsByStudentId(Long studentId) {
        return enrollmentMapper.selectByStudentId(studentId)
                .stream().map(Enrollment::getCourseId).toList();
    }
}
