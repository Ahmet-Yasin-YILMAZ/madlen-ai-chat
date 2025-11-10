package com.example.bootcamptoprod.dto;

public class ChatResponse {
    private String sessionId;
    private String response;

    public ChatResponse(String sessionId, String response) {
        this.sessionId = sessionId;
        this.response = response;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getResponse() {
        return response;
    }
}
