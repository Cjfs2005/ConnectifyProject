package com.example.connectifyproject.views.superadmin.requests;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.R;
import com.example.connectifyproject.model.Role;
import com.example.connectifyproject.model.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class SaRequestsFragment extends Fragment {

    private SaGuideRequestsAdapter adapter;
    private SaGuideRequestsAdapter.SortOrder sort = SaGuideRequestsAdapter.SortOrder.RECENT;

    private ExtendedFloatingActionButton fabEnable;
    private TextInputEditText etSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sa_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        RecyclerView rv = v.findViewById(R.id.rvRequests);
        View btnSelectAll = v.findViewById(R.id.btnSelectAll);
        View btnSort      = v.findViewById(R.id.btnSort);
        etSearch          = v.findViewById(R.id.etSearch);
        fabEnable         = v.findViewById(R.id.fabEnable);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new SaGuideRequestsAdapter(buildMock(), new SaGuideRequestsAdapter.Listener() {
            @Override public void onSelectionChanged(int count) { refreshFab(); }
            @Override public void onOpen(SaGuideRequestsAdapter.GuideRequest req) {
                Bundle b = new Bundle();
                b.putParcelable("user", req.user);
                b.putLong("requestedAt", req.requestedAt);
                NavHostFragment.findNavController(SaRequestsFragment.this)
                        .navigate(R.id.saGuideRequestDetailFragment, b);
            }
        });
        rv.setAdapter(adapter);

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

        // ✅ Confirmación al habilitar selección múltiple
        fabEnable.setOnClickListener(view -> {
            int n = adapter.getSelectedCount();
            if (n == 0) return;

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Habilitar guías")
                    .setMessage("¿Habilitar " + n + " guía(s)?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Habilitar", (d, w) -> {
                        // TODO: llamada real al backend
                        adapter.selectAll(false);     // limpia selección
                        refreshFab();
                        Snackbar.make(view, "Guías habilitados", Snackbar.LENGTH_SHORT).show();
                    })
                    .show();
        });

        refreshFab();
    }

    private void refreshFab() {
        int n = (adapter == null) ? 0 : adapter.getSelectedCount();
        if (fabEnable == null) return;
        if (n > 0) fabEnable.show(); else fabEnable.hide();
    }

    private void showSortMenu(View anchor) {
        PopupMenu pm = new PopupMenu(requireContext(), anchor);
        pm.getMenuInflater().inflate(R.menu.menu_sa_requests_sort, pm.getMenu());
        pm.getMenu().findItem(R.id.sort_recent)
                .setChecked(sort == SaGuideRequestsAdapter.SortOrder.RECENT);
        pm.getMenu().findItem(R.id.sort_old)
                .setChecked(sort == SaGuideRequestsAdapter.SortOrder.OLD);
        pm.setOnMenuItemClickListener(this::onSortItem);
        pm.show();
    }

    private boolean onSortItem(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.sort_recent)      sort = SaGuideRequestsAdapter.SortOrder.RECENT;
        else if (id == R.id.sort_old)    sort = SaGuideRequestsAdapter.SortOrder.OLD;
        else return false;
        adapter.setSort(sort);
        refreshFab();
        return true;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        out.putBoolean("sortOld", sort == SaGuideRequestsAdapter.SortOrder.OLD);
        out.putString("q", etSearch.getText() == null ? "" : etSearch.getText().toString());
    }

    // ------- MOCK DATA -------
    private List<SaGuideRequestsAdapter.GuideRequest> buildMock() {
        List<SaGuideRequestsAdapter.GuideRequest> list = new ArrayList<>();
        list.add(req(new User("Rosa","Paye D.", "74561230","Cusco Guide", Role.GUIDE,"DNI","10/01/1995","rosa@cg.pe","999111222","Cusco",null), daysAgo(1)));
        list.add(req(new User("Nicolás","Zapata L.", "71230987","Inca Tours", Role.GUIDE,"DNI","03/11/1992","nico@incatours.com","988776655","Cusco",null), daysAgo(2)));
        list.add(req(new User("Sebastián","Flores F.", "70123456","Perú Travel", Role.GUIDE,"DNI","07/05/1990","seb@perutravel.com","977665544","Lima",null), daysAgo(0)));
        list.add(req(new User("Ricardo","Montero M.","75678901","Andes Corp", Role.GUIDE,"DNI","09/21/1994","rick@andescorp.pe","955667788","Arequipa",null), daysAgo(5)));
        list.add(req(new User("Laslo","Fernandez S.","70011223","Wayna Picchu", Role.GUIDE,"DNI","12/12/1993","laslo@wp.pe","933221100","Cusco",null), daysAgo(3)));
        list.add(req(new User("Samira","Ezaguirre L.","73456789","Cusco Guide", Role.GUIDE,"DNI","04/30/1997","samira@cg.pe","944556677","Cusco",null), daysAgo(8)));
        return list;
    }

    private SaGuideRequestsAdapter.GuideRequest req(User u, long when) {
        return new SaGuideRequestsAdapter.GuideRequest(u, when);
    }

    private long daysAgo(int d) {
        return System.currentTimeMillis() - d * 24L * 60 * 60 * 1000L;
    }
}
