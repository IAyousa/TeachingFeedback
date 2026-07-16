package com.example.project.mapper;

import com.example.project.pojo.entity.Feedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface FeedbackMapper {
    int insert(Feedback row);
    Feedback selectById(@Param("id") Long id);
    List<Feedback> selectByCourseAndDate(@Param("courseId") Long courseId,
                                          @Param("feedbackDate") LocalDate feedbackDate);
    List<Feedback> selectByCourseAndDateRange(@Param("courseId") Long courseId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);
    List<Feedback> selectByStudentAndCourse(@Param("studentId") Long studentId,
                                             @Param("courseId") Long courseId);
    List<Feedback> selectByStudentId(@Param("studentId") Long studentId);
    List<Feedback> selectByCourseId(@Param("courseId") Long courseId);
}
