package com.example.connectifyproject.ui.superadmin.users;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.connectifyproject.R;
import com.example.connectifyproject.databinding.FragmentSaUsersBinding;
import com.example.connectifyproject.model.Role;
import com.example.connectifyproject.model.User;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class SaUsersFragment extends Fragment {

    private FragmentSaUsersBinding binding;
    private SaUsersAdapter adapter;

    // Selección de roles actual
    private EnumSet<Role> selectedRoles = EnumSet.of(Role.GUIDE, Role.ADMIN, Role.CLIENT);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSaUsersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Lista
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SaUsersAdapter(mockData(), u -> {
            Bundle args = new Bundle();
            args.putString("name",    u.getName());
            args.putString("dni",     u.getDni());
            args.putString("company", u.getCompany());
            args.putString("role",    u.getRole().name());
            NavHostFragment.findNavController(this)
                    .navigate(R.id.saUserDetailFragment, args);
        });
        binding.rvUsers.setAdapter(adapter);

        // Chip "Todos"
        binding.chipAll.setOnClickListener(v -> {
            selectedRoles = EnumSet.of(Role.GUIDE, Role.ADMIN, Role.CLIENT);
            adapter.setRoleFilter(selectedRoles);
            updateFabVisibility();
        });

        // Popup de roles
        binding.btnRoles.setOnClickListener(this::showRolesPopup);

        // Buscador
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.setQuery(String.valueOf(s));
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // FAB: ir a crear admin
        binding.fabAddAdmin.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.saCreateAdminFragment)
        );

        // Al inicio: oculto
        updateFabVisibility();
    }

    private void showRolesPopup(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_sa_roles, popup.getMenu());

        popup.getMenu().findItem(R.id.role_guide).setChecked(selectedRoles.contains(Role.GUIDE));
        popup.getMenu().findItem(R.id.role_admin).setChecked(selectedRoles.contains(Role.ADMIN));
        popup.getMenu().findItem(R.id.role_client).setChecked(selectedRoles.contains(Role.CLIENT));

        popup.setOnMenuItemClickListener(item -> {
            toggleRole(item);
            adapter.setRoleFilter(selectedRoles);
            updateFabVisibility();
            return true;
        });
        popup.show();
    }

    private void toggleRole(MenuItem item) {
        item.setChecked(!item.isChecked());
        int id = item.getItemId();
        if (id == R.id.role_guide) {
            if (item.isChecked()) selectedRoles.add(Role.GUIDE); else selectedRoles.remove(Role.GUIDE);
        } else if (id == R.id.role_admin) {
            if (item.isChecked()) selectedRoles.add(Role.ADMIN); else selectedRoles.remove(Role.ADMIN);
        } else if (id == R.id.role_client) {
            if (item.isChecked()) selectedRoles.add(Role.CLIENT); else selectedRoles.remove(Role.CLIENT);
        }
        if (selectedRoles.isEmpty()) selectedRoles = EnumSet.of(Role.GUIDE, Role.ADMIN, Role.CLIENT);
    }

    /** Muestra el FAB solo si el filtro es EXACTAMENTE {ADMIN} */
    private void updateFabVisibility() {
        boolean onlyAdmin = selectedRoles.size() == 1 && selectedRoles.contains(Role.ADMIN);
        binding.fabAddAdmin.setVisibility(onlyAdmin ? View.VISIBLE : View.GONE);
    }

    private List<User> mockData() {
        List<User> list = new ArrayList<>();
        list.add(new User("María Chavez P.", "70999111", "Cusco Guide", Role.GUIDE));
        list.add(new User("Alejandro Mora A.", "70456789", "Perú Travel", Role.ADMIN));
        list.add(new User("Carlos Antama C.", "70900123", "Andes Corp", Role.CLIENT));
        list.add(new User("Miranda Asturia B.", "70123456", "Inca Tours", Role.CLIENT));
        list.add(new User("Mateo Rentería S.", "70456789", "Perú Travel", Role.ADMIN));
        return list;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
