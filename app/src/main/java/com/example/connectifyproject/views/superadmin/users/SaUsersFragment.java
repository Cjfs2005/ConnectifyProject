package com.example.connectifyproject.views.superadmin.users;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.R;
import com.example.connectifyproject.model.Role;
import com.example.connectifyproject.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class SaUsersFragment extends Fragment {

    private SaUsersAdapter adapter;
    private EnumSet<Role> selectedRoles = EnumSet.of(Role.GUIDE, Role.ADMIN, Role.CLIENT);
    private String currentQuery = "";

    private TextInputEditText etSearch;
    private MaterialButton btnRoles;
    private FloatingActionButton fabAdd;
    private ImageButton btnNotifications;

    public SaUsersFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sa_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        final NavController nav = NavHostFragment.findNavController(this);

        // --- Recycler ---
        RecyclerView rv = v.findViewById(R.id.rvUsers);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SaUsersAdapter(buildMockUsers(), u -> {
            Bundle b = new Bundle();
            b.putParcelable("user", u);
            nav.navigate(R.id.saUserDetailFragment, b);
        });
        rv.setAdapter(adapter);

        // --- Controles ---
        etSearch         = v.findViewById(R.id.etSearch);
        btnRoles         = v.findViewById(R.id.btnRoles);
        fabAdd           = v.findViewById(R.id.fabAddAdmin);
        btnNotifications = v.findViewById(R.id.btnNotifications);

        // 🔔 Campanita → Notificaciones (enviamos fromDestId)
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(view -> {
                Bundle args = new Bundle();
                args.putInt("fromDestId", nav.getCurrentDestination() != null ? nav.getCurrentDestination().getId() : 0);
                nav.navigate(R.id.saNotificationsFragment, args);
            });
        }

        // FAB → crear admin
        if (fabAdd != null) {
            fabAdd.setOnClickListener(view -> nav.navigate(R.id.saCreateAdminFragment));
        }

        // --- Restaurar estado ---
        if (savedInstanceState != null) {
            currentQuery = savedInstanceState.getString("q", "");
            int mask = savedInstanceState.getInt("roles", 0b111);
            selectedRoles = decodeRoles(mask);

            if (etSearch != null) etSearch.setText(currentQuery);
            adapter.setQuery(currentQuery);
            adapter.setRoleFilter(selectedRoles);
        }

        // --- Buscador ---
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    currentQuery = s == null ? "" : s.toString();
                    adapter.setQuery(currentQuery);
                }
            });
        }

        // --- Botón "Roles" ---
        btnRoles.setOnClickListener(this::showRolesPopup);
    }

    private void showRolesPopup(View anchor) {
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
        else if (id == R.id.role_admin) toggle(Role.ADMIN,  item.isChecked());
        else if (id == R.id.role_client)toggle(Role.CLIENT, item.isChecked());

        if (selectedRoles.isEmpty()) selectedRoles = EnumSet.of(Role.GUIDE, Role.ADMIN, Role.CLIENT);
        adapter.setRoleFilter(selectedRoles);
        return true;
    }

    private void toggle(Role r, boolean add) { if (add) selectedRoles.add(r); else selectedRoles.remove(r); }

    private EnumSet<Role> decodeRoles(int mask) {
        EnumSet<Role> set = EnumSet.noneOf(Role.class);
        if ((mask & 0b001) != 0) set.add(Role.GUIDE);
        if ((mask & 0b010) != 0) set.add(Role.ADMIN);
        if ((mask & 0b100) != 0) set.add(Role.CLIENT);
        if (set.isEmpty()) set = EnumSet.of(Role.GUIDE, Role.ADMIN, Role.CLIENT);
        return set;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar referencias para evitar memory leaks
        etSearch = null;
        btnRoles = null;
        fabAdd = null;
        btnNotifications = null;
        adapter = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        int mask = 0;
        if (selectedRoles.contains(Role.GUIDE))  mask |= 0b001;
        if (selectedRoles.contains(Role.ADMIN))  mask |= 0b010;
        if (selectedRoles.contains(Role.CLIENT)) mask |= 0b100;
        out.putInt("roles", mask);
        out.putString("q", currentQuery);
    }

    private List<User> buildMockUsers() {
        List<User> list = new ArrayList<>();
        list.add(new User("Alejandro","Mora A.","70456789","Perú Travel",Role.ADMIN,"DNI","08/17/1996","alejandro@perutravel.com","999888777","Av. Perú 123",null));
        list.add(new User("Alessandro","Mazz I.","74444444","Cusco Guide",Role.GUIDE,"DNI","06/19/1991","alessandro@cuscoguide.pe","999111222","Av. Cultura 555",null));
        list.add(new User("Carlos","Antama C.","70124567","Inca Tours",Role.ADMIN,"DNI","09/21/1990","carlos@incatours.com","988776655","Av. Inca 101",null));
        list.add(new User("María","Chávez P.","71230011","Cusco Guide",Role.GUIDE,"DNI","05/02/1993","maria.chavez@cuscoguide.pe","987654321","Jr. Mapi 456",null));
        list.add(new User("Mateo","Rentería S.","70456799","Perú Travel",Role.CLIENT,"DNI","07/10/1997","mateo@correo.com","955667788","Psj. Lima 22",null));
        list.add(new User("Miranda","Asturia B.","70112345","Andes Corp",Role.CLIENT,"DNI","01/15/1998","miranda@andescorp.com","901223344","Calle Sol 300",null));
        list.add(new User("Mónica","Asturias Z.","70660606","Perú Travel",Role.CLIENT,"DNI","04/28/1994","monica@correo.com","913579246","Urb. Santa Ana 12",null));
        list.add(new User("Sandra","Vera F.","70991122","Inca Tours",Role.CLIENT,"DNI","11/08/1999","sandra@correo.com","912345678","Jr. Tres Cruces 77",null));
        list.add(new User("Sergio","Tiravanti R.","72233445","Andes Corp",Role.ADMIN,"DNI","12/30/1992","sergio@andescorp.com","970334455","Jr. Qosqo 120",null));
        list.add(new User("Sofía","Loaiza B.","73322110","Wayna Picchu",Role.GUIDE,"DNI","03/03/1995","sofia@wpicchu.pe","980112233","Mz. B Lt. 5",null));
        return list;
    }
}
