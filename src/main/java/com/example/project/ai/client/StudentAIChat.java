package com.example.project.ai.client;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.example.project.ai.prompt.StudentPromptTemplate;
import com.example.project.ai.tools.StudentAITool;
import com.example.project.service.StudentAuthService;
import com.example.project.service.impl.TeachingAiDataServiceImpl;

import reactor.core.publisher.Flux;

@Component
public class StudentAIChat {

    @Autowired
    private StudentAITool studentAITool;

    @Autowired
    private StudentAuthService studentAuthService;

    @Autowired
    private StudentPromptTemplate studentPromptTemplate;

    private final ChatClient chatClient;

    public StudentAIChat(ChatClient.Builder chatClient,
                         Advisor retrievalAugmentationAdvisor,
                         MessageChatMemoryAdvisor messageChatMemoryAdvisor) {
        this.chatClient = chatClient
                .defaultAdvisors(retrievalAugmentationAdvisor, messageChatMemoryAdvisor)
                .build();
    }

    public Flux<String> stream(String timeId, String message, String jwtSubject) {
        String loginUsername = StringUtils.hasText(jwtSubject) ? jwtSubject.trim() : "（未登录）";
        Long studentId = studentAuthService.resolveStudentUserId(loginUsername);

        // 由 StudentPromptTemplate 统一处理上下文构建（结构对标教师端的 TeachingPromptTemplate）
        var parts = studentPromptTemplate.renderParts(studentId, loginUsername);
        String userMessage = parts.combinedUserMessage(message);

        return chatClient.prompt()
                .system(parts.systemPrompt())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, timeId))
                .tools(studentAITool)
                .user(userMessage)
                .stream()
                .content()
                .doOnSubscribe(s -> TeachingAiDataServiceImpl.setCurrentJwtFallback(loginUsername))
                .doFinally(signalType -> TeachingAiDataServiceImpl.clearCurrentJwtFallback());
    }
}
