
package com.cungz.v2.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cungz.v2.R;
import com.cungz.v2.adapters.HistoryAdapter;
import com.cungz.v2.models.HistoryItem;
import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {
    private HistoryAdapter historyAdapter;
    private List<HistoryItem> historyList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.history_recycler);
        Button clearBtn = view.findViewById(R.id.history_clear_btn);
        historyAdapter = new HistoryAdapter(historyList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(historyAdapter);

        // Dummy data
        historyList.add(new HistoryItem("Navigasi ke Kampus", "2025-07-07"));
        historyList.add(new HistoryItem("Chat AI: Cek cuaca", "2025-07-06"));
        historyAdapter.notifyDataSetChanged();

        clearBtn.setOnClickListener(v -> {
            historyList.clear();
            historyAdapter.notifyDataSetChanged();
            Toast.makeText(getContext(), "Riwayat dihapus semua!", Toast.LENGTH_SHORT).show();
        });
        return view;
    }
}
