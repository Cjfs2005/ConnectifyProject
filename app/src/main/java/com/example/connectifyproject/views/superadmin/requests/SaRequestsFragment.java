package com.example.connectifyproject.views.superadmin.requests;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SaRequestsFragment extends Fragment {

    private static final String TAG = "SaRequestsFragment";
    private FirebaseFirestore db;
    private SaGuideRequestsAdapter adapter;
    private SaGuideRequestsAdapter.SortOrder sort = SaGuideRequestsAdapter.SortOrder.RECENT;

    private ExtendedFloatingActionButton fabEnable;
    private TextInputEditText etSearch;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        return inflater.inflate(R.layout.fragment_sa_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        final NavController nav = NavHostFragment.findNavController(this);

        RecyclerView rv   = v.findViewById(R.id.rvRequests);
        View btnSelectAll = v.findViewById(R.id.btnSelectAll);
        View btnSort      = v.findViewById(R.id.btnSort);
        etSearch          = v.findViewById(R.id.etSearch);
        fabEnable         = v.findViewById(R.id.fabEnable);

        // 🔔 Campanita → Notificaciones (enviamos fromDestId)
        View bell = v.findViewById(R.id.btnNotifications);
        if (bell != null) {
            bell.setOnClickListener(x -> {
                Bundle args = new Bundle();
                args.putInt("fromDestId", nav.getCurrentDestination() != null ? nav.getCurrentDestination().getId() : 0);
                nav.navigate(R.id.saNotificationsFragment, args);
            });
        }

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SaGuideRequestsAdapter(new ArrayList<>(), new SaGuideRequestsAdapter.Listener() {
            @Override public void onSelectionChanged(int count) { refreshFab(); }
            @Override public void onOpen(SaGuideRequestsAdapter.GuideRequest req) {
                Bundle b = new Bundle();
                b.putParcelable("user", req.user);
                b.putLong("requestedAt", req.requestedAt);
                nav.navigate(R.id.saGuideRequestDetailFragment, b);
            }
        });
        rv.setAdapter(adapter);
        
        // Cargar guías no habilitados desde Firestore
        loadPendingGuides();

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override public void onChanged() { refreshFab(); }
        });

        if (savedInstanceState != null) {
            sort = savedInstanceState.getBoolean("sortOld", false)
                    ? SaGuideRequestsAdapter.SortOrder.OLD
                    : SaGuideRequestsAdapter.SortOrder.RECENT;
            adapter.setSort(sort);

            String q = savedInstanceState.getString("q", "");
            if (etSearch != null) etSearch.setText(q);
            adapter.setQuery(q);
        }

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                adapter.setQuery(s == null ? "" : s.toString());
                refreshFab();
            }
        });

        btnSelectAll.setOnClickListener(view -> {
            boolean selectAll = adapter.getSelectedCount() < adapter.getItemCount();
            adapter.selectAll(selectAll);
            refreshFab();
        });

        btnSort.setOnClickListener(this::showSortMenu);

        fabEnable.setOnClickListener(view -> {
            int n = adapter.getSelectedCount();
            if (n == 0) return;

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Habilitar guías")
                    .setMessage("¿Habilitar " + n + " guía(s)?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Habilitar", (d, w) -> {
                        enableSelectedGuides();
                    })
                    .show();
        });

        refreshFab();
    }

    private void showSortMenu(View anchor) {
        PopupMenu pm = new PopupMenu(requireContext(), anchor);
        pm.getMenuInflater().inflate(R.menu.menu_sa_requests_sort, pm.getMenu());

        pm.getMenu().findItem(R.id.sort_recent).setChecked(sort == SaGuideRequestsAdapter.SortOrder.RECENT);
        pm.getMenu().findItem(R.id.sort_old).setChecked(sort == SaGuideRequestsAdapter.SortOrder.OLD);

        pm.setOnMenuItemClickListener(this::onSortItem);
        pm.show();
    }

    private boolean onSortItem(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.sort_recent) {
            sort = SaGuideRequestsAdapter.SortOrder.RECENT;
            item.setChecked(true);
        } else if (id == R.id.sort_old) {
            sort = SaGuideRequestsAdapter.SortOrder.OLD;
            item.setChecked(true);
        } else {
            return false;
        }
        adapter.setSort(sort);
        refreshFab();
        return true;
    }

    private void refreshFab() {
        int n = (adapter == null) ? 0 : adapter.getSelectedCount();
        if (fabEnable == null) return;
        if (n > 0) fabEnable.show(); else fabEnable.hide();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        out.putBoolean("sortOld", sort == SaGuideRequestsAdapter.SortOrder.OLD);
        out.putString("q", etSearch.getText() == null ? "" : etSearch.getText().toString());
    }

    // ------- FIREBASE METHODS -------
    private void loadPendingGuides() {
        db.collection(AuthConstants.COLLECTION_USUARIOS)
                .whereEqualTo(AuthConstants.FIELD_ROL, AuthConstants.ROLE_GUIA)
                .whereEqualTo(AuthConstants.FIELD_HABILITADO, false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<SaGuideRequestsAdapter.GuideRequest> requests = new ArrayList<>();
                    
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        try {
                            String nombre = doc.getString(AuthConstants.FIELD_NOMBRE_COMPLETO);
                            String[] parts = nombre != null ? nombre.split(" ", 2) : new String[]{"", ""};
                            String firstName = parts.length > 0 ? parts[0] : "";
                            String lastName = parts.length > 1 ? parts[1] : "";
                            
                            String dni = doc.getString(AuthConstants.FIELD_NUMERO_DOCUMENTO);
                            String tipoDoc = doc.getString(AuthConstants.FIELD_TIPO_DOCUMENTO);
                            String email = doc.getString(AuthConstants.FIELD_EMAIL);
                            String phone = doc.getString(AuthConstants.FIELD_TELEFONO);
                            String address = doc.getString(AuthConstants.FIELD_DOMICILIO);
                            String photoUrl = doc.getString(AuthConstants.FIELD_PHOTO_URL);
                            
                            // Obtener timestamp de creación
                            Timestamp fechaCreacion = doc.getTimestamp(AuthConstants.FIELD_FECHA_CREACION);
                            long requestedAt = fechaCreacion != null ? fechaCreacion.toDate().getTime() : System.currentTimeMillis();
                            
                            User user = new User(
                                    firstName,
                                    lastName,
                                    dni,
                                    "", // company (no aplica para guías)
                                    Role.GUIDE,
                                    tipoDoc,
                                    doc.getString(AuthConstants.FIELD_FECHA_NACIMIENTO),
                                    email,
                                    phone,
                                    address,
                                    photoUrl
                            );
                            user.setUid(doc.getId());
                            
                            requests.add(new SaGuideRequestsAdapter.GuideRequest(user, requestedAt));
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing guide document: " + doc.getId(), e);
                        }
                    }
                    
                    adapter.replaceAll(requests);
                    Log.d(TAG, "Loaded " + requests.size() + " pending guides");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading pending guides", e);
                    Snackbar.make(requireView(), "Error al cargar solicitudes", Snackbar.LENGTH_SHORT).show();
                });
    }
    
    private void enableSelectedGuides() {
        List<SaGuideRequestsAdapter.GuideRequest> selected = adapter.getSelected();
        if (selected.isEmpty()) return;
        
        int total = selected.size();
        int[] completed = {0};
        
        for (SaGuideRequestsAdapter.GuideRequest req : selected) {
            String uid = req.user.getUid();
            if (uid == null) {
                Log.w(TAG, "Guide has no UID, skipping");
                completed[0]++;
                continue;
            }
            
            db.collection(AuthConstants.COLLECTION_USUARIOS)
                    .document(uid)
                    .update(AuthConstants.FIELD_HABILITADO, true)
                    .addOnSuccessListener(aVoid -> {
                        completed[0]++;
                        Log.d(TAG, "Guide enabled: " + uid);
                        
                        if (completed[0] == total) {
                            // Recargar lista
                            loadPendingGuides();
                            adapter.selectAll(false);
                            refreshFab();
                            Snackbar.make(requireView(), total + " guía(s) habilitado(s)", Snackbar.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        completed[0]++;
                        Log.e(TAG, "Error enabling guide: " + uid, e);
                        
                        if (completed[0] == total) {
                            Snackbar.make(requireView(), "Error al habilitar algunos guías", Snackbar.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar referencias para evitar memory leaks
        fabEnable = null;
        etSearch = null;
        adapter = null;
        db = null;
    }
}
