package com.example.project.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.project.ai.client.TeacherAIChat;
import com.example.project.service.ChatMemoryConversationService;
import com.example.project.service.TeacherService;

import reactor.core.Disposable;

@RestController
public class TeacherChatController {

    @Autowired
    private TeacherAIChat teacherAIChat;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private ChatMemoryConversationService chatMemoryConversationService;

    private static final long CHAT_SSE_TIMEOUT_MS = 600_000L;

    @GetMapping(value = "/teacher/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestAttribute("userId") String jwtSubject,
                           @RequestParam("timeId") String timeId,
                           @RequestParam("message") String message) {
        if (!StringUtils.hasText(message)) {
            SseEmitter emitter = new SseEmitter(0L);
            try { emitter.send(SseEmitter.event().data("消息内容不能为空", MediaType.TEXT_PLAIN)); } catch (IOException ignored) {}
            emitter.complete();
            return emitter;
        }
        Long userId = teacherService.resolveTeacherUserId(jwtSubject);
        SseEmitter emitter = new SseEmitter(CHAT_SSE_TIMEOUT_MS);
        MediaType textUtf8 = new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8);

        AtomicReference<Disposable> subscriptionRef = new AtomicReference<>();
        Disposable subscription = teacherAIChat.stream(userId, timeId, message)
                .subscribe(chunk -> {
                    try { emitter.send(SseEmitter.event().data(chunk, textUtf8)); }
                    catch (IOException e) {
                        Disposable d = subscriptionRef.get();
                        if (d != null) d.dispose();
                        emitter.completeWithError(e);
                    }
                }, emitter::completeWithError, emitter::complete);
        subscriptionRef.set(subscription);

        emitter.onCompletion(subscription::dispose);
        emitter.onTimeout(() -> { subscription.dispose(); emitter.complete(); });
        return emitter;
    }

    @DeleteMapping("/teacher/chat/memory")
    public ResponseEntity<Void> deleteChatMemory(@RequestParam("timeId") String timeId) {
        if (!StringUtils.hasText(timeId)) return ResponseEntity.badRequest().build();
        int deleted = chatMemoryConversationService.deleteByConversationId(timeId);
        return deleted > 0 ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
