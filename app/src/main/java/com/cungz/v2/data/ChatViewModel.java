package com.cungz.v2.data;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;
import java.util.concurrent.Executors;

public class ChatViewModel extends AndroidViewModel {
    private final ChatRepository repo;
    private final MutableLiveData<List<ChatHistory>> chatList = new MutableLiveData<>();

    public ChatViewModel(@NonNull Application app) {
        super(app);
        repo = new ChatRepository(app.getApplicationContext());
        loadChats();
    }
    public LiveData<List<ChatHistory>> getChats() { return chatList; }
    public void loadChats() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ChatHistory> data = repo.getAll();
            chatList.postValue(data);
        });
    }
    public void insert(ChatHistory chat) {
        repo.insert(chat);
        loadChats();
    }
    public void clear() {
        repo.deleteAll();
        loadChats();
    }
}
