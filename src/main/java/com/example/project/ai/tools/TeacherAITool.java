package com.example.project.ai.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.project.service.TeachingAiDataService;

@Component
public class TeacherAITool {

    private static final String TOOL_DESC_COURSE_ID_TAIL =
            " 参数 courseId 必须与用户提示中「会话上下文」所列课程ID及「【工具参数】」行数字字符串完全一致，勿留空或猜测。";

    @Autowired
    private TeachingAiDataService dataService;

    @Tool(description = "根据 JWT 解析当前教师身份，返回教师 ID、姓名、手机号、名下课程列表及课程基本信息。用于对话开始时自动确认身份。无参数。")
    public String getCurrentTeacherContext() {
        return dataService.getCurrentTeacherContext();
    }

    @Tool(description = "查询指定课程的基础信息：课程名称、编号、学期、授课教师、选课人数、所属院系及联系方式。参数 courseId：课程主键 ID。" + TOOL_DESC_COURSE_ID_TAIL)
    public String queryCourseInfo(String courseId) {
        return dataService.queryCourseInfo(courseId);
    }

    @Tool(description = "按课程和日期查询学生反馈汇总：返回各维度平均分、反馈数量、文字反馈列表。参数 courseId；feedbackDate：YYYY-MM-DD，缺省为当日。" + TOOL_DESC_COURSE_ID_TAIL)
    public String queryFeedbackByDate(String courseId, String feedbackDate) {
        return dataService.queryFeedbackByDate(courseId, feedbackDate);
    }

    @Tool(description = "查询指定课程在日期范围内的反馈趋势：按日期返回各维度的评分变化趋势。参数 courseId；startDate：YYYY-MM-DD；endDate：YYYY-MM-DD。" + TOOL_DESC_COURSE_ID_TAIL)
    public String queryFeedbackTrend(String courseId, String startDate, String endDate) {
        return dataService.queryFeedbackTrend(courseId, startDate, endDate);
    }

    @Tool(description = "对某门课程的反馈进行知识点聚类分析：识别学生对各知识维度的掌握程度，返回各维度平均分、薄弱环节列表。参数 courseId。" + TOOL_DESC_COURSE_ID_TAIL)
    public String analyzeKnowledgeClustering(String courseId) {
        return dataService.analyzeKnowledgeClustering(courseId);
    }

    @Tool(description = "根据反馈数据自动生成教学改进建议：针对薄弱维度提供可操作建议。参数 courseId。" + TOOL_DESC_COURSE_ID_TAIL)
    public String generateSuggestions(String courseId) {
        return dataService.generateSuggestions(courseId);
    }

    @Tool(description = "查询某门课程的历史改进建议列表，按日期倒序。参数 courseId；dateSince：可选，YYYY-MM-DD。" + TOOL_DESC_COURSE_ID_TAIL)
    public String querySuggestionsHistory(String courseId, String dateSince) {
        return dataService.querySuggestionsHistory(courseId, dateSince);
    }

    @Tool(description = "查询当前教师名下的全部课程列表。无参数。")
    public String listMyCourses() {
        return dataService.listMyCourses();
    }

    @Tool(description = "对比分析多门课程的反馈数据：比较各维度评分差异。参数 courseIds：逗号分隔的课程ID列表。")
    public String compareCourses(String courseIds) {
        return dataService.compareCourses(courseIds);
    }

    @Tool(description = "查询院系信息：名称、办公电话、地点。参数 deptId：院系主键 ID。")
    public String queryDepartmentInfo(String deptId) {
        return dataService.queryDepartmentInfo(deptId);
    }

    @Tool(description = "汇总某门课程当前学期的整体教学质量报告：反馈统计、维度分析、改进建议列表。参数 courseId。" + TOOL_DESC_COURSE_ID_TAIL)
    public String generateCourseReport(String courseId) {
        return dataService.generateCourseReport(courseId);
    }

    @Tool(description = "查询教学常见问答。参数 category：可选，如 COURSE、FEEDBACK、ACCOUNT、GENERAL；留空返回全部。")
    public String queryFAQ(String category) {
        return dataService.queryFAQ(category);
    }

    @Tool(description = "检测反馈中的异常模式：某维度评分明显下降等情况。参数 courseId。" + TOOL_DESC_COURSE_ID_TAIL)
    public String detectAnomalies(String courseId) {
        return dataService.detectAnomalies(courseId);
    }

    @Tool(description = "查询过去N周内的反馈参与率变化。参数 courseId；weeks：正整数，缺省4，最大90。" + TOOL_DESC_COURSE_ID_TAIL)
    public String queryParticipationTrend(String courseId, String weeks) {
        return dataService.queryParticipationTrend(courseId, weeks);
    }

    @Tool(description = "按关键词搜索历史反馈的文字内容。参数 courseId；keyword：搜索关键词。" + TOOL_DESC_COURSE_ID_TAIL)
    public String searchFeedbackContent(String courseId, String keyword) {
        return dataService.searchFeedbackContent(courseId, keyword);
    }
}
