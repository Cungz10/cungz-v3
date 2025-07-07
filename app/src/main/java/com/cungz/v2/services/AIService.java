package com.cungz.v2.services;

import android.util.Log;
import com.cungz.v2.models.ChatMessage;
import com.google.gson.Gson;
import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;

import com.cungz.v2.BuildConfig;

public class AIService {
    private static final String TAG = "AIService";
    private static final String API_URL = BuildConfig.FLASK_SERVER_URL + "/ai";
    private static final String API_KEY = BuildConfig.OPENAI_API_KEY;
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    public interface AIChatCallback {
        void onResponse(ChatMessage response);
        void onFailure(String error);
    }

    public void sendMessage(String message, AIChatCallback callback) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("message", message);
            RequestBody body = RequestBody.create(requestBody.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .post(body)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure("Gagal mengirim pesan: " + e.getMessage());
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        callback.onFailure("Gagal menerima respons: " + response.code());
                        return;
                    }
                    String resp = response.body().string();
                    try {
                        JSONObject obj = new JSONObject(resp);
                        String aiMsg = obj.optString("reply", "(AI nggak bales bro)");
                        callback.onResponse(new ChatMessage(aiMsg, false));
                    } catch (Exception e) {
                        callback.onFailure("Parse error: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            callback.onFailure("Terjadi kesalahan: " + e.getMessage());
        }
    }
}
