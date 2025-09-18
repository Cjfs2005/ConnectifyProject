package com.example.connectifyproject;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavFragment extends Fragment {

    // Interface para comunicar clicks al Activity padre
    public interface OnNavItemClickListener {
        void onNavItemClick(int itemId);
    }

    private OnNavItemClickListener listener;
    private BottomNavigationView bottomNav;

    // Datos de navegación hardcodeados
    private int[] navIcons = {
        R.drawable.ic_home,
        R.drawable.ic_search,
        R.drawable.ic_tours,
        R.drawable.ic_chat,
        R.drawable.ic_profile
    };

    private String[] navTitles = {
        "Inicio",
        "Reservas", 
        "Tours",
        "Chat",
        "Perfil"
    };

    private int[] navIds = {
        R.id.nav_home,
        R.id.nav_reservas,
        R.id.nav_tours,
        R.id.nav_chat,
        R.id.nav_perfil
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnNavItemClickListener) {
            listener = (OnNavItemClickListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_nav, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        bottomNav = view.findViewById(R.id.bottom_navigation);
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            if (listener != null) {
                listener.onNavItemClick(item.getItemId());
            }
            return true;
        });
    }

    // Método para marcar un item como seleccionado desde el Activity
    public void setSelectedItem(int itemId) {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(itemId);
        }
    }
}
