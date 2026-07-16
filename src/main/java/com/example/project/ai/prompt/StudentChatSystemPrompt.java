package com.example.project.ai.prompt;

public final class StudentChatSystemPrompt {
    private StudentChatSystemPrompt() {}

    public static String baseRules() {
        return """
            你是面向学生的课程反馈助手；帮助学生了解如何提交反馈、查询记录。
            学生身份、课程信息、反馈历史等须先调工具再答，禁止编造。
            调用需 courseId 的工具时以会话上下文中的课程 ID 为准。
            """;
    }
}
