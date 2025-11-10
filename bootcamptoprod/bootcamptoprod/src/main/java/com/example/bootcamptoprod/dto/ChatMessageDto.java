package com.example.bootcamptoprod.dto;

import java.time.Instant;

public class ChatMessageDto {
    private Long id;
    private String sender;
    private String content;
    private Instant createdAt;

    public ChatMessageDto(Long id, String sender, String content, Instant createdAt) {
        this.id = id;
        this.sender = sender;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getSender() { return sender; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
}
