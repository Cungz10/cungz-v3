
package com.cungz.v2.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cungz.v2.R;
import com.cungz.v2.adapters.ChatAdapter;
import com.cungz.v2.models.ChatMessage;
import com.cungz.v2.services.AIService;
import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.lifecycle.ViewModelProvider;
import com.cungz.v2.data.ChatViewModel;
import com.cungz.v2.data.ChatHistory;
import com.cungz.v2.utils.MemoryManager;
import java.util.concurrent.Executors;

public class AIFragment extends Fragment {
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatList = new ArrayList<>();
    private AIService aiService;
    private ChatViewModel chatViewModel;
    private MemoryManager memoryManager = new MemoryManager();
    private Handler handler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.ai_chat_recycler);
        EditText input = view.findViewById(R.id.ai_input);
        ImageButton sendBtn = view.findViewById(R.id.ai_send_btn);
        chatAdapter = new ChatAdapter(chatList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(chatAdapter);
        aiService = new AIService();

        // ViewModel buat chat history
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        chatViewModel.getChats().observe(getViewLifecycleOwner(), chats -> {
            chatList.clear();
            for (ChatHistory c : chats) {
                chatList.add(new ChatMessage(c.message, "User".equals(c.sender)));
                memoryManager.addMessage(c.sender, c.message);
            }
            chatAdapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(chatList.size() - 1);
        });

        // Kirim pesan
        sendBtn.setOnClickListener(v -> {
            String text = input.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                ChatMessage userMsg = new ChatMessage(text, true);
                chatList.add(userMsg);
                chatAdapter.notifyItemInserted(chatList.size() - 1);
                recyclerView.scrollToPosition(chatList.size() - 1);
                input.setText("");
                chatViewModel.insert(new ChatHistory("User", text, System.currentTimeMillis()));
                memoryManager.addMessage("User", text);
                // Tampilkan loading bubble
                ChatMessage loading = new ChatMessage("", false, true);
                chatList.add(loading);
                chatAdapter.notifyItemInserted(chatList.size() - 1);
                recyclerView.scrollToPosition(chatList.size() - 1);

                // Ambil context 5-10 chat terakhir
                List<String> context = memoryManager.getContext();
                aiService.sendMessageWithContext(text, context, new AIService.AIChatCallback() {
                    @Override
                    public void onResponse(ChatMessage response) {
                        if (getActivity() == null) return;
                        handler.post(() -> {
                            // Hapus loading
                            if (!chatList.isEmpty() && chatList.get(chatList.size() - 1).isLoading) {
                                chatList.remove(chatList.size() - 1);
                                chatAdapter.notifyItemRemoved(chatList.size());
                            }
                            chatList.add(response);
                            chatAdapter.notifyItemInserted(chatList.size() - 1);
                            recyclerView.scrollToPosition(chatList.size() - 1);
                            chatViewModel.insert(new ChatHistory("AI", response.message, System.currentTimeMillis()));
                            memoryManager.addMessage("AI", response.message);
                        });
                    }
                    @Override
                    public void onFailure(String error) {
                        if (getActivity() == null) return;
                        handler.post(() -> {
                            if (!chatList.isEmpty() && chatList.get(chatList.size() - 1).isLoading) {
                                chatList.remove(chatList.size() - 1);
                                chatAdapter.notifyItemRemoved(chatList.size());
                            }
                            Toast.makeText(getContext(), "AI gagal bales: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            } else {
                Toast.makeText(getContext(), "Tulis pesan dulu bro!", Toast.LENGTH_SHORT).show();
            }
        });

        // Clear chat history (long click title)
        view.findViewById(R.id.ai_title).setOnLongClickListener(v -> {
            new android.app.AlertDialog.Builder(getContext())
                .setTitle("Clear chat?")
                .setMessage("Yakin mau hapus semua chat?")
                .setPositiveButton("Hapus", (d, w) -> {
                    chatViewModel.clear();
                    memoryManager.reset();
                    chatList.clear();
                    chatAdapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Chat history direset!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
            return true;
        });

        return view;
    }
}
