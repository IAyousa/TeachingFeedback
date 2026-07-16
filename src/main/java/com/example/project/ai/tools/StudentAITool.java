package com.example.project.ai.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.project.service.TeachingAiDataService;

@Component
public class StudentAITool {

    @Autowired
    private TeachingAiDataService dataService;

    @Tool(description = "根据 JWT 解析当前学生身份，返回 studentId、姓名、已选课程列表。无参数。此工具已不再需要通过 HTTP 请求解析 JWT，stream() 方法已将登录信息注入提示词。")
    public String getCurrentStudentContext() {
        return dataService.getCurrentStudentContext();
    }

    @Tool(description = "查询本人指定课程的基础信息。参数 courseId：课程主键 ID。")
    public String queryMyCourseInfo(String courseId) {
        return dataService.queryMyCourseInfo(courseId);
    }

    @Tool(description = "查询本人的历史反馈记录。参数 courseId：可选，筛选特定课程。")
    public String queryMyFeedbackHistory(String courseId) {
        return dataService.queryMyFeedbackHistory(courseId);
    }

    @Tool(description = "提交一条新的课程反馈。参数 courseId；dimension：反馈维度（KNOWLEDGE/PACE/INTERACTION/CLARITY/ENGAGEMENT/MATERIAL）；score：1-5分；comment：文字评价。")
    public String submitFeedback(String courseId, String dimension, String score, String comment) {
        return dataService.submitFeedback(courseId, dimension, score, comment);
    }

    @Tool(description = "查询本人已提交反馈的统计摘要：总反馈数、各维度平均分。无参数。")
    public String queryMyFeedbackSummary() {
        return dataService.queryMyFeedbackSummary();
    }

    @Tool(description = "查询本人所在院系的信息。无参数（自动关联本人院系）。")
    public String queryMyDepartmentInfo() {
        return dataService.queryMyDepartmentInfo();
    }
}
