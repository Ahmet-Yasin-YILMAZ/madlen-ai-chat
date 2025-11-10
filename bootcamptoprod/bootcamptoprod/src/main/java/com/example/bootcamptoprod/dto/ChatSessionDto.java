package com.example.bootcamptoprod.dto;

import java.time.Instant;

public class ChatSessionDto {
    private String id;
    private String name;
    private Instant createdAt;

    public ChatSessionDto(String id, String name, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Instant getCreatedAt() { return createdAt; }
}
