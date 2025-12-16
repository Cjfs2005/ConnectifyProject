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
            int mask = savedInstanceState.getInt("roles", 0b111);
            selectedRoles = decodeRoles(mask);
            adapter.setSort(sort);
            adapter.setRoleFilter(selectedRoles);
            updateRoleButton();
        }

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
            binding.btnRole.setText("Roles");
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
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar referencias para evitar memory leaks
        binding = null;
        adapter = null;
    }

    private List<SaLogsAdapter.LogItem> mockLogs() {
        List<SaLogsAdapter.LogItem> list = new ArrayList<>();
        long now = System.currentTimeMillis();
        
        // Logs recientes (últimas 24 horas)
        list.add(new SaLogsAdapter.LogItem("Creó un tour", Role.ADMIN, new User("Alejandro","Mora A.","70456789","Perú Travel",Role.ADMIN,"DNI","08/17/1996","alejandro@perutravel.com","999888777","Av. Perú 123",null), now - hours(3)));
        list.add(new SaLogsAdapter.LogItem("Modificó un tour", Role.ADMIN, new User("Alejandro","Mora A.","70456789","Perú Travel",Role.ADMIN,"DNI","08/17/1996","alejandro@perutravel.com","999888777","Av. Perú 123",null), now - hours(5)));
        list.add(new SaLogsAdapter.LogItem("Reservó un tour", Role.CLIENT, new User("Luis","Torres M.","70334455","Cusco Tours",Role.CLIENT,"DNI","03/14/1995","luis.torres@correo.com","944556677","Av. Los Nogales 89",null), now - hours(8)));
        list.add(new SaLogsAdapter.LogItem("Canceló reserva", Role.CLIENT, new User("Patricia","Mendoza R.","70667788","Inca Tours",Role.CLIENT,"DNI","12/20/1992","patricia.m@correo.com","933445566","Jr. Las Rosas 45",null), now - hours(12)));
        list.add(new SaLogsAdapter.LogItem("Inició sesión", Role.GUIDE, new User("Jorge","Quispe T.","72334455","Peru Adventures",Role.GUIDE,"DNI","04/25/1988","jorge.quispe@peruadv.com","977889900","Psj. San Martín 12",null), now - hours(15)));
        list.add(new SaLogsAdapter.LogItem("Registró nuevo guía", Role.ADMIN, new User("Carlos","Antama C.","70124567","Inca Tours",Role.ADMIN,"DNI","09/21/1990","carlos@incatours.com","988776655","Av. Inca 101",null), now - hours(18)));
        
        // Logs de ayer
        list.add(new SaLogsAdapter.LogItem("Creó un tour", Role.ADMIN, new User("Carlos","Antama C.","70124567","Inca Tours",Role.ADMIN,"DNI","09/21/1990","carlos@incatours.com","988776655","Av. Inca 101",null), now - days(1) - hours(5)));
        list.add(new SaLogsAdapter.LogItem("Finalizó el tour", Role.GUIDE, new User("Rosa","Flores C.","73112233","Peru Adventures",Role.GUIDE,"DNI","08/30/1989","rosa.flores@peruadv.com","966778899","Av. Cultural 234",null), now - days(1) - hours(10)));
        list.add(new SaLogsAdapter.LogItem("Reservó un tour", Role.CLIENT, new User("Diego","Ramírez P.","70556677","Perú Travel",Role.CLIENT,"DNI","01/15/1998","diego.ram@correo.com","922334455","Jr. Ollantay 67",null), now - days(1) - hours(14)));
        list.add(new SaLogsAdapter.LogItem("Eliminó un tour", Role.ADMIN, new User("Alejandro","Mora A.","70456789","Perú Travel",Role.ADMIN,"DNI","08/17/1996","alejandro@perutravel.com","999888777","Av. Perú 123",null), now - days(1) - hours(20)));
        
        // Logs de hace 3 días
        list.add(new SaLogsAdapter.LogItem("Actualizó perfil", Role.GUIDE, new User("Pedro","Huamán S.","74556677","Cusco Guide",Role.GUIDE,"DNI","10/05/1987","pedro.huaman@cuscoguide.pe","955443322","Av. Sol 890",null), now - days(3) - hours(6)));
        list.add(new SaLogsAdapter.LogItem("Reservó un tour", Role.CLIENT, new User("Ana","López V.","70778899","Inca Tours",Role.CLIENT,"DNI","06/22/1994","ana.lopez@correo.com","911223344","Psj. Wiracocha 33",null), now - days(3) - hours(11)));
        list.add(new SaLogsAdapter.LogItem("Creó un tour", Role.ADMIN, new User("Carlos","Antama C.","70124567","Inca Tours",Role.ADMIN,"DNI","09/21/1990","carlos@incatours.com","988776655","Av. Inca 101",null), now - days(3) - hours(16)));
        
        // Logs de hace una semana
        list.add(new SaLogsAdapter.LogItem("Inició sesión", Role.ADMIN, new User("Alejandro","Mora A.","70456789","Perú Travel",Role.ADMIN,"DNI","08/17/1996","alejandro@perutravel.com","999888777","Av. Perú 123",null), now - days(7) - hours(4)));
        list.add(new SaLogsAdapter.LogItem("Finalizó el tour", Role.GUIDE, new User("Carmen","Vargas L.","73445566","Peru Adventures",Role.GUIDE,"DNI","02/18/1990","carmen.vargas@peruadv.com","988990011","Jr. Pachacutec 55",null), now - days(7) - hours(9)));
        list.add(new SaLogsAdapter.LogItem("Reservó un tour", Role.CLIENT, new User("Roberto","Castillo F.","70889900","Cusco Tours",Role.CLIENT,"DNI","09/03/1996","roberto.cast@correo.com","900112233","Av. Grau 78",null), now - days(7) - hours(13)));
        list.add(new SaLogsAdapter.LogItem("Modificó un tour", Role.ADMIN, new User("Carlos","Antama C.","70124567","Inca Tours",Role.ADMIN,"DNI","09/21/1990","carlos@incatours.com","988776655","Av. Inca 101",null), now - days(7) - hours(17)));
        
        // Logs de hace 2 semanas
        list.add(new SaLogsAdapter.LogItem("Registró nuevo cliente", Role.ADMIN, new User("Alejandro","Mora A.","70456789","Perú Travel",Role.ADMIN,"DNI","08/17/1996","alejandro@perutravel.com","999888777","Av. Perú 123",null), now - days(14) - hours(7)));
        list.add(new SaLogsAdapter.LogItem("Finalizó el tour", Role.GUIDE, new User("Miguel","Prado N.","74667788","Cusco Guide",Role.GUIDE,"DNI","11/12/1986","miguel.prado@cuscoguide.pe","977665544","Psj. Manco Cápac 90",null), now - days(14) - hours(12)));
        list.add(new SaLogsAdapter.LogItem("Canceló reserva", Role.CLIENT, new User("Elena","Paredes H.","70998877","Perú Travel",Role.CLIENT,"DNI","04/08/1993","elena.paredes@correo.com","933221100","Jr. Saphy 44",null), now - days(14) - hours(18)));
        
        // Logs antiguos (hace 26 días)
        list.add(new SaLogsAdapter.LogItem("Finalizó el tour", Role.GUIDE, new User("Alessandro","Mazz I.","74444444","Cusco Guide",Role.GUIDE,"DNI","06/19/1991","alessandro@cuscoguide.pe","999111222","Av. Cultura 555",null), now - days(26)));
        list.add(new SaLogsAdapter.LogItem("Finalizó el tour", Role.GUIDE, new User("María","Chávez P.","71230011","Cusco Guide",Role.GUIDE,"DNI","05/02/1993","maria.chavez@cuscoguide.pe","987654321","Jr. Mapi 456",null), now - days(26) - hours(2)));
        list.add(new SaLogsAdapter.LogItem("Reservó un tour", Role.CLIENT, new User("Mateo","Rentería S.","70456799","Perú Travel",Role.CLIENT,"DNI","07/10/1997","mateo@correo.com","955667788","Psj. Lima 22",null), now - days(26) - hours(3)));
        list.add(new SaLogsAdapter.LogItem("Reservó un tour", Role.CLIENT, new User("Sandra","Vera F.","70991122","Inca Tours",Role.CLIENT,"DNI","11/08/1999","sandra@correo.com","912345678","Jr. Tres Cruces 77",null), now - days(26) - hours(7)));
        list.add(new SaLogsAdapter.LogItem("Creó un tour", Role.ADMIN, new User("Carlos","Antama C.","70124567","Inca Tours",Role.ADMIN,"DNI","09/21/1990","carlos@incatours.com","988776655","Av. Inca 101",null), now - days(26) - hours(10)));
        
        return list;
    }
    private long days(int d) { return d * 24L * 60 * 60 * 1000; }
    private long hours(int h) { return h * 60L * 60 * 1000; }
}
