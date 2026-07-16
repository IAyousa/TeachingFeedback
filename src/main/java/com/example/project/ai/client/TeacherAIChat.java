package com.example.project.ai.client;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import com.example.project.ai.prompt.TeachingPromptTemplate;
import com.example.project.ai.tools.TeacherAITool;

@Component
public class TeacherAIChat {

    @Autowired
    private TeacherAITool teacherAITool;

    @Autowired
    private TeachingPromptTemplate teachingPromptTemplate;

    private final ChatClient chatClient;

    public TeacherAIChat(ChatClient.Builder chatClient,
                         Advisor retrievalAugmentationAdvisor,
                         MessageChatMemoryAdvisor messageChatMemoryAdvisor) {
        this.chatClient = chatClient
                .defaultAdvisors(retrievalAugmentationAdvisor, messageChatMemoryAdvisor)
                .build();
    }

    public Flux<String> stream(Long userId, String timeId, String message) {
        var parts = teachingPromptTemplate.renderParts(userId, null);
        String userTurn = parts.combinedUserMessage(message);
        return chatClient.prompt()
                .system(parts.systemPrompt())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, timeId))
                .tools(teacherAITool)
                .user(userTurn)
                .stream()
                .content();
    }
}
