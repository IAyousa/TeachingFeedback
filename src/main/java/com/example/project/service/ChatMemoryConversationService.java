package com.example.project.service;

import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatMemoryConversationService {

    private final ChatMemoryRepository chatMemoryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public ChatMemoryConversationService(ChatMemoryRepository chatMemoryRepository) {
        this.chatMemoryRepository = chatMemoryRepository;
    }

    /**
     * 按会话 ID 删除 JDBC 表 {@code spring_ai_chat_memory} 中该会话的全部消息。
     *
     * @param conversationId 会话 ID
     * @return 实际删除的消息条数；0 表示该会话不存在
     */
    public int deleteByConversationId(String conversationId) {
        String sql = "DELETE FROM spring_ai_chat_memory WHERE conversation_id = ?";
        return jdbcTemplate.update(sql, conversationId.trim());
    }
}
