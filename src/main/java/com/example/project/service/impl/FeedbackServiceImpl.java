package com.example.project.service.impl;

import com.example.project.mapper.FeedbackMapper;
import com.example.project.pojo.entity.Feedback;
import com.example.project.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Override
    public List<Feedback> listByCourseAndDate(Long courseId, LocalDate feedbackDate) {
        return feedbackMapper.selectByCourseAndDate(courseId, feedbackDate);
    }

    @Override
    public List<Feedback> listByCourseAndDateRange(Long courseId, LocalDate start, LocalDate end) {
        return feedbackMapper.selectByCourseAndDateRange(courseId, start, end);
    }

    @Override
    public List<Feedback> listByStudentAndCourse(Long studentId, Long courseId) {
        return feedbackMapper.selectByStudentAndCourse(studentId, courseId);
    }

    @Override
    public List<Feedback> listByStudentId(Long studentId) {
        return feedbackMapper.selectByStudentId(studentId);
    }

    @Override
    public List<Feedback> listByCourseId(Long courseId) {
        return feedbackMapper.selectByCourseId(courseId);
    }

    @Override
    public int add(Feedback feedback) { return feedbackMapper.insert(feedback); }
}
