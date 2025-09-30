package com.example.connectifyproject.ui.guia;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectifyproject.databinding.GuiaItemLanguageBinding;
import com.example.connectifyproject.fragment.GuiaLanguageDialogFragment;
import com.example.connectifyproject.model.GuiaLanguage;

import java.util.List;

public class GuiaLanguageAdapter extends RecyclerView.Adapter<GuiaLanguageAdapter.ViewHolder> implements GuiaLanguageDialogFragment.OnLanguageUpdatedListener {
    private List<GuiaLanguage> languages;
    private Activity activity;

    public GuiaLanguageAdapter(Activity activity, List<GuiaLanguage> languages) {
        this.activity = activity;
        this.languages = languages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        GuiaItemLanguageBinding binding = GuiaItemLanguageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GuiaLanguage lang = languages.get(position);
        holder.binding.nameText.setText(lang.getName());

        holder.binding.editBtn.setOnClickListener(v -> {
            GuiaLanguageDialogFragment dialog = GuiaLanguageDialogFragment.newInstance(languages, this);
            dialog.setPosition(position);
            dialog.show(((androidx.appcompat.app.AppCompatActivity) activity).getSupportFragmentManager(), "edit_lang");
        });

        holder.binding.deleteBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Eliminar")
                    .setMessage("¿Eliminar " + lang.getName() + "?")
                    .setPositiveButton("Sí", (d, w) -> {
                        languages.remove(position);
                        onLanguagesUpdated(languages);
                        notifyItemRemoved(position);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return languages.size();
    }

    public void updateList(List<GuiaLanguage> newList) {
        languages = newList;
        notifyDataSetChanged();
    }

    @Override
    public void onLanguagesUpdated(List<GuiaLanguage> updatedLanguages) {
        updateList(updatedLanguages);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        GuiaItemLanguageBinding binding;

        ViewHolder(GuiaItemLanguageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}