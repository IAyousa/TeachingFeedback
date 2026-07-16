package com.example.project.service.impl;

import com.example.project.mapper.*;
import com.example.project.pojo.entity.*;
import com.example.project.pojo.vo.*;
import com.example.project.security.JwtUtil;
import com.example.project.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeachingAiDataServiceImpl implements TeachingAiDataService {

    private static final int TREND_LOOKAHEAD_DAYS = 14;
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * 当前请求的学生登录用户名（SseEmitter 串行处理，每请求唯一，故可用 volatile）。
     * 由 {@link com.example.project.ai.client.StudentAIChat#stream} 在调用链开始前设置，
     * 供 Reactive 异步线程中的工具方法通过 {@link #resolveJwtSubjectFromRequest()} 兜底读取。
     */
    private static volatile String currentJwtFallback = null;

    /**
     * 设置当前请求的 JWT subject 兜底值（供 Reactor 异步线程中的工具方法使用）。
     */
    public static void setCurrentJwtFallback(String subject) {
        currentJwtFallback = subject;
    }

    /**
     * 清除当前请求的 JWT subject 兜底值。
     */
    public static void clearCurrentJwtFallback() {
        currentJwtFallback = null;
    }

    @Autowired private TeacherMapper teacherMapper;
    @Autowired private StudentMapper studentMapper;
    @Autowired private CourseMapper courseMapper;
    @Autowired private DepartmentMapper departmentMapper;
    @Autowired private EnrollmentMapper enrollmentMapper;
    @Autowired private FeedbackMapper feedbackMapper;
    @Autowired private SuggestionMapper suggestionMapper;
    @Autowired private FaqMapper faqMapper;
    @Autowired private CourseService courseService;
    @Autowired private DepartmentService departmentService;
    @Autowired private FeedbackService feedbackService;
    @Autowired private SuggestionService suggestionService;
    @Autowired private EnrollmentService enrollmentService;
    @Autowired private ObjectMapper objectMapper;

    // ==================== 辅助方法 ====================

    private static String resolveJwtSubjectFromRequest() {
        // 优先从 RequestContextHolder 取（同步线程）
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) {
            var req = sra.getRequest();
            Object u = req.getAttribute("userId");
            if (u != null && !u.toString().isBlank()) return u.toString().trim();
            String auth = req.getHeader(AUTH_HEADER);
            if (auth != null && auth.startsWith(BEARER_PREFIX)) {
                try { return JwtUtil.getUserId(auth.substring(7)); } catch (Exception ignored) {}
            }
        }
        // 兜底：Reactor 异步线程中 RequestContextHolder 可能为空，从 volatile fallback 取
        String fb = currentJwtFallback;
        if (fb != null && !fb.isBlank()) return fb;
        return null;
    }

    private static Long parseLong(String raw, String label) {
        if (raw == null || raw.isBlank()) throw new IllegalArgumentException(label + " 不能为空");
        return Long.parseLong(raw.trim());
    }

    private static LocalDate parseDateOrToday(String raw) {
        if (raw == null || raw.isBlank()) return LocalDate.now();
        return LocalDate.parse(raw.trim());
    }

    private static LocalDate parseDateNullable(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return LocalDate.parse(raw.trim());
    }

    private static int parseIntOrDefault(String raw, int def, int max) {
        int d = def;
        if (raw != null && !raw.isBlank()) d = Integer.parseInt(raw.trim());
        if (d < 1) d = 1;
        if (d > max) d = max;
        return d;
    }

    private String toJson(Object value) {
        try { return objectMapper.writeValueAsString(value != null ? value : Collections.emptyList()); }
        catch (Exception e) { return "[]"; }
    }

    @Override
    public Long resolveCurrentTeacherIdFromJwt() {
        String sub = resolveJwtSubjectFromRequest();
        if (sub == null) return null;
        return teacherMapper.selectIdByUsername(sub.trim());
    }

    @Override
    public Long resolveCurrentStudentIdFromJwt() {
        String sub = resolveJwtSubjectFromRequest();
        if (sub == null) return null;
        Long id = studentMapper.selectIdByUsername(sub.trim());
        if (id != null) return id;
        try { return Long.parseLong(sub); } catch (NumberFormatException e) { return null; }
    }

    // ==================== 教师端工具实现 ====================

    @Override
    public String getCurrentTeacherContext() {
        String username = resolveJwtSubjectFromRequest();
        if (username == null) return "当前请求未携带有效 JWT";
        Long tid = teacherMapper.selectIdByUsername(username);
        if (tid == null) return "未找到对应教师账号";
        Map<String, Object> account = teacherMapper.selectAccountByUsername(username);
        List<Course> courses = courseMapper.selectAll();
        TeacherSessionVo vo = new TeacherSessionVo();
        vo.setTeacherId(tid);
        vo.setLoginUsername(username);
        if (account != null) {
            vo.setFullName(account.get("full_name") != null ? account.get("full_name").toString() : null);
            vo.setPhone(account.get("phone") != null ? account.get("phone").toString() : null);
        }
        vo.setBoundCourseIds(courses.stream().map(Course::getId).toList());
        List<CourseInfoVo> courseVos = courses.stream().map(c -> courseService.getCourseWithDept(c.getId())).collect(Collectors.toList());
        vo.setCourses(courseVos);
        return toJson(vo);
    }

    @Override
    public String queryCourseInfo(String courseId) {
        try {
            CourseInfoVo vo = courseService.getCourseWithDept(parseLong(courseId, "courseId"));
            if (vo == null) return "未找到课程信息";
            return toJson(vo);
        } catch (IllegalArgumentException e) { return e.getMessage(); }
    }

    @Override
    public String queryFeedbackByDate(String courseId, String feedbackDate) {
        try {
            Long cid = parseLong(courseId, "courseId");
            LocalDate d = parseDateOrToday(feedbackDate);
            List<Feedback> rows = feedbackService.listByCourseAndDate(cid, d);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("courseId", cid);
            body.put("feedbackDate", d.toString());
            body.put("totalCount", rows.size());
            body.put("scoreSummary", buildScoreSummary(rows));
            body.put("feedbacks", rows);
            return toJson(body);
        } catch (Exception e) { return e.getMessage(); }
    }

    @Override
    public String queryFeedbackTrend(String courseId, String startDate, String endDate) {
        try {
            Long cid = parseLong(courseId, "courseId");
            LocalDate start = parseDateOrToday(startDate);
            LocalDate end = parseDateNullable(endDate);
            if (end == null) end = start.plusDays(TREND_LOOKAHEAD_DAYS - 1);
            List<Feedback> rows = feedbackService.listByCourseAndDateRange(cid, start, end);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("courseId", cid);
            body.put("startDate", start.toString());
            body.put("endDate", end.toString());
            // 按日期分组
            Map<String, List<Feedback>> byDate = rows.stream()
                    .collect(Collectors.groupingBy(f -> f.getFeedbackDate().toString()));
            List<Map<String, Object>> daily = new ArrayList<>();
            for (var entry : byDate.entrySet()) {
                Map<String, Object> day = new LinkedHashMap<>();
                day.put("date", entry.getKey());
                day.put("count", entry.getValue().size());
                day.putAll(buildScoreSummary(entry.getValue()));
                daily.add(day);
            }
            body.put("trend", daily);
            return toJson(body);
        } catch (Exception e) { return e.getMessage(); }
    }

    @Override
    public String analyzeKnowledgeClustering(String courseId) {
        try {
            Long cid = parseLong(courseId, "courseId");
            List<Feedback> all = feedbackService.listByCourseId(cid);
            Map<String, Double> dimensionAvg = all.stream()
                    .collect(Collectors.groupingBy(
                            Feedback::getDimension,
                            Collectors.averagingInt(Feedback::getScore)));
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("courseId", cid);
            body.put("dimensionAverages", dimensionAvg);
            // 识别薄弱环节（平均分 < 3.0）
            List<Map.Entry<String, Double>> weak = dimensionAvg.entrySet().stream()
                    .filter(e -> e.getValue() < 3.0).toList();
            body.put("weakDimensions", weak.stream().map(e -> Map.of("dimension", e.getKey(), "avgScore", e.getValue())).toList());
            body.put("totalFeedbackCount", all.size());
            return toJson(body);
        } catch (IllegalArgumentException e) { return e.getMessage(); }
    }

    @Override
    public String generateSuggestions(String courseId) {
        try {
            Long cid = parseLong(courseId, "courseId");
            String analysis = analyzeKnowledgeClustering(courseId);
            // 此处返回聚类分析结果给 AI，由 AI 生成建议文本
            // 也可以预先从 suggestion 表中读取已有建议
            List<Suggestion> existing = suggestionService.listByCourseId(cid);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("courseId", cid);
            body.put("existingSuggestions", existing);
            body.put("note", "以上为已有改进建议，请 AI 结合实际数据生成新的补充建议");
            return toJson(body);
        } catch (IllegalArgumentException e) { return e.getMessage(); }
    }

    @Override
    public String querySuggestionsHistory(String courseId, String dateSince) {
        try {
            Long cid = parseLong(courseId, "courseId");
            List<Suggestion> list = suggestionService.listByCourseId(cid);
            return toJson(list);
        } catch (IllegalArgumentException e) { return e.getMessage(); }
    }

    @Override
    public String listMyCourses() {
        List<Course> courses = courseMapper.selectAll();
        return toJson(courses);
    }

    @Override
    public String compareCourses(String courseIds) {
        try {
            if (courseIds == null || courseIds.isBlank()) return "courseIds 不能为空";
            List<Map<String, Object>> results = new ArrayList<>();
            for (String id : courseIds.split(",")) {
                Long cid = parseLong(id.trim(), "courseId");
                CourseInfoVo info = courseService.getCourseWithDept(cid);
                List<Feedback> feedbacks = feedbackService.listByCourseId(cid);
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("courseInfo", info);
                item.put("feedbackCount", feedbacks.size());
                item.put("scoreSummary", buildScoreSummary(feedbacks));
                results.add(item);
            }
            return toJson(results);
        } catch (IllegalArgumentException e) { return e.getMessage(); }
    }

    @Override
    public String queryDepartmentInfo(String deptId) {
        try {
            Long did = parseLong(deptId, "deptId");
            Department d = departmentService.getById(did);
            return d == null ? "未找到院系信息" : toJson(d);
        } catch (IllegalArgumentException e) { return e.getMessage(); }
    }

    @Override
    public String generateCourseReport(String courseId) {
        try {
            Long cid = parseLong(courseId, "courseId");
            CourseInfoVo info = courseService.getCourseWithDept(cid);
            List<Feedback> all = feedbackService.listByCourseId(cid);
            List<Suggestion> suggestions = suggestionService.listByCourseId(cid);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("courseInfo", info);
            body.put("totalFeedback", all.size());
            body.put("scoreSummary", buildScoreSummary(all));
            body.put("suggestions", suggestions);
            return toJson(body);
        } catch (IllegalArgumentException e) { return e.getMessage(); }
    }

    @Override
    public String queryFAQ(String category) {
        String cat = category != null ? category.trim() : null;
        if (cat != null && cat.isEmpty()) cat = null;
        List<Faq> list = faqMapper.selectByCategoryOptional(cat);
        return toJson(list);
    }

    @Override
    public String detectAnomalies(String courseId) {
        // 简化实现：查找某维度评分骤降的情况
        try {
            Long cid = parseLong(courseId, "courseId");
            List<Feedback> all = feedbackService.listByCourseId(cid);
            Map<String, List<Feedback>> byDimension = all.stream()
                    .collect(Collectors.groupingBy(Feedback::getDimension));
            List<Map<String, Object>> anomalies = new ArrayList<>();
            for (var entry : byDimension.entrySet()) {
                var byDate = entry.getValue().stream()
                        .collect(Collectors.groupingBy(f -> f.getFeedbackDate().toString()));
                if (byDate.size() < 2) continue;
                double firstAvg = byDate.values().stream().findFirst().orElse(List.of()).stream()
                        .mapToInt(Feedback::getScore).average().orElse(0);
                double lastAvg = new ArrayList<>(byDate.values()).get(byDate.size()-1).stream()
                        .mapToInt(Feedback::getScore).average().orElse(0);
                if (lastAvg < firstAvg - 1.0) {
                    anomalies.add(Map.of("dimension", entry.getKey(), "drop", firstAvg - lastAvg, "firstAvg", firstAvg, "lastAvg", lastAvg));
                }
            }
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("courseId", cid);
            body.put("anomalies", anomalies);
            return toJson(body);
        } catch (IllegalArgumentException e) { return e.getMessage(); }
    }

    @Override
    public String queryParticipationTrend(String courseId, String weeks) {
        try {
            Long cid = parseLong(courseId, "courseId");
            int w = parseIntOrDefault(weeks, 4, 90);
            LocalDate end = LocalDate.now();
            LocalDate start = end.minusWeeks(w);
            List<Feedback> rows = feedbackService.listByCourseAndDateRange(cid, start, end);
            Map<String, Long> byWeek = rows.stream()
                    .collect(Collectors.groupingBy(
                            f -> f.getFeedbackDate().toString(),
                            Collectors.counting()));
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("courseId", cid);
            body.put("weeks", w);
            body.put("participationByDate", byWeek);
            return toJson(body);
        } catch (IllegalArgumentException e) { return e.getMessage(); }
    }

    @Override
    public String searchFeedbackContent(String courseId, String keyword) {
        try {
            Long cid = parseLong(courseId, "courseId");
            if (keyword == null || keyword.isBlank()) return "keyword 不能为空";
            List<Feedback> all = feedbackService.listByCourseId(cid);
            List<Feedback> matched = all.stream()
                    .filter(f -> f.getComment() != null && f.getComment().contains(keyword))
                    .toList();
            return toJson(matched);
        } catch (IllegalArgumentException e) { return e.getMessage(); }
    }

    // ==================== 学生端工具实现 ====================

    @Override
    public String getCurrentStudentContext() {
        String username = resolveJwtSubjectFromRequest();
        if (username == null) return "当前请求未携带有效 JWT";
        Long sid = studentMapper.selectIdByUsername(username);
        if (sid == null) return "未找到对应学生账号";
        Student student = studentMapper.selectById(sid);
        if (student == null) return "未找到学生信息";
        List<Long> courseIds = enrollmentService.listCourseIdsByStudentId(sid);
        List<CourseInfoVo> courses = courseIds.stream()
                .map(courseService::getCourseWithDept)
                .filter(Objects::nonNull)
                .toList();
        StudentSessionVo vo = new StudentSessionVo();
        vo.setStudentId(sid);
        vo.setLoginUsername(username);
        vo.setFullName(student.getFullName());
        vo.setStudentNo(student.getStudentNo());
        vo.setCourses(courses);
        return toJson(vo);
    }

    @Override
    public String queryMyFeedbackHistory(String courseId) {
        Long sid = resolveCurrentStudentIdFromJwt();
        if (sid == null) return "无法解析当前学生身份";
        try {
            if (courseId != null && !courseId.isBlank()) {
                Long cid = parseLong(courseId, "courseId");
                return toJson(feedbackService.listByStudentAndCourse(sid, cid));
            }
            return toJson(feedbackService.listByStudentId(sid));
        } catch (IllegalArgumentException e) { return e.getMessage(); }
    }

    @Override
    public String queryMyCourseInfo(String courseId) {
        try {
            Long cid = parseLong(courseId, "courseId");
            CourseInfoVo vo = courseService.getCourseWithDept(cid);
            return vo == null ? "未找到课程信息" : toJson(vo);
        } catch (IllegalArgumentException e) { return e.getMessage(); }
    }

    @Override
    public String submitFeedback(String courseId, String dimension, String score, String comment) {
        Long sid = resolveCurrentStudentIdFromJwt();
        if (sid == null) return "无法解析当前学生身份";
        try {
            Long cid = parseLong(courseId, "courseId");
            int s = Integer.parseInt(score);
            if (s < 1 || s > 5) return "score 必须在 1-5 之间";
            Feedback f = new Feedback();
            f.setCourseId(cid);
            f.setStudentId(sid);
            f.setFeedbackDate(LocalDate.now());
            f.setDimension(dimension);
            f.setScore(s);
            f.setComment(comment);
            f.setIsAnalyzed(false);
            feedbackService.add(f);
            return toJson(Map.of("success", true, "message", "反馈提交成功"));
        } catch (IllegalArgumentException e) { return e.getMessage(); }
    }

    @Override
    public String queryMyFeedbackSummary() {
        Long sid = resolveCurrentStudentIdFromJwt();
        if (sid == null) return "无法解析当前学生身份";
        List<Feedback> all = feedbackService.listByStudentId(sid);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("totalFeedbacks", all.size());
        if (!all.isEmpty()) {
            body.put("scoreSummary", buildScoreSummary(all));
        }
        return toJson(body);
    }

    @Override
    public String queryMyDepartmentInfo() {
        Long sid = resolveCurrentStudentIdFromJwt();
        if (sid == null) return "无法解析当前学生身份";
        Student student = studentMapper.selectById(sid);
        if (student == null || student.getDeptId() == null) return "未找到院系信息";
        Department d = departmentService.getById(student.getDeptId());
        return d == null ? "未找到院系信息" : toJson(d);
    }

    // ==================== 通用统计 ====================

    private Map<String, Object> buildScoreSummary(List<Feedback> feedbacks) {
        if (feedbacks == null || feedbacks.isEmpty()) return Map.of("note", "暂无数据");
        Map<String, Double> byDimension = feedbacks.stream()
                .collect(Collectors.groupingBy(
                        Feedback::getDimension,
                        Collectors.averagingInt(Feedback::getScore)));
        double overall = feedbacks.stream().mapToInt(Feedback::getScore).average().orElse(0);
        Map<String, Object> summary = new LinkedHashMap<>(byDimension);
        summary.put("overallAverage", Math.round(overall * 100.0) / 100.0);
        summary.put("totalCount", feedbacks.size());
        return summary;
    }
}
