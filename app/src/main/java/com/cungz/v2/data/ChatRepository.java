package com.cungz.v2.data;

import android.content.Context;
import androidx.room.Room;
import java.util.List;
import java.util.concurrent.Executors;

public class ChatRepository {
    private final AppDatabase db;
    public ChatRepository(Context context) {
        db = Room.databaseBuilder(context, AppDatabase.class, "chat_db").build();
    }
    public void insert(ChatHistory chat) {
        Executors.newSingleThreadExecutor().execute(() -> db.chatHistoryDao().insert(chat));
    }
    public void deleteAll() {
        Executors.newSingleThreadExecutor().execute(() -> db.chatHistoryDao().deleteAll());
    }
    public List<ChatHistory> getAll() {
        return db.chatHistoryDao().getAll();
    }
}
