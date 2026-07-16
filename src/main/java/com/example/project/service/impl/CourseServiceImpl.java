package com.example.project.service.impl;

import com.example.project.mapper.CourseMapper;
import com.example.project.mapper.DepartmentMapper;
import com.example.project.pojo.entity.Course;
import com.example.project.pojo.entity.Department;
import com.example.project.pojo.vo.CourseInfoVo;
import com.example.project.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Override
    public Course getById(Long id) { return courseMapper.selectById(id); }

    @Override
    public CourseInfoVo getCourseWithDept(Long courseId) {
        Course c = courseMapper.selectById(courseId);
        if (c == null) return null;
        Department d = departmentMapper.selectById(c.getDeptId());
        CourseInfoVo vo = new CourseInfoVo();
        vo.setCourseId(c.getId());
        vo.setCourseName(c.getCourseName());
        vo.setCourseCode(c.getCourseCode());
        vo.setTeacherName(c.getTeacherName());
        vo.setSemester(c.getSemester());
        vo.setStudentCount(c.getStudentCount());
        vo.setStatus(c.getStatus());
        vo.setDeptId(c.getDeptId());
        if (d != null) {
            vo.setDeptName(d.getDeptName());
            vo.setDeptContactPhone(d.getContactPhone());
            vo.setDeptLocation(d.getLocation());
        }
        return vo;
    }

    @Override
    public List<Course> listByTeacherId(Long teacherId) { return courseMapper.selectAll(); }

    @Override
    public List<Course> listByDeptId(Long deptId) { return courseMapper.selectByDeptId(deptId); }

    @Override
    public List<Course> listAll() { return courseMapper.selectAll(); }
}
