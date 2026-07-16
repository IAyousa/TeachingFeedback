package com.example.project.service;

import com.example.project.pojo.entity.Feedback;
import java.time.LocalDate;
import java.util.List;

public interface FeedbackService {
    List<Feedback> listByCourseAndDate(Long courseId, LocalDate feedbackDate);
    List<Feedback> listByCourseAndDateRange(Long courseId, LocalDate start, LocalDate end);
    List<Feedback> listByStudentAndCourse(Long studentId, Long courseId);
    List<Feedback> listByStudentId(Long studentId);
    List<Feedback> listByCourseId(Long courseId);
    int add(Feedback feedback);
}
