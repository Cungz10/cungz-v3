package com.cungz.v2.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {ChatHistory.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ChatHistoryDao chatHistoryDao();
}
