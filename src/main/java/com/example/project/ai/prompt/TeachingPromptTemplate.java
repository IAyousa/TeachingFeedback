package com.example.project.ai.prompt;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.example.project.pojo.entity.Course;
import com.example.project.service.CourseService;

@Slf4j
@Component
public class TeachingPromptTemplate {

    public record TeachingPromptParts(String systemPrompt, String sessionAndToolBlock) {
        public String combinedUserMessage(String userMessage) {
            String body = userMessage == null ? "" : userMessage;
            if (sessionAndToolBlock == null || sessionAndToolBlock.isBlank()) {
                return "【用户消息】\n" + body;
            }
            return sessionAndToolBlock + "\n\n【用户消息】\n" + body;
        }
    }

    @Autowired
    private CourseService courseService;

    @Value("classpath:prompt/teacher-system.st")
    private Resource templateResource;

    public TeachingPromptParts renderParts(Long userId, String extraContext) {
        List<Course> courses = userId == null ? List.of() : courseService.listAll();
        String courseBlock = formatCourseBlock(courses);

        PromptTemplate template = new PromptTemplate(templateResource);
        String system = template.render(Map.of("baseRules", TeacherChatSystemPrompt.baseRules()));

        StringBuilder userContext = new StringBuilder();
        userContext.append("【会话上下文】\n");
        userContext.append("teacherId: ").append(userId == null ? "（未登录）" : userId).append("\n");
        userContext.append("服务端当日: ").append(LocalDate.now()).append("\n\n");
        userContext.append(courseBlock);
        if (extraContext != null && !extraContext.isBlank()) {
            userContext.append("\n\n").append(extraContext.trim());
        }
        userContext.append("\n\n").append(buildToolParameterPrefix(courses));
        return new TeachingPromptParts(system, userContext.toString());
    }

    private static String formatCourseBlock(List<Course> courses) {
        StringBuilder sb = new StringBuilder("【关联课程】\n");
        if (courses == null || courses.isEmpty()) {
            sb.append("当前账号暂无课程数据。");
            return sb.toString();
        }
        int i = 1;
        for (Course c : courses) {
            sb.append(i++).append(") courseId=").append(c.getId())
                    .append(" 课程=").append(nullToEmpty(c.getCourseName()))
                    .append(" 编号=").append(nullToEmpty(c.getCourseCode()))
                    .append(" 学期=").append(nullToEmpty(c.getSemester()))
                    .append(" 选课=").append(c.getStudentCount()).append("人\n");
        }
        return sb.toString().trim();
    }

    private static String buildToolParameterPrefix(List<Course> courses) {
        if (courses == null || courses.isEmpty()) {
            return "【工具参数】当前账号暂无课程，调用需要 courseId 的工具时勿虚构 ID。";
        }
        if (courses.size() == 1) {
            Course c = courses.get(0);
            return "【工具参数】用户所指课程默认对应「" + nullToEmpty(c.getCourseName()) + "」。"
                    + "凡工具要求 courseId，必须传入字符串 \"" + c.getId() + "\"。禁止留空或编造。";
        }
        StringBuilder sb = new StringBuilder("【工具参数】本账号关联多门课程，调用需 courseId 的工具请按用户所指选用：");
        for (int i = 0; i < courses.size(); i++) {
            Course c = courses.get(i);
            if (i > 0) sb.append("；");
            sb.append("courseId ").append(c.getId()).append(" 对应 ").append(nullToEmpty(c.getCourseName()));
        }
        return sb.toString();
    }

    private static String nullToEmpty(String s) { return s == null || s.isBlank() ? "—" : s; }
}
