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

    private FragmentSaLogsBinding binding;
    private SaLogsAdapter adapter;

    private SaLogsAdapter.SortOrder sort = SaLogsAdapter.SortOrder.RECENT;
    private EnumSet<Role> selectedRoles = EnumSet.of(Role.GUIDE, Role.ADMIN, Role.CLIENT);

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

        // 🟣 Forzar morado oscuro en la barra superior (IDs opcionales)
        int purple = ContextCompat.getColor(requireContext(), R.color.brand_purple_dark);
        MaterialToolbar tb = v.findViewById(R.id.toolbar);
        if (tb != null) {
            tb.setBackgroundColor(purple);
            tb.setTitleTextColor(0xFFFFFFFF);
            tb.setNavigationIconTint(0xFFFFFFFF);
        }
        AppBarLayout appBar = v.findViewById(R.id.appbar);
        if (appBar != null) appBar.setBackgroundColor(purple);

        // Recycler + Adapter
        binding.rvLogs.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SaLogsAdapter(mockLogs(), item -> {
            Bundle b = new Bundle();
            b.putParcelable("user", item.user);
            b.putLong("at", item.atMillis);
            b.putString("action", item.action);
            b.putString("role", item.role.name());
            NavHostFragment.findNavController(this).navigate(R.id.saLogDetailFragment, b);
        });
        binding.rvLogs.setAdapter(adapter);

        // Restaurar estado
        if (savedInstanceState != null) {
            sort = savedInstanceState.getBoolean("sortOld", false)
                    ? SaLogsAdapter.SortOrder.OLD
                    : SaLogsAdapter.SortOrder.RECENT;
            int mask = savedInstanceState.getInt("roles", 0b111);
            selectedRoles = decodeRoles(mask);
            adapter.setSort(sort);
            adapter.setRoleFilter(selectedRoles);
            updateRoleButton();
        }

        // Listeners
        binding.btnSort.setOnClickListener(this::showSortPopup);
        binding.btnRole.setOnClickListener(this::showRolePopup);
        updateRoleButton();
    }

    private void showSortPopup(View anchor) {
        PopupMenu pm = new PopupMenu(requireContext(), anchor);
        pm.getMenuInflater().inflate(R.menu.menu_logs_sort, pm.getMenu());
        pm.getMenu().findItem(R.id.sort_recent).setChecked(sort == SaLogsAdapter.SortOrder.RECENT);
        pm.getMenu().findItem(R.id.sort_old).setChecked(sort == SaLogsAdapter.SortOrder.OLD);
        pm.setOnMenuItemClickListener(this::onSortItem);
        pm.show();
    }

    private boolean onSortItem(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.sort_recent) {
            sort = SaLogsAdapter.SortOrder.RECENT;
            item.setChecked(true);
        } else if (id == R.id.sort_old) {
            sort = SaLogsAdapter.SortOrder.OLD;
            item.setChecked(true);
        } else return false;
        adapter.setSort(sort);
        return true;
    }

    private void showRolePopup(View anchor) {
        PopupMenu pm = new PopupMenu(requireContext(), anchor);
        pm.inflate(R.menu.menu_sa_roles);
        pm.getMenu().findItem(R.id.role_guide).setChecked(selectedRoles.contains(Role.GUIDE));
        pm.getMenu().findItem(R.id.role_admin).setChecked(selectedRoles.contains(Role.ADMIN));
        pm.getMenu().findItem(R.id.role_client).setChecked(selectedRoles.contains(Role.CLIENT));
        pm.setOnMenuItemClickListener(this::onRoleItemClicked);
        pm.show();
    }

    private boolean onRoleItemClicked(MenuItem item) {
        final int id = item.getItemId();
        item.setChecked(!item.isChecked());
        if (id == R.id.role_guide)      toggle(Role.GUIDE,  item.isChecked());
        else if (id == R.id.role_admin) toggle(Role.ADMIN, item.isChecked());
        else if (id == R.id.role_client)toggle(Role.CLIENT, item.isChecked());

        if (selectedRoles.isEmpty()) selectedRoles = EnumSet.of(Role.GUIDE, Role.ADMIN, Role.CLIENT);
        adapter.setRoleFilter(selectedRoles);
        updateRoleButton();
        return true;
    }

    private void toggle(Role r, boolean add) { if (add) selectedRoles.add(r); else selectedRoles.remove(r); }

    private void updateRoleButton() {
        if (selectedRoles.size() == 3) {
            binding.btnRole.setText("Todos");
        } else if (selectedRoles.equals(EnumSet.of(Role.ADMIN))) {
            binding.btnRole.setText("Admin");
        } else if (selectedRoles.equals(EnumSet.of(Role.GUIDE))) {
            binding.btnRole.setText("Guía");
        } else if (selectedRoles.equals(EnumSet.of(Role.CLIENT))) {
            binding.btnRole.setText("Cliente");
        } else {
            binding.btnRole.setText("Mixto");
        }
    }

    private EnumSet<Role> decodeRoles(int mask) {
        EnumSet<Role> set = EnumSet.noneOf(Role.class);
        if ((mask & 0b001) != 0) set.add(Role.GUIDE);
        if ((mask & 0b010) != 0) set.add(Role.ADMIN);
        if ((mask & 0b100) != 0) set.add(Role.CLIENT);
        if (set.isEmpty()) set = EnumSet.of(Role.GUIDE, Role.ADMIN, Role.CLIENT);
        return set;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        out.putBoolean("sortOld", sort == SaLogsAdapter.SortOrder.OLD);
        int mask = 0;
        if (selectedRoles.contains(Role.GUIDE))  mask |= 0b001;
        if (selectedRoles.contains(Role.ADMIN))  mask |= 0b010;
        if (selectedRoles.contains(Role.CLIENT)) mask |= 0b100;
        out.putInt("roles", mask);
    }

    @Override
    public void onDestroyView() { super.onDestroyView(); binding = null; }

    // ---------------- Mock data ----------------
    private List<SaLogsAdapter.LogItem> mockLogs() {
        List<SaLogsAdapter.LogItem> list = new ArrayList<>();
        long now = System.currentTimeMillis();
        list.add(new SaLogsAdapter.LogItem("Creó un tour", Role.ADMIN, new User("Alejandro","Mora A.","70456789","Perú Travel",Role.ADMIN,"DNI","08/17/1996","alejandro@perutravel.com","999888777","Av. Perú 123",null), now - hours(3)));
        list.add(new SaLogsAdapter.LogItem("Creó un tour", Role.ADMIN, new User("Carlos","Antama C.","70124567","Inca Tours",Role.ADMIN,"DNI","09/21/1990","carlos@incatours.com","988776655","Av. Inca 101",null), now - days(1) - hours(5)));
        list.add(new SaLogsAdapter.LogItem("Finalizó el tour", Role.GUIDE, new User("Alessandro","Mazz I.","74444444","Cusco Guide",Role.GUIDE,"DNI","06/19/1991","alessandro@cuscoguide.pe","999111222","Av. Cultura 555",null), now - days(26)));
        list.add(new SaLogsAdapter.LogItem("Finalizó el tour", Role.GUIDE, new User("María","Chávez P.","71230011","Cusco Guide",Role.GUIDE,"DNI","05/02/1993","maria.chavez@cuscoguide.pe","987654321","Jr. Mapi 456",null), now - days(26) - hours(2)));
        list.add(new SaLogsAdapter.LogItem("Reservó un tour", Role.CLIENT, new User("Mateo","Rentería S.","70456799","Perú Travel",Role.CLIENT,"DNI","07/10/1997","mateo@correo.com","955667788","Psj. Lima 22",null), now - days(26) - hours(3)));
        list.add(new SaLogsAdapter.LogItem("Reservó un tour", Role.CLIENT, new User("Sandra","Vera F.","70991122","Inca Tours",Role.CLIENT,"DNI","11/08/1999","sandra@correo.com","912345678","Jr. Tres Cruces 77",null), now - days(26) - hours(7)));
        return list;
    }
    private long days(int d) { return d * 24L * 60 * 60 * 1000; }
    private long hours(int h) { return h * 60L * 60 * 1000; }
}
