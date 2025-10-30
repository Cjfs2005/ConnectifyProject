package com.example.connectifyproject.views.superadmin.notifications;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.connectifyproject.R;
import com.example.connectifyproject.databinding.FragmentSaNotificationsBinding;
import com.example.connectifyproject.viewmodel.superadmin.notifications.SaNotificationsViewModel;
import com.google.android.material.snackbar.Snackbar;

public class SaNotificationsFragment extends Fragment {

    private FragmentSaNotificationsBinding b;
    private SaNotificationsViewModel vm;
    private SaNotificationsAdapter adapter;

    // guardamos el destino de origen
    private int fromDestId = 0;

    public SaNotificationsFragment() {
        super(R.layout.fragment_sa_notifications);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        b = FragmentSaNotificationsBinding.bind(view);
        vm = new ViewModelProvider(this).get(SaNotificationsViewModel.class);

        // leer 'fromDestId' si vino en los argumentos
        Bundle args = getArguments();
        if (args != null) {
            fromDestId = args.getInt("fromDestId", 0);
        }

        final NavController nav = NavHostFragment.findNavController(this);

        // Toolbar + back arrow
        b.toolbar.setTitle(R.string.notifications);
        b.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24);
        b.toolbar.setNavigationOnClickListener(v -> goBack(nav));
        b.toolbar.inflateMenu(R.menu.menu_sa_notifications);
        b.toolbar.setOnMenuItemClickListener(this::onMenuClick);

        // Botón físico “atrás”
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override public void handleOnBackPressed() { goBack(nav); }
                }
        );

        // Recycler
        adapter = new SaNotificationsAdapter(new SaNotificationsAdapter.Listener() {
            @Override public void onOpen(NotificationItem item, int position) {
                // Abrir pantalla de Solicitudes de guía
                nav.navigate(R.id.saRequestsFragment);
            }
            @Override public void onDelete(NotificationItem item, int position) {
                vm.deleteById(item.getId());
                Snackbar.make(b.getRoot(), R.string.deleted, Snackbar.LENGTH_SHORT).show();
            }
        });
        b.rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        b.rv.setAdapter(adapter);

        // FAB: marcar todo
        b.fabMarkAll.setOnClickListener(v -> {
            vm.markAllRead();
            Snackbar.make(b.getRoot(), R.string.all_marked_read, Snackbar.LENGTH_SHORT).show();
        });

        // Datos
        vm.getNotifications().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            toggleEmpty(list == null || list.isEmpty());
        });
    }

    private void goBack(NavController nav) {
        // 1) intenta volver con pop
        if (nav.popBackStack()) return;

        // 2) si no hay stack, intenta volver al destino de origen si lo conocemos
        if (fromDestId != 0) {
            try {
                nav.navigate(fromDestId);
                return;
            } catch (Exception ignored) { /* por si no está en el grafo actual */ }
        }

        // 3) fallback
        nav.navigateUp();
    }

    private boolean onMenuClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_clear_all) {
            vm.deleteAll();
            Snackbar.make(b.getRoot(), R.string.cleared_all, Snackbar.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_refresh) {
            vm.refresh();
            return true;
        }
        return false;
    }

    private void toggleEmpty(boolean empty) {
        b.emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        b.rv.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}
