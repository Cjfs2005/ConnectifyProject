package com.example.connectifyproject.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.connectifyproject.R;
import com.example.connectifyproject.admin_chat;
import com.example.connectifyproject.admin_dashboard;
import com.example.connectifyproject.admin_pagos;
import com.example.connectifyproject.admin_perfil;
import com.example.connectifyproject.admin_tours;
import com.google.android.material.navigation.NavigationBarView;

public class AdminBottomNavFragment extends Fragment {
    
    private NavigationBarView bottomNav;
    private String currentPage = "dashboard"; // Por defecto dashboard
    
    public static AdminBottomNavFragment newInstance(String currentPage) {
        AdminBottomNavFragment fragment = new AdminBottomNavFragment();
        Bundle args = new Bundle();
        args.putString("current_page", currentPage);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentPage = getArguments().getString("current_page", "dashboard");
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_bottom_nav_fragment, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        bottomNav = view.findViewById(R.id.bottomNav);
        
        // Configurar el item seleccionado basado en la página actual
        setSelectedMenuItem(currentPage);
        
        bottomNav.setOnItemSelectedListener(navListener);
    }
    
    private void setSelectedMenuItem(String page) {
        switch (page) {
            case "dashboard":
                bottomNav.setSelectedItemId(R.id.nav_dashboard);
                break;
            case "tours":
                bottomNav.setSelectedItemId(R.id.nav_tours);
                break;
            case "chat":
                bottomNav.setSelectedItemId(R.id.nav_chat);
                break;
            case "pagos":
                bottomNav.setSelectedItemId(R.id.nav_pagos);
                break;
            case "perfil":
                bottomNav.setSelectedItemId(R.id.nav_perfil);
                break;
        }
    }
    
    private final NavigationBarView.OnItemSelectedListener navListener = item -> {
        if (getActivity() == null) return false;
        
        int id = item.getItemId();
        Intent intent = null;
        
        if (id == R.id.nav_dashboard && !currentPage.equals("dashboard")) {
            intent = new Intent(getActivity(), admin_dashboard.class);
        } else if (id == R.id.nav_tours && !currentPage.equals("tours")) {
            intent = new Intent(getActivity(), admin_tours.class);
        } else if (id == R.id.nav_chat && !currentPage.equals("chat")) {
            intent = new Intent(getActivity(), admin_chat.class);
        } else if (id == R.id.nav_pagos && !currentPage.equals("pagos")) {
            intent = new Intent(getActivity(), admin_pagos.class);
        } else if (id == R.id.nav_perfil && !currentPage.equals("perfil")) {
            intent = new Intent(getActivity(), admin_perfil.class);
        }
        
        if (intent != null) {
            startActivity(intent);
            return true;
        }
        
        return false;
    };
}
