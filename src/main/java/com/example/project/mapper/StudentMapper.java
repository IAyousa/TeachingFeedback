package com.example.project.mapper;

import com.example.project.pojo.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface StudentMapper {
    int insert(Student row);
    int updateById(Student row);
    int deleteById(@Param("id") Long id);
    Student selectById(@Param("id") Long id);
    Student selectByStudentNo(@Param("studentNo") String studentNo);
    List<Student> selectAll();
    String selectPasswordByUsername(@Param("username") String username);
    Long selectIdByUsername(@Param("username") String username);
    int updateUsernameAndPassword(@Param("id") Long id,
                                  @Param("username") String username,
                                  @Param("passwordHash") String passwordHash);
}
