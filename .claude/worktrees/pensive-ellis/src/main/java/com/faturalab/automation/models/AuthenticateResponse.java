package com.faturalab.automation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthenticateResponse {
    
    @JsonProperty("sessionId")
    private String sessionId;
    
    @JsonProperty("message")
    private String message;
    
    public AuthenticateResponse() {}
    
    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public String toString() {
        return "AuthenticateResponse{" +
                "sessionId='" + sessionId + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
} 