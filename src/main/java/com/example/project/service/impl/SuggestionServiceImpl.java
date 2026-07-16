package com.example.project.service.impl;

import com.example.project.mapper.SuggestionMapper;
import com.example.project.pojo.entity.Suggestion;
import com.example.project.service.SuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SuggestionServiceImpl implements SuggestionService {

    @Autowired
    private SuggestionMapper suggestionMapper;

    @Override
    public List<Suggestion> listByCourseId(Long courseId) {
        return suggestionMapper.selectByCourseId(courseId);
    }

    @Override
    public int add(Suggestion suggestion) { return suggestionMapper.insert(suggestion); }
}
