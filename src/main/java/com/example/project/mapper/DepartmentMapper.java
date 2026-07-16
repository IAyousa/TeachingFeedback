package com.example.project.mapper;

import com.example.project.pojo.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DepartmentMapper {
    Department selectById(@Param("id") Long id);
    List<Department> selectAll();
}
