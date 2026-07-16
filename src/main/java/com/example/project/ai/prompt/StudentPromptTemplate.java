package com.example.project.ai.prompt;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.example.project.pojo.entity.Course;
import com.example.project.service.CourseService;
import com.example.project.service.EnrollmentService;

/**
 * 学生端提示词模板，负责将学生身份和选课信息格式化为 AI 可理解的会话上下文。
 * 结构对标 {@link TeachingPromptTemplate}。
 */
@Component
public class StudentPromptTemplate {

    public record StudentPromptParts(String systemPrompt, String sessionAndToolBlock) {
        public String combinedUserMessage(String userMessage) {
            String body = userMessage == null ? "" : userMessage;
            if (sessionAndToolBlock == null || sessionAndToolBlock.isBlank()) {
                return "【用户消息】\n" + body;
            }
            return sessionAndToolBlock + "\n\n【用户消息】\n" + body;
        }
    }

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private CourseService courseService;

    @Value("classpath:prompt/student-system.st")
    private Resource templateResource;

    /**
     * 渲染学生端的提示词部件。
     *
     * @param studentId     学生主键 ID，为空时标记为未登录
     * @param loginUsername 登录用户名
     * @return systemPrompt + 会话上下文块
     */
    public StudentPromptParts renderParts(Long studentId, String loginUsername) {
        // 渲染系统提示词
        PromptTemplate template = new PromptTemplate(templateResource);
        String system = template.render(Map.of(
                "baseRules", StudentChatSystemPrompt.baseRules(),
                "serverDate", LocalDate.now().toString()));

        // 构建会话上下文
        StringBuilder ctx = new StringBuilder();
        ctx.append("【会话上下文】\n");
        ctx.append("studentId: ").append(studentId == null ? "（未登录）" : studentId).append("\n");
        ctx.append("loginUsername: ").append(loginUsername != null ? loginUsername : "（未登录）").append("\n");
        ctx.append("服务端当日: ").append(LocalDate.now()).append("\n\n");

        // 查询并格式化选课信息
        List<Long> courseIds = (studentId != null)
                ? enrollmentService.listCourseIdsByStudentId(studentId)
                : List.of();

        ctx.append("【已选课程】\n");
        if (courseIds.isEmpty()) {
            ctx.append("当前账号暂无课程数据。\n");
        } else {
            int i = 1;
            for (Long cid : courseIds) {
                Course c = courseService.getById(cid);
                if (c != null) {
                    ctx.append(i++).append(") courseId=").append(c.getId())
                            .append(" 课程=").append(nullToEmpty(c.getCourseName()))
                            .append(" 编号=").append(nullToEmpty(c.getCourseCode()))
                            .append(" 学期=").append(nullToEmpty(c.getSemester()))
                            .append("\n");
                }
            }
        }

        ctx.append("\n");
        // 工具参数指引（同教师端风格）
        if (courseIds.size() == 1) {
            String name = nullToEmpty(courseService.getById(courseIds.get(0)) != null
                    ? courseService.getById(courseIds.get(0)).getCourseName() : "");
            ctx.append("【工具参数】用户所指课程默认对应「").append(name)
                    .append("」。凡工具要求 courseId，必须传入字符串 \"")
                    .append(courseIds.get(0)).append("\"。禁止留空或编造。\n");
        } else if (courseIds.size() > 1) {
            ctx.append("【工具参数】本账号关联多门课程，调用需 courseId 的工具请按用户所指选用：");
            boolean first = true;
            for (Long cid : courseIds) {
                if (!first) ctx.append("；");
                first = false;
                String name = nullToEmpty(courseService.getById(cid) != null
                        ? courseService.getById(cid).getCourseName() : "");
                ctx.append("courseId ").append(cid).append(" 对应 ").append(name);
            }
            ctx.append("\n");
        }

        return new StudentPromptParts(system, ctx.toString().trim());
    }

    private static String nullToEmpty(String s) {
        return s == null || s.isBlank() ? "—" : s;
    }
}
