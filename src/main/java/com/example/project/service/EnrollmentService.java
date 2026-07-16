package com.example.project.service;

import com.example.project.pojo.entity.Enrollment;
import java.util.List;

public interface EnrollmentService {
    List<Enrollment> listByStudentId(Long studentId);
    List<Enrollment> listByCourseId(Long courseId);
    List<Long> listCourseIdsByStudentId(Long studentId);
}
