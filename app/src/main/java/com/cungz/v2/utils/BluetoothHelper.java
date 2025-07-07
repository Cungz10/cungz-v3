package com.cungz.v2.utils;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothHelper {
    private static final String TAG = "BluetoothHelper";
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private Handler handler = new Handler();
    private Context context;
    private BluetoothDevice connectedDevice;
    private static final UUID ESP32_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Ganti sesuai UUID ESP32

    public BluetoothHelper(Context context) {
        this.context = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    // 1️⃣ Scan device
    public void scanDevices(Activity activity, DeviceScanCallback callback) {
        Dexter.withContext(context)
                .withPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                        callback.onDevicesFound(pairedDevices);
                        Log.d(TAG, "Scan selesai, device: " + pairedDevices.size());
                    }
                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Log.e(TAG, "Permission Bluetooth ditolak");
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    // 2️⃣ Pair & connect ke ESP32
    public void connectToDevice(BluetoothDevice device, ConnectCallback callback) {
        new Thread(() -> {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(ESP32_UUID);
                bluetoothSocket.connect();
                outputStream = bluetoothSocket.getOutputStream();
                inputStream = bluetoothSocket.getInputStream();
                connectedDevice = device;
                Log.d(TAG, "Terkoneksi ke ESP32: " + device.getName());
                callback.onConnected();
            } catch (IOException e) {
                Log.e(TAG, "Gagal konek: " + e.getMessage());
                callback.onFailed(e.getMessage());
            }
        }).start();
    }

    // 3️⃣ Kirim data ke ESP32
    public void sendData(String data, SendCallback callback) {
        sendDataWithRetry(data, callback, 0);
    }

    // 1️⃣, 2️⃣, 3️⃣, 4️⃣: Kirim data dengan timeout & retry
    private void sendDataWithRetry(String data, SendCallback callback, int retryCount) {
        new Thread(() -> {
            try {
                if (outputStream != null) {
                    outputStream.write(data.getBytes());
                    outputStream.flush();
                    Log.d(TAG, "Data dikirim: " + data + " (retry: " + retryCount + ")");
                    // 1️⃣ Set timeout 5 detik nunggu ACK/NACK
                    handler.postDelayed(() -> {
                        if (retryCount < 3) {
                            Log.w(TAG, "Timeout, retry ke-" + (retryCount + 1));
                            sendDataWithRetry(data, callback, retryCount + 1);
                            if (callback instanceof RetryCallback) {
                                ((RetryCallback) callback).onTimeout(retryCount + 1);
                            }
                        } else {
                            Log.e(TAG, "Gagal kirim setelah 3x retry");
                            if (callback instanceof RetryCallback) {
                                ((RetryCallback) callback).onRetryFailed();
                            }
                            callback.onFailed("Timeout, gagal kirim setelah 3x percobaan");
                        }
                    }, 5000);
                    // 5️⃣ Listen response, cancel timeout kalau dapet ACK/NACK
                    listenResponse(response -> {
                        if (response.trim().equalsIgnoreCase("ACK") || response.trim().equalsIgnoreCase("NACK")) {
                            handler.removeCallbacksAndMessages(null);
                            Log.d(TAG, "ESP32 balas: " + response);
                            callback.onSent();
                        }
                    });
                } else {
                    callback.onFailed("OutputStream null");
                }
            } catch (IOException e) {
                Log.e(TAG, "Gagal kirim: " + e.getMessage());
                callback.onFailed(e.getMessage());
            }
        }).start();
    }

    // 4️⃣ Listener response dari ESP32
    public void listenResponse(ResponseCallback callback) {
        new Thread(() -> {
            try {
                byte[] buffer = new byte[1024];
                int bytes;
                while ((bytes = inputStream.read(buffer)) > 0) {
                    String response = new String(buffer, 0, bytes);
                    Log.d(TAG, "ESP32 response: " + response);
                    callback.onResponse(response);
                }
            } catch (IOException e) {
                Log.e(TAG, "Gagal baca response: " + e.getMessage());
            }
        }).start();
    }

    // 5️⃣ Callback interfaces
    public interface DeviceScanCallback {
        void onDevicesFound(Set<BluetoothDevice> devices);
    }
    public interface ConnectCallback {
        void onConnected();
        void onFailed(String error);
    }
    public interface SendCallback {
        void onSent();
        void onFailed(String error);
    }
    public interface RetryCallback extends SendCallback {
        void onTimeout(int retryCount);
        void onRetryFailed();
    }
    public interface ResponseCallback {
        void onResponse(String response);
    }
}
