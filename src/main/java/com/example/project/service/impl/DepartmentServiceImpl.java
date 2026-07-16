package com.example.project.service.impl;

import com.example.project.mapper.DepartmentMapper;
import com.example.project.pojo.entity.Department;
import com.example.project.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Override
    public Department getById(Long id) { return departmentMapper.selectById(id); }

    @Override
    public List<Department> listAll() { return departmentMapper.selectAll(); }
}
