package com.example.connectifyproject.views.superadmin;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.connectifyproject.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SaUsersView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sa_users_view);

        BottomNavigationView bottom = findViewById(R.id.bottom_nav_sa);
        NavController nav = Navigation.findNavController(this, R.id.nav_host_sa);
        NavigationUI.setupWithNavController(bottom, nav);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController nav = Navigation.findNavController(this, R.id.nav_host_sa);
        return nav.navigateUp() || super.onSupportNavigateUp();
    }
}
