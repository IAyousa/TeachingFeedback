package com.example.project.mapper;

import com.example.project.pojo.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CourseMapper {
    int insert(Course row);
    int updateById(Course row);
    int deleteById(@Param("id") Long id);
    Course selectById(@Param("id") Long id);
    List<Course> selectAll();
    List<Course> selectByDeptId(@Param("deptId") Long deptId);
    String selectPasswordByUsername(@Param("username") String username);
    Long selectIdByUsername(@Param("username") String username);
}
