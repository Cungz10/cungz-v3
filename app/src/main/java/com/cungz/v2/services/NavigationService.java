package com.cungz.v2.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.cungz.v2.BuildConfig;
import com.cungz.v2.models.RouteInfo;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import okhttp3.*;
import java.io.IOException;

public class NavigationService extends Service {
    private static final String TAG = "NavigationService";
    // Pindahin URL & Key ke BuildConfig biar lebih aman dan fleksibel
    // private static final String ROUTE_API_URL = "YOUR_ROUTE_API_URL";
    // private static final String API_KEY = "YOUR_API_KEY";
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface NavigationCallback {
        void onRouteFound(RouteInfo routeInfo);
        void onNavigationStarted();
        void onNavigationStep(String instruction);
        void onFailure(String error);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Kalo mau bikin service yang bisa di-bind, kita perlu return Binder di sini.
        // Untuk sekarang, return null dulu gapapa.
        return null;
    }

    // Kita buat lebih realistis, biasanya butuh koordinat awal dan akhir.
    public void findRoute(String startCoords, String endCoords, NavigationCallback callback) {
        // Implementasi: Panggil API rute, parsing response jadi RouteInfo, kasih ke callback
        // Pake HttpUrl.Builder biar lebih aman!
        HttpUrl url = HttpUrl.parse(BuildConfig.FLASK_SERVER_URL + "/route") // Asumsi endpoint di server Flask lo
                .newBuilder()
                .addQueryParameter("start", startCoords)
                .addQueryParameter("end", endCoords)
                // API Key bisa juga dikirim via header, lebih secure!
                // .addHeader("X-API-KEY", BuildConfig.YOUR_API_KEY)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error finding route: ", e);
                // Balikin error ke Main Thread biar aman kalo mau update UI
                mainHandler.post(() -> callback.onFailure("Gagal mencari rute: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                // Pake try-with-resources biar response body-nya auto close, lebih hemat memori
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful() || responseBody == null) {
                        String errorBody = responseBody != null ? responseBody.string() : "Unknown error";
                        Log.e(TAG, "API Error: " + response.code() + " - " + errorBody);
                        mainHandler.post(() -> callback.onFailure("Gagal menerima respons rute: " + response.code()));
                        return;
                    }

                    String bodyString = responseBody.string();
                    RouteInfo routeInfo = gson.fromJson(bodyString, RouteInfo.class);
                    // Balikin hasil ke Main Thread
                    mainHandler.post(() -> callback.onRouteFound(routeInfo));
                } catch (JsonSyntaxException e) {
                    Log.e(TAG, "JSON parsing error", e);
                    mainHandler.post(() -> callback.onFailure("Format data rute salah."));
                } catch (IOException e) {
                    Log.e(TAG, "Response body reading error", e);
                    mainHandler.post(() -> callback.onFailure("Gagal membaca respons rute."));
                }
            }
        });
    }
}
