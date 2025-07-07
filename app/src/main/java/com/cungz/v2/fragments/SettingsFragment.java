
package com.cungz.v2.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.cungz.v2.R;
import android.content.Intent;
import com.cungz.v2.services.BluetoothService;

public class SettingsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        Switch bluetooth = view.findViewById(R.id.settings_bluetooth);
        Switch autoConnect = view.findViewById(R.id.settings_auto_connect);
        Switch voiceNav = view.findViewById(R.id.settings_voice_nav);
        Switch voiceResponse = view.findViewById(R.id.settings_voice_response);
        Switch darkMode = view.findViewById(R.id.settings_dark_mode);
        Switch animations = view.findViewById(R.id.settings_animations);

        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {
            String msg = buttonView.getText() + (isChecked ? " ON" : " OFF");
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        };
        bluetooth.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Start BluetoothService
                getContext().startService(new Intent(getContext(), BluetoothService.class));
                Toast.makeText(getContext(), "Bluetooth ESP32 ON", Toast.LENGTH_SHORT).show();
            } else {
                getContext().stopService(new Intent(getContext(), BluetoothService.class));
                Toast.makeText(getContext(), "Bluetooth ESP32 OFF", Toast.LENGTH_SHORT).show();
            }
        });
        autoConnect.setOnCheckedChangeListener(listener);
        voiceNav.setOnCheckedChangeListener(listener);
        voiceResponse.setOnCheckedChangeListener(listener);
        darkMode.setOnCheckedChangeListener(listener);
        animations.setOnCheckedChangeListener(listener);
        return view;
    }
}
