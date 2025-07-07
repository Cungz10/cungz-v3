package com.cungz.v2.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface ChatHistoryDao {
    @Insert
    void insert(ChatHistory chat);

    @Query("SELECT * FROM chat_history ORDER BY timestamp ASC")
    List<ChatHistory> getAll();

    @Query("DELETE FROM chat_history")
    void deleteAll();
}
