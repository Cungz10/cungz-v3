
package com.cungz.v2.fragments;

import com.cungz.v2.utils.BluetoothHelper;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.cungz.v2.R;
import com.cungz.v2.services.NavigationService;
import com.cungz.v2.models.RouteInfo;
import android.content.Context;
import android.view.ViewGroup;
import androidx.compose.ui.platform.ComposeView;
import android.graphics.Color;
import android.util.Log;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import android.location.Location;
import androidx.core.content.ContextCompat;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.cungz.v2.BuildConfig;

public class MapsFragment extends Fragment {
    // --- Gen Z style: State variables ---
    private org.maplibre.android.annotations.Marker userMarker = null;
    private org.maplibre.android.annotations.Polyline currentPolyline = null;
    private java.util.List<LatLng> lastRoute = null;
    private FusedLocationProviderClient fusedLocationClient;
    private com.google.android.gms.location.LocationCallback locationCallback;
    private MapLibreMap mapLibreMap;
    private View rootView;
    private boolean autoResendInProgress = false;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_maps, container, false);
        EditText search = rootView.findViewById(R.id.maps_search);
        Button findRoute = rootView.findViewById(R.id.maps_find_route);
        Button resendRoute = rootView.findViewById(R.id.maps_resend_route);
        ComposeView composeView = rootView.findViewById(R.id.compose_view);
        TextView routeInfo = rootView.findViewById(R.id.maps_route_info);

        // MapLibre Compose setup
        composeView.setContent(() -> {
            com.cungz.v2.maps.MapLibreMapComposableKt.MapLibreMapComposable();
            return null;
        });

        // FusedLocationProviderClient for user location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // --- LocationCallback for live tracking ---
        locationCallback = new com.google.android.gms.location.LocationCallback() {
            @Override
            public void onLocationResult(com.google.android.gms.location.LocationResult locationResult) {
                if (locationResult == null || mapLibreMap == null) return;
                Location location = locationResult.getLastLocation();
                LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                // Update user marker
                if (userMarker != null) userMarker.remove();
                userMarker = mapLibreMap.addMarker(new MarkerOptions()
                        .position(userLatLng)
                        .title("Lokasi Kamu")
                        .icon(org.maplibre.android.annotations.IconFactory.getInstance(requireContext()).fromResource(R.drawable.ic_marker)));
                // Geser kamera biar user kelihatan
                mapLibreMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));
                Log.d("MAPS", "[Live] User location: " + userLatLng.getLatitude() + ", " + userLatLng.getLongitude());
                // Cek auto resend kalau user keluar rute jauh banget
                if (lastRoute != null && !autoResendInProgress) {
                    double minDist = 9999;
                    for (LatLng point : lastRoute) {
                        double dist = distance(userLatLng, point);
                        if (dist < minDist) minDist = dist;
                    }
                    if (minDist > 30) { // 30 meter threshold, bisa diubah
                        autoResendInProgress = true;
                        Snackbar.make(rootView, "Bro, lo keluar jalur! Auto resend ke ESP32...", Snackbar.LENGTH_LONG).show();
                        sendRouteToEsp32(lastRoute, rootView);
                        rootView.postDelayed(() -> autoResendInProgress = false, 5000); // biar nggak spam
                    }
                }
            }
        };

        // Map logic di Compose sekarang. Semua MapView, Polyline, Marker, dsb, harus di-handle di Kotlin Compose.


        return rootView;
    }

    private void sendRouteToEsp32(java.util.List<LatLng> points, View rootView) {
        try {
            JSONArray arr = new JSONArray();
            for (LatLng latLng : points) {
                JSONObject obj = new JSONObject();
                obj.put("lat", latLng.getLatitude());
                obj.put("lng", latLng.getLongitude());
                arr.put(obj);
            }
            String json = arr.toString();
            BluetoothHelper bluetoothHelper = new BluetoothHelper(requireContext());
            bluetoothHelper.sendData(json, new BluetoothHelper.RetryCallback() {
                int retry = 0;
                @Override
                public void onSent() {
                    Snackbar.make(rootView, "Route sent! ACK dari ESP32 🚀", Snackbar.LENGTH_LONG).show();
                    Log.i("MAPS", "Route sent to ESP32, dapet ACK!");
                }
                @Override
                public void onFailed(String error) {
                    Snackbar.make(rootView, "Gagal kirim ke ESP32: " + error, Snackbar.LENGTH_LONG).show();
                    Log.e("MAPS", "Gagal kirim ke ESP32: " + error);
                }
                @Override
                public void onTimeout(int retryCount) {
                    Log.w("MAPS", "Timeout, retry ke-" + retryCount);
                }
                @Override
                public void onRetryFailed() {
                    Snackbar.make(rootView, "ESP32 nggak respon setelah 3x retry!", Snackbar.LENGTH_LONG).show();
                    Log.e("MAPS", "ESP32 nggak respon setelah 3x retry!");
                }
            });
        } catch (Exception e) {
            Snackbar.make(rootView, "Format rute error: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    }

    // Semua lifecycle MapView dihapus, ComposeView auto handle lifecycle

    // --- Helper: hitung jarak 2 titik (meter) ---
    private double distance(LatLng a, LatLng b) {
        float[] res = new float[1];
        Location.distanceBetween(a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude(), res);
        return res[0];
    }
}
