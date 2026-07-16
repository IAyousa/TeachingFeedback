package com.example.project.mapper;

import com.example.project.pojo.entity.Suggestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SuggestionMapper {
    int insert(Suggestion row);
    List<Suggestion> selectByCourseId(@Param("courseId") Long courseId);
    Suggestion selectById(@Param("id") Long id);
}
