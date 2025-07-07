package com.cungz.v2.utils;

import java.util.LinkedList;
import java.util.List;

public class MemoryManager {
    private final LinkedList<String> contextList = new LinkedList<>();
    private final int maxContext = 10;

    public void addMessage(String sender, String message) {
        contextList.add(sender + ": " + message);
        if (contextList.size() > maxContext) contextList.removeFirst();
    }
    public List<String> getContext() {
        return new LinkedList<>(contextList);
    }
    public void reset() {
        contextList.clear();
    }
}
