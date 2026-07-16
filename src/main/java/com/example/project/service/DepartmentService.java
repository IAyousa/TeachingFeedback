package com.example.project.service;

import com.example.project.pojo.entity.Department;
import java.util.List;

public interface DepartmentService {
    Department getById(Long id);
    List<Department> listAll();
}
