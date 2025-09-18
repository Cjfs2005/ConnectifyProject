package com.example.connectifyproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

/**
 * Fragment reutilizable para el menú de navegación inferior del cliente
 */
public class cliente_fragment_menu extends Fragment {
    
    private BottomNavigationView bottomNavigation;
    private OnMenuItemSelectedListener listener;
    private int pendingSelectedItemId = -1; // Para guardar selección pendiente
    
    public interface OnMenuItemSelectedListener {
        boolean onMenuItemSelected(int itemId);
    }
    
    public void setOnMenuItemSelectedListener(OnMenuItemSelectedListener listener) {
        this.listener = listener;
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cliente_fragment_menu, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        bottomNavigation = view.findViewById(R.id.cliente_bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(navListener);
        
        // Si tenemos una selección pendiente, aplicarla ahora
        if (pendingSelectedItemId != -1) {
            bottomNavigation.setSelectedItemId(pendingSelectedItemId);
            pendingSelectedItemId = -1;
        }
    }
    
    public void setSelectedItem(int itemId) {
        if (bottomNavigation != null) {
            // La vista está lista, seleccionar inmediatamente
            bottomNavigation.setSelectedItemId(itemId);
        } else {
            // La vista no está lista, guardar para más tarde
            pendingSelectedItemId = itemId;
        }
    }
    
    private final NavigationBarView.OnItemSelectedListener navListener = item -> {
        if (listener != null) {
            return listener.onMenuItemSelected(item.getItemId());
        }
        return false;
    };
}