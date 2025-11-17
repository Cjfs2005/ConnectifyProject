package com.example.connectifyproject.views.superadmin.reports;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Adapter para la lista de empresas en Reportes (SuperAdmin).
 * - Búsqueda por nombre (setQuery)
 * - Orden A-Z / Z-A (setAsc)
 * - Filtro por empresa específica ("ALL" o companyId) (setCompanyFilter)
 * - Carga directa desde CompanyStat (submitFromStats)
 *
 * Tolerante a IDs: intenta mapear a varios nombres de id comunes para evitar errores de R.id.
 */
public class SaReportsAdapter extends RecyclerView.Adapter<SaReportsAdapter.VH> {

    public interface Listener { void onDownload(CompanyItem item); }

    /** Item para la fila. */
    public static class CompanyItem {
        public final String id;    // companyId
        public final String name;  // nombre visible
        public final int count;    // reservas del mes (badge)
        public CompanyItem(String id, String name, int count) {
            this.id = id; this.name = name; this.count = count;
        }
    }

    private final List<CompanyItem> full = new ArrayList<>();
    private final List<CompanyItem> items = new ArrayList<>();
    private boolean asc = true;
    private String q = "";
    private String companyFilter = "ALL"; // "ALL" o companyId
    private final Listener listener;

    public SaReportsAdapter(List<CompanyItem> initial, Listener listener) {
        this.listener = listener;
        replaceAll(initial);
    }

    /** Carga lista ya mapeada a CompanyItem. */
    public void replaceAll(List<CompanyItem> data) {
        full.clear();
        if (data != null) full.addAll(data);
        apply();
    }

    /** Atajo para cargar desde CompanyStat. */
    public void submitFromStats(List<CompanyStat> stats) {
        List<CompanyItem> mapped = new ArrayList<>();
        if (stats != null) {
            for (CompanyStat cs : stats) {
                mapped.add(new CompanyItem(cs.companyId, cs.name, cs.monthTotal));
            }
        }
        replaceAll(mapped);
    }

    public void setQuery(String query) {
        this.q = query == null ? "" : query.trim().toLowerCase();
        apply();
    }

    public void setAsc(boolean asc) {
        this.asc = asc;
        apply();
    }

    /** Filtro por empresa. Usa "ALL" para todas. */
    public void setCompanyFilter(String companyIdOrAll) {
        this.companyFilter = (companyIdOrAll == null || companyIdOrAll.isEmpty()) ? "ALL" : companyIdOrAll;
        apply();
    }

    private void apply() {
        items.clear();
        for (CompanyItem it : full) {
            if (!"ALL".equals(companyFilter) && !it.id.equals(companyFilter)) continue;
            if (!q.isEmpty() && !it.name.toLowerCase().contains(q)) continue;
            items.add(it);
        }
        Comparator<CompanyItem> cmp = (a, b) -> a.name.compareToIgnoreCase(b.name);
        if (!asc) cmp = (a, b) -> b.name.compareToIgnoreCase(a.name);
        Collections.sort(items, cmp);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report_company, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        h.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() { return items.size(); }

    public CompanyItem getItem(int position) { return items.get(position); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName;       // Nombre de empresa
        TextView tvBadge;      // Valor (reservas del mes)
        ImageButton btnDownload; // Botón descargar (si existe)

        VH(@NonNull View itemView) {
            super(itemView);
            Context ctx = itemView.getContext();

            // Intentar varios ids para el nombre
            tvName = findText(itemView, ctx,
                    new String[]{"tvName", "textEmpresa", "tvCompany", "title", "tv_title"});

            // Intentar varios ids para el badge/valor
            tvBadge = findText(itemView, ctx,
                    new String[]{"tvBadge", "textTotalMes", "tvValue", "value", "tv_count"});

            // Intentar varios ids para el botón de descarga
            btnDownload = findImageButton(itemView, ctx,
                    new String[]{"btnDownload", "buttonDownload", "btn_descargar", "btnAction"});
        }

        void bind(CompanyItem item, Listener listener) {
            if (tvName != null) tvName.setText(item.name);
            if (tvBadge != null) tvBadge.setText(String.valueOf(item.count));

            // Si no existe botón, hacemos que el click en la fila "simule" descarga
            if (btnDownload != null) {
                btnDownload.setOnClickListener(v -> {
                    if (listener != null) listener.onDownload(item);
                });
                itemView.setOnClickListener(v -> btnDownload.performClick());
            } else {
                itemView.setOnClickListener(v -> {
                    if (listener != null) listener.onDownload(item);
                });
            }
        }

        // ---- utilidades tolerant a ids ----
        private static TextView findText(View root, Context ctx, String[] ids) {
            for (String name : ids) {
                @IdRes int resId = ctx.getResources().getIdentifier(name, "id", ctx.getPackageName());
                if (resId != 0) {
                    View v = root.findViewById(resId);
                    if (v instanceof TextView) return (TextView) v;
                }
            }
            return null;
        }

        private static ImageButton findImageButton(View root, Context ctx, String[] ids) {
            for (String name : ids) {
                @IdRes int resId = ctx.getResources().getIdentifier(name, "id", ctx.getPackageName());
                if (resId != 0) {
                    View v = root.findViewById(resId);
                    if (v instanceof ImageButton) return (ImageButton) v;
                }
            }
            return null;
        }
    }
}
