package com.example.project.mapper;

import com.example.project.pojo.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface TeacherMapper {
    int insert(Teacher row);
    int updateById(Teacher row);
    int deleteById(@Param("id") Long id);
    int deleteByUsername(@Param("username") String username);
    Teacher selectById(@Param("id") Long id);
    List<Teacher> selectAll();
    String selectPasswordByUsername(@Param("username") String username);
    Long selectIdByUsername(@Param("username") String username);
    Map<String, Object> selectAccountByUsername(@Param("username") String username);
    int updatePasswordHash(@Param("username") String username, @Param("password") String password);
    int insertUser(@Param("username") String username, @Param("password") String password);
}
