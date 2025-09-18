package com.example.connectifyproject.ui.superadmin.logs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.connectifyproject.databinding.FragmentSaLogsBinding;

public class SaLogsFragment extends Fragment {

    private FragmentSaLogsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSaLogsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // TODO: UI de "Logs"
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
