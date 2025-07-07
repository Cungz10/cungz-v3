package com.cungz.v2.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_history")
public class ChatHistory {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String sender; // "AI" atau "User"
    public String message;
    public long timestamp;

    public ChatHistory(String sender, String message, long timestamp) {
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
    }
}
