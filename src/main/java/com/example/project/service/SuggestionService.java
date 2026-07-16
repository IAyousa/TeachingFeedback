package com.example.project.service;

import com.example.project.pojo.entity.Suggestion;
import java.util.List;

public interface SuggestionService {
    List<Suggestion> listByCourseId(Long courseId);
    int add(Suggestion suggestion);
}
