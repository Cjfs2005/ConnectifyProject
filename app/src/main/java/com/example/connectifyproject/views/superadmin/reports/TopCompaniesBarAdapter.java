package com.example.connectifyproject.views.superadmin.reports;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.connectifyproject.databinding.ItemReportCompanyBarBinding;
import java.util.*;

public class TopCompaniesBarAdapter extends RecyclerView.Adapter<TopCompaniesBarAdapter.VH> {

    private final List<CompanyStat> data = new ArrayList<>();
    private int maxValue = 1;

    public void submit(List<CompanyStat> list){
        data.clear();
        if(list!=null) data.addAll(list);
        maxValue = 1;
        for(CompanyStat c: data) if(c.monthTotal>maxValue) maxValue = c.monthTotal;
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemReportCompanyBarBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        CompanyStat cs = data.get(pos);
        h.b.textEmpresa.setText(cs.name);
        h.b.textValor.setText(String.valueOf(cs.monthTotal));

        // barra proporcional simple (Material progress)
        int percent = (int) Math.round((cs.monthTotal * 100.0) / maxValue);
        h.b.progress.setProgress(percent);
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder{
        final ItemReportCompanyBarBinding b;
        VH(ItemReportCompanyBarBinding b){ super(b.getRoot()); this.b=b; }
    }
}
