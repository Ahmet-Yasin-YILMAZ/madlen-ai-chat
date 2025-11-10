package com.example.bootcamptoprod.dto;

import java.time.Instant;

public class ChatReplyDto {
    private String sessionId;
    private String reply;
    private Instant createdAt;

    public ChatReplyDto(String sessionId, String reply, Instant createdAt) {
        this.sessionId = sessionId;
        this.reply = reply;
        this.createdAt = createdAt;
    }

    public String getSessionId() { return sessionId; }
    public String getReply() { return reply; }
    public Instant getCreatedAt() { return createdAt; }
}
