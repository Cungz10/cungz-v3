package com.cungz.v2.models;

public class ChatMessage {
    public String message;
    public boolean isUser;
    public long timestamp;
    public boolean isLoading; // true kalau ini bubble loading

    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
        this.timestamp = System.currentTimeMillis();
        this.isLoading = false;
    }
    public ChatMessage(String message, boolean isUser, boolean isLoading) {
        this.message = message;
        this.isUser = isUser;
        this.timestamp = System.currentTimeMillis();
        this.isLoading = isLoading;
    }
}
