package com.example.connectifyproject.views.superadmin.users;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.example.connectifyproject.utils.AuthConstants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class SaUsersFragment extends Fragment {

    private static final String TAG = "SaUsersFragment";

    private SaUsersAdapter adapter;
    private EnumSet<Role> selectedRoles = EnumSet.of(Role.GUIDE, Role.ADMIN, Role.CLIENT);
    private String currentQuery = "";

    private TextInputEditText etSearch;
    private MaterialButton btnRoles;
    private FloatingActionButton fabAdd;
    private ImageButton btnNotifications;
    
    private FirebaseFirestore db;
    private FirebaseStorage storage;

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
        
        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // --- Recycler ---
        RecyclerView rv = v.findViewById(R.id.rvUsers);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SaUsersAdapter(new ArrayList<>(), u -> {
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
        
        // Cargar usuarios desde Firebase
        loadUsersFromFirebase();
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
        db = null;
        storage = null;
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

    private void loadUsersFromFirebase() {
        // Primero cargar usuarios con perfil completo
        db.collection(AuthConstants.COLLECTION_USUARIOS)
                .whereEqualTo(AuthConstants.FIELD_PERFIL_COMPLETO, true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<User> users = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        User user = parseUserDocument(doc, true);
                        if (user != null) {
                            users.add(user);
                        }
                    }
                    
                    // Luego cargar administradores con perfil incompleto
                    db.collection(AuthConstants.COLLECTION_USUARIOS)
                            .whereEqualTo(AuthConstants.FIELD_PERFIL_COMPLETO, false)
                            .whereEqualTo(AuthConstants.FIELD_ROL, AuthConstants.ROLE_ADMIN)
                            .get()
                            .addOnSuccessListener(incompleteSnapshot -> {
                                for (DocumentSnapshot doc : incompleteSnapshot.getDocuments()) {
                                    User user = parseUserDocument(doc, false);
                                    if (user != null) {
                                        users.add(user);
                                    }
                                }
                                adapter.replaceAll(users);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error loading incomplete admins", e);
                                // Mostrar al menos los usuarios completos
                                adapter.replaceAll(users);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading users", e);
                });
    }
    
    private User parseUserDocument(DocumentSnapshot doc, boolean profileComplete) {
        try {
            String uid = doc.getId();
            String nombreCompleto = doc.getString(AuthConstants.FIELD_NOMBRE_COMPLETO);
            String dni = doc.getString(AuthConstants.FIELD_NUMERO_DOCUMENTO);
            String tipoDocumento = doc.getString(AuthConstants.FIELD_TIPO_DOCUMENTO);
            String rolStr = doc.getString(AuthConstants.FIELD_ROL);
            String email = doc.getString(AuthConstants.FIELD_EMAIL);
            String telefono = doc.getString(AuthConstants.FIELD_TELEFONO);
            String direccion = doc.getString(AuthConstants.FIELD_DOMICILIO);
            String fechaNacimiento = doc.getString(AuthConstants.FIELD_FECHA_NACIMIENTO);
            Boolean habilitado = doc.getBoolean(AuthConstants.FIELD_HABILITADO);
            String photoUrl = doc.getString(AuthConstants.FIELD_PHOTO_URL);
            String nombreEmpresa = doc.getString(AuthConstants.FIELD_NOMBRE_EMPRESA);
            
            // Si no hay foto, usar la foto por defecto de Firebase Storage
            if (photoUrl == null || photoUrl.isEmpty()) {
                photoUrl = AuthConstants.DEFAULT_PHOTO_HTTP_URL;
            }
            
            // Para admins con perfil incompleto, usar nombre especial
            String nombre = "";
            String apellidos = "";
            
            if (!profileComplete && rolStr != null && rolStr.equals(AuthConstants.ROLE_ADMIN)) {
                // Admin con perfil incompleto: "Administrador de <nombre empresa>"
                nombre = "Administrador de " + (nombreEmpresa != null ? nombreEmpresa : "Empresa");
                apellidos = "";
            } else {
                // Usuario normal: separar nombre y apellidos
                if (nombreCompleto != null && !nombreCompleto.isEmpty()) {
                    String[] partes = nombreCompleto.split(" ", 2);
                    nombre = partes[0];
                    if (partes.length > 1) {
                        apellidos = partes[1];
                    }
                }
            }
            
            // Convertir rol
            Role role = convertRole(rolStr);
            
            // Crear objeto User
            User user = new User(
                    nombre,
                    apellidos,
                    dni != null ? dni : "",
                    nombreEmpresa != null ? nombreEmpresa : "", // company
                    role,
                    tipoDocumento,
                    fechaNacimiento,
                    email,
                    telefono,
                    direccion,
                    photoUrl
            );
            
            user.setUid(uid);
            user.setEnabled(habilitado != null ? habilitado : true);
            user.setProfileComplete(profileComplete);
            
            return user;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing user document", e);
            return null;
        }
    }
    
    private Role convertRole(String rolStr) {
        if (rolStr == null) return Role.CLIENT;
        switch (rolStr) {
            case AuthConstants.ROLE_GUIA: return Role.GUIDE;
            case AuthConstants.ROLE_ADMIN: return Role.ADMIN;
            case AuthConstants.ROLE_CLIENTE: return Role.CLIENT;
            default: return Role.CLIENT;
        }
    }
}
