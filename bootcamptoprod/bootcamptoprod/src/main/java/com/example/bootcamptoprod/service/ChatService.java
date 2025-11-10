package com.example.bootcamptoprod.service;

import com.example.bootcamptoprod.dto.ChatRequest;
import com.example.bootcamptoprod.dto.ChatResponse;
import com.example.bootcamptoprod.entity.ChatMessage;
import com.example.bootcamptoprod.entity.ChatSession;
import com.example.bootcamptoprod.repository.ChatMessageRepository;
import com.example.bootcamptoprod.repository.ChatSessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;

@Service
public class ChatService {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    // application.properties'e bunu ekleyebilirsin:
    // openrouter.api.key=sk-....
    @Value("${openrouter.api.key:}")
    private String openRouterApiKey;

    @Value("${openrouter.api.url:https://openrouter.ai/api/v1/chat/completions}")
    private String openRouterUrl;

    public ChatService(ChatSessionRepository sessionRepository,
                       ChatMessageRepository messageRepository) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
    }

    public ChatResponse processMessage(ChatRequest req) {
        try {
            // 1) oturumu bul / oluştur
            ChatSession session = findOrCreateSession(req.getSessionId());

            // 2) kullanıcı mesajını kaydet
            ChatMessage userMsg = new ChatMessage();
            userMsg.setSender("USER");
            userMsg.setContent(req.getMessage());
            userMsg.setCreatedAt(Instant.now());
            userMsg.setSession(session);
            messageRepository.save(userMsg);

            // 3) OpenRouter'a git
            String model = (req.getModel() == null || req.getModel().isBlank())
                    ? "gpt-3.5-turbo" // burada ücretsiz bir modeli sen seçersin
                    : req.getModel();

            String assistantReply = callOpenRouter(model, req.getMessage());

            // 4) asistan mesajını kaydet
            ChatMessage assistantMsg = new ChatMessage();
            assistantMsg.setSender("ASSISTANT");
            assistantMsg.setContent(assistantReply);
            assistantMsg.setCreatedAt(Instant.now());
            assistantMsg.setSession(session);
            messageRepository.save(assistantMsg);

            // 5) frontend'in beklediği DTO
            return new ChatResponse(session.getId(), assistantReply);

        } catch (Exception e) {
            // buraya düşerse frontend’e saçma yazı dönmesin
            return new ChatResponse(
                    req.getSessionId(),
                    "OpenRouter'a giderken hata oluştu: " + e.getMessage()
            );
        }
    }

    private ChatSession findOrCreateSession(String sessionId) {
        if (sessionId != null && !sessionId.isBlank()) {
            return sessionRepository.findById(sessionId)
                    .orElseGet(() -> {
                        ChatSession s = new ChatSession();
                        s.setName("Yeni Oturum");
                        s.setCreatedAt(Instant.now());
                        return sessionRepository.save(s);
                    });
        }
        ChatSession s = new ChatSession();
        s.setName("Yeni Oturum");
        s.setCreatedAt(Instant.now());
        return sessionRepository.save(s);
    }

    private String callOpenRouter(String model, String userMessage) {
        // API key hiç yoksa buradan dön, senin şu an gördüğün cümle buradan geliyordu
        if (openRouterApiKey == null || openRouterApiKey.isBlank()) {
            return "OpenRouter API key tanımlı değil, bu yüzden gerçek modele gidemedim.";
        }

        // OpenRouter chat/completions payload'ı
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> user = new HashMap<>();
        user.put("role", "user");
        user.put("content", userMessage);
        messages.add(user);

        body.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + openRouterApiKey);
        headers.set("HTTP-Referer", "http://localhost:5173");
        headers.set("X-Title", "Madlen Chat");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> resp = restTemplate.postForEntity(openRouterUrl, entity, Map.class);

        if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
            // OpenRouter genelde OpenAI'ya benzer dönüyor:
            // { choices: [ { message: { content: "..." } } ] }
            Object choicesObj = resp.getBody().get("choices");
            if (choicesObj instanceof List<?> choices && !choices.isEmpty()) {
                Object first = choices.get(0);
                if (first instanceof Map<?,?> f) {
                    Object msgObj = f.get("message");
                    if (msgObj instanceof Map<?,?> m) {
                        Object content = m.get("content");
                        if (content != null) {
                            return content.toString();
                        }
                    }
                }
            }
        }

        return "Modelden cevap çözümlenemedi.";
    }
}
