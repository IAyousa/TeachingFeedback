package com.example.project.service;

import com.example.project.pojo.entity.Course;
import com.example.project.pojo.vo.CourseInfoVo;
import java.util.List;

public interface CourseService {
    Course getById(Long id);
    CourseInfoVo getCourseWithDept(Long courseId);
    List<Course> listByTeacherId(Long teacherId);
    List<Course> listByDeptId(Long deptId);
    List<Course> listAll();
}
