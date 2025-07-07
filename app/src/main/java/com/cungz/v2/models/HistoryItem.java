package com.cungz.v2.models;

public class HistoryItem {
    private String content;
    private String date;

    public HistoryItem(String content, String date) {
        this.content = content;
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public String getDate() {
        return date;
    }
}
