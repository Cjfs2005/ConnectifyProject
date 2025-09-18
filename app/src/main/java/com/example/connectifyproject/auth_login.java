package com.example.connectifyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.connectifyproject.databinding.MainLoginViewBinding;

public class auth_login extends AppCompatActivity {

    private MainLoginViewBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding del layout main_login_view.xml
        binding = MainLoginViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Click en “Iniciar sesión”
        binding.btnLogin.setOnClickListener(v -> tryLogin());

        // Enter/Done en el password también intenta login
        binding.etPassword.setOnEditorActionListener((tv, actionId, event) -> {
            boolean pressedEnter =
                    actionId == EditorInfo.IME_ACTION_DONE ||
                            (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                                    && event.getAction() == KeyEvent.ACTION_DOWN);
            if (pressedEnter) {
                tryLogin();
                return true;
            }
            return false;
        });
    }

    /** Valida inputs y, si todo ok, navega a MainActivity */
    private void tryLogin() {
        // limpia errores
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);

        String email = binding.etEmail.getText() == null ? "" : binding.etEmail.getText().toString().trim();
        String pass  = binding.etPassword.getText() == null ? "" : binding.etPassword.getText().toString();

        boolean ok = true;
        if (email.isEmpty()) {
            binding.tilEmail.setError("Ingresa tu correo");
            ok = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Correo inválido");
            ok = false;
        }

        if (pass.isEmpty()) {
            binding.tilPassword.setError("Ingresa tu contraseña");
            ok = false;
        }

        if (!ok) return;

        // TODO: aquí iría tu llamada real a API/Firebase
        goToHome();
    }

    /** Navega al host del flujo (NavHost + BottomNav) */
    private void goToHome() {
        Intent i = new Intent(auth_login.this, MainActivity.class);
        startActivity(i);
        finish(); // evita volver al login con back
    }
}
