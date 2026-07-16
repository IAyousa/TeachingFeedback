package com.example.project.mapper;

import com.example.project.pojo.entity.Enrollment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface EnrollmentMapper {
    List<Enrollment> selectByStudentId(@Param("studentId") Long studentId);
    List<Enrollment> selectByCourseId(@Param("courseId") Long courseId);
    int insert(Enrollment row);
    int delete(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}
