package com.example.project.service;

import com.example.project.pojo.vo.CourseInfoVo;

import java.util.List;
import java.util.Map;

/**
 * 教学反馈 AI 数据服务层 — 供 @Tool 方法调用，封装查询逻辑并返回 JSON 字符串。
 */
public interface TeachingAiDataService {

    /** 从当前请求 JWT 解析教师 ID */
    Long resolveCurrentTeacherIdFromJwt();

    /** 从当前请求 JWT 解析学生 ID */
    Long resolveCurrentStudentIdFromJwt();

    /* === 教师端工具 === */

    /** 获取当前登录教师的上下文信息（身份+名下课程列表） */
    String getCurrentTeacherContext();

    /** 查询课程基础信息（含院系信息） */
    String queryCourseInfo(String courseId);

    /** 按课程和日期查询反馈汇总 */
    String queryFeedbackByDate(String courseId, String feedbackDate);

    /** 查询日期范围内的反馈趋势 */
    String queryFeedbackTrend(String courseId, String startDate, String endDate);

    /** 知识点聚类分析 */
    String analyzeKnowledgeClustering(String courseId);

    /** 生成教学改进建议 */
    String generateSuggestions(String courseId);

    /** 查询历史改进建议 */
    String querySuggestionsHistory(String courseId, String dateSince);

    /** 当前教师名下的课程列表 */
    String listMyCourses();

    /** 多课程对比分析 */
    String compareCourses(String courseIds);

    /** 查询院系信息 */
    String queryDepartmentInfo(String deptId);

    /** 生成课程教学质量报告 */
    String generateCourseReport(String courseId);

    /** 查询 FAQ */
    String queryFAQ(String category);

    /** 异常模式检测 */
    String detectAnomalies(String courseId);

    /** 反馈参与率趋势 */
    String queryParticipationTrend(String courseId, String weeks);

    /** 关键字搜索反馈内容 */
    String searchFeedbackContent(String courseId, String keyword);

    /* === 学生端工具 === */

    /** 获取当前登录学生的上下文 */
    String getCurrentStudentContext();

    /** 查询本人历史反馈 */
    String queryMyFeedbackHistory(String courseId);

    /** 查询本人课程信息 */
    String queryMyCourseInfo(String courseId);

    /** 提交反馈 */
    String submitFeedback(String courseId, String dimension, String score, String comment);

    /** 本人反馈统计摘要 */
    String queryMyFeedbackSummary();

    /** 本人所在院系信息 */
    String queryMyDepartmentInfo();
}
