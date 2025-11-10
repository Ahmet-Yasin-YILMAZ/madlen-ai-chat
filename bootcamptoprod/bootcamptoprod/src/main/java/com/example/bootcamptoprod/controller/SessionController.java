package com.example.bootcamptoprod.controller;

import com.example.bootcamptoprod.entity.ChatSession;
import com.example.bootcamptoprod.repository.ChatSessionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.Instant;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final ChatSessionRepository sessionRepository;

    public SessionController(ChatSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @GetMapping
    public List<com.example.bootcamptoprod.dto.ChatSessionDto> list() {
        return sessionRepository.findAll().stream()
                .map(s -> new com.example.bootcamptoprod.dto.ChatSessionDto(
                        s.getId(),
                        s.getName(),
                        s.getCreatedAt()
                ))
                .toList();
    }


    @PatchMapping("/{id}")
    public ResponseEntity<?> rename(@PathVariable String id, @RequestBody RenameRequest req) {
        return sessionRepository.findById(id)
                .map(s -> {
                    s.setName(req.name());
                    sessionRepository.save(s);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<com.example.bootcamptoprod.dto.ChatSessionDto> create() {
        ChatSession s = new ChatSession();
        s.setName("Yeni Oturum");
        s.setCreatedAt(Instant.now());
        ChatSession saved = sessionRepository.save(s);

        return ResponseEntity.ok(
                new com.example.bootcamptoprod.dto.ChatSessionDto(
                        saved.getId(),
                        saved.getName(),
                        saved.getCreatedAt()
                )
        );
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        if (sessionRepository.existsById(id)) {
            sessionRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<com.example.bootcamptoprod.dto.ChatMessageDto>> messages(@PathVariable String id) {
        return sessionRepository.findById(id)
                .map(s -> ResponseEntity.ok(
                        s.getMessages()
                                .stream()
                                .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                                .map(m -> new com.example.bootcamptoprod.dto.ChatMessageDto(
                                        m.getId(),
                                        m.getSender(),
                                        m.getContent(),
                                        m.getCreatedAt()
                                ))
                                .toList()
                ))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    public record RenameRequest(String name) {}

    public record ChatMessageDto(
            Long id,
            String sender,
            String content,
            java.time.Instant createdAt
    ) {}
}
