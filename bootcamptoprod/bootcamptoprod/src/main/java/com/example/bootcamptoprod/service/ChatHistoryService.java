package com.example.bootcamptoprod.service;

import com.example.bootcamptoprod.entity.ChatMessage;
import com.example.bootcamptoprod.entity.ChatSession;
import com.example.bootcamptoprod.repository.ChatMessageRepository;
import com.example.bootcamptoprod.repository.ChatSessionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ChatHistoryService {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;

    public ChatHistoryService(ChatSessionRepository sessionRepository,
                              ChatMessageRepository messageRepository) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
    }

    public String appendUserMessage(String sessionId, String message) {
        ChatSession session;

        if (sessionId == null || sessionId.isBlank()) {
            session = new ChatSession();
            sessionRepository.save(session);
        } else {
            session = sessionRepository.findById(sessionId)
                    .orElseGet(() -> {
                        ChatSession s = new ChatSession();
                        sessionRepository.save(s);
                        return s;
                    });
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSender("USER");
        chatMessage.setContent(message);
        chatMessage.setCreatedAt(Instant.now());
        chatMessage.setSession(session);
        messageRepository.save(chatMessage);

        return session.getId();
    }

    public void appendAssistantMessage(String sessionId, String message) {
        ChatSession session = sessionRepository.findById(sessionId).orElseThrow();

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSender("ASSISTANT");
        chatMessage.setContent(message);
        chatMessage.setCreatedAt(Instant.now());
        chatMessage.setSession(session);
        messageRepository.save(chatMessage);
    }

    public List<ChatMessage> getMessagesForSession(String sessionId) {
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }
}
