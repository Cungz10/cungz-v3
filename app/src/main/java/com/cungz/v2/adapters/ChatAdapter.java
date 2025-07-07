package com.cungz.v2.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.cungz.v2.R;
import com.cungz.v2.models.ChatMessage;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<ChatMessage> chatList;
    private static final int TYPE_USER = 0;
    private static final int TYPE_AI = 1;
    private static final int TYPE_LOADING = 2;

    public ChatAdapter(List<ChatMessage> chatList) {
        this.chatList = chatList;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = chatList.get(position);
        if (msg.isLoading) return TYPE_LOADING;
        return msg.isUser ? TYPE_USER : TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_USER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(v);
        } else if (viewType == TYPE_AI) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_ai, parent, false);
            return new AIViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_loading, parent, false);
            return new LoadingViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = chatList.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).msg.setText(msg.message);
        } else if (holder instanceof AIViewHolder) {
            ((AIViewHolder) holder).msg.setText(msg.message);
        }
        // LoadingViewHolder: nothing to bind
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView msg;
        ImageView avatar;
        UserViewHolder(View v) {
            super(v);
            msg = v.findViewById(R.id.chat_user_message);
            avatar = v.findViewById(R.id.chat_user_avatar);
        }
    }
    static class AIViewHolder extends RecyclerView.ViewHolder {
        TextView msg;
        ImageView avatar;
        AIViewHolder(View v) {
            super(v);
            msg = v.findViewById(R.id.chat_ai_message);
            avatar = v.findViewById(R.id.chat_ai_avatar);
        }
    }
    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        ProgressBar bar;
        LoadingViewHolder(View v) {
            super(v);
            bar = v.findViewById(R.id.chat_loading_bar);
        }
    }
}
