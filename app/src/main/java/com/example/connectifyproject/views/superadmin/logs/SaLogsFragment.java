package com.example.connectifyproject.views.superadmin.logs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.connectifyproject.R;
import com.example.connectifyproject.databinding.FragmentSaLogsBinding;
import com.example.connectifyproject.model.Role;
import com.example.connectifyproject.model.User;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class SaLogsFragment extends Fragment {

    // Actualiza el texto del botón de orden según la selección
    private void updateSortButton() {
        if (binding != null && binding.btnSort != null) {
            if (sort == SaLogsAdapter.SortOrder.RECENT) {
                binding.btnSort.setText("Más recientes");
            } else {
                binding.btnSort.setText("Más antiguos");
            }
        }
    }

    // Muestra el menú de orden
    private void showSortPopup(View anchor) {
        PopupMenu pm = new PopupMenu(requireContext(), anchor);
        pm.getMenu().add(0, 1, 0, "Más recientes").setCheckable(true).setChecked(sort == SaLogsAdapter.SortOrder.RECENT);
        pm.getMenu().add(0, 2, 1, "Más antiguos").setCheckable(true).setChecked(sort == SaLogsAdapter.SortOrder.OLD);
        pm.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1 && sort != SaLogsAdapter.SortOrder.RECENT) {
                sort = SaLogsAdapter.SortOrder.RECENT;
                adapter.setSort(sort);
                updateSortButton();
                return true;
            } else if (item.getItemId() == 2 && sort != SaLogsAdapter.SortOrder.OLD) {
                sort = SaLogsAdapter.SortOrder.OLD;
                adapter.setSort(sort);
                updateSortButton();
                return true;
            }
            return false;
        });
        pm.show();
    }

    private FragmentSaLogsBinding binding;
    private SaLogsAdapter adapter;

    private SaLogsAdapter.SortOrder sort = SaLogsAdapter.SortOrder.RECENT;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSaLogsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // Buscador de logs
        if (binding.etSaLogsSearch != null) {
            binding.etSaLogsSearch.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    adapter.setSearchText(s != null ? s.toString() : "");
                }
                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }

        // Conectar botón de orden
        if (binding.btnSort != null) {
            binding.btnSort.setOnClickListener(this::showSortPopup);
            updateSortButton();
        }

        final NavController nav = NavHostFragment.findNavController(this);

        int purple = ContextCompat.getColor(requireContext(), R.color.brand_purple_dark);
        MaterialToolbar tb = v.findViewById(R.id.toolbar);
        if (tb != null) {
            tb.setBackgroundColor(purple);
            tb.setTitleTextColor(0xFFFFFFFF);
            tb.setNavigationIconTint(0xFFFFFFFF);
        }
        AppBarLayout appBar = v.findViewById(R.id.appBar);
        if (appBar != null) appBar.setBackgroundColor(purple);

        // 🔔 Campanita → Notificaciones (enviamos fromDestId)
        View bell = v.findViewById(R.id.btnNotifications);
        if (bell != null) {
            bell.setOnClickListener(x -> {
                Bundle args = new Bundle();
                args.putInt("fromDestId", nav.getCurrentDestination() != null ? nav.getCurrentDestination().getId() : 0);
                nav.navigate(R.id.saNotificationsFragment, args);
            });
        }

        binding.rvLogs.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SaLogsAdapter(requireContext(), new ArrayList<>(), item -> {
            // Mostrar diálogo con título y descripción completa
            new android.app.AlertDialog.Builder(requireContext())
                .setTitle(item.titulo)
                .setMessage(item.descripcion)
                .setPositiveButton("Cerrar", null)
                .show();
        });
        binding.rvLogs.setAdapter(adapter);

        // Cargar logs desde Firestore
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("logs")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener((snap, err) -> {
                if (err != null) return;
                java.util.List<SaLogFirestoreItem> logs = new java.util.ArrayList<>();
                if (snap != null) {
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                        String titulo = doc.getString("titulo");
                        String descripcion = doc.getString("descripcion");
                        Long timestamp = doc.getLong("timestamp");
                        if (titulo != null && descripcion != null && timestamp != null) {
                            logs.add(new SaLogFirestoreItem(titulo, descripcion, timestamp));
                        }
                    }
                }
                adapter.replaceAll(logs);
            });

        if (savedInstanceState != null) {
            sort = savedInstanceState.getBoolean("sortOld", false)
                    ? SaLogsAdapter.SortOrder.OLD
                    : SaLogsAdapter.SortOrder.RECENT;
            adapter.setSort(sort);
        }
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        out.putBoolean("sortOld", sort == SaLogsAdapter.SortOrder.OLD);
        // Removed roles saving
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar referencias para evitar memory leaks
        binding = null;
        adapter = null;
    }

}
