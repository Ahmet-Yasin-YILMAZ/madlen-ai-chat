package com.example.bootcamptoprod.controller;

import com.example.bootcamptoprod.dto.ChatRequest;
import com.example.bootcamptoprod.dto.ChatResponse;
import com.example.bootcamptoprod.dto.OpenRouterModel;
import com.example.bootcamptoprod.entity.ChatMessage;
import com.example.bootcamptoprod.service.ChatService;
import com.example.bootcamptoprod.service.ModelService;
import com.example.bootcamptoprod.service.ChatHistoryService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final ModelService modelService;
    private final ChatHistoryService chatHistoryService;

    public ChatController(ChatService chatService,
                          ModelService modelService,
                          ChatHistoryService chatHistoryService) {
        this.chatService = chatService;
        this.modelService = modelService;
        this.chatHistoryService = chatHistoryService;
    }

    @PostMapping("/message")
    public ResponseEntity<com.example.bootcamptoprod.dto.ChatReplyDto> sendMessage(
            @RequestBody ChatRequest chatRequest) {

        ChatResponse resp = chatService.processMessage(chatRequest);

        String sessionId = resp.getSessionId();
        String answer = resp.getResponse();  // 👈 burada getAnswer değil getResponse()
        java.time.Instant createdAt = java.time.Instant.now();

        return ResponseEntity.ok(
                new com.example.bootcamptoprod.dto.ChatReplyDto(
                        sessionId,
                        answer,
                        createdAt
                )
        );
    }



    @GetMapping("/models")
    public ResponseEntity<List<OpenRouterModel>> getModels() {
        return ResponseEntity.ok(modelService.getModels());
    }

    @GetMapping("/messages")
    public ResponseEntity<List<com.example.bootcamptoprod.dto.ChatMessageDto>> getMessages(
            @RequestParam("sessionId") String sessionId) {

        var messages = chatHistoryService.getMessagesForSession(sessionId)
                .stream()
                .map(m -> new com.example.bootcamptoprod.dto.ChatMessageDto(
                        m.getId(),
                        m.getSender(),
                        m.getContent(),
                        m.getCreatedAt()
                ))
                .toList();

        return ResponseEntity.ok(messages);
    }



    @PostMapping(
            value = "/message-with-file",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ChatResponse> sendMessageWithFile(
            @RequestPart("message") String message,
            @RequestPart(value = "model", required = false) String model,
            @RequestPart(value = "sessionId", required = false) String sessionId,
            @RequestPart("file") MultipartFile file
    ) {
        String extra = " [Kullanıcı dosya yükledi: " + file.getOriginalFilename() + "]";
        ChatRequest req = new ChatRequest();
        req.setMessage(message + extra);
        req.setModel(model);
        req.setSessionId(sessionId);

        ChatResponse resp = chatService.processMessage(req);
        return ResponseEntity.ok(resp);
    }
}
