package com.example.connectifyproject.views.superadmin.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.connectifyproject.R;
import com.example.connectifyproject.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class SaUserDetailFragment extends Fragment {

    private TextInputEditText etFirstName, etLastName, etDocType, etDocNumber,
            etBirth, etEmail, etPhone, etAddress;

    private MaterialButton btnActivate, btnDeactivate;
    private ImageButton btnBack;

    private boolean isActive = true;   // estado simple
    private User user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sa_user_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        // 1) Bind de vistas
        btnBack       = root.findViewById(R.id.btnBack);
        btnActivate   = root.findViewById(R.id.btnActivate);
        btnDeactivate = root.findViewById(R.id.btnDeactivate);

        etFirstName = root.findViewById(R.id.etFirstName);
        etLastName  = root.findViewById(R.id.etLastName);
        etDocType   = root.findViewById(R.id.etDocType);
        etDocNumber = root.findViewById(R.id.etDocNumber);
        etBirth     = root.findViewById(R.id.etBirth);
        etEmail     = root.findViewById(R.id.etEmail);
        etPhone     = root.findViewById(R.id.etPhone);
        etAddress   = root.findViewById(R.id.etAddress);

        // 2) Traer el usuario enviado por la lista (Parcelable o Serializable)
        Bundle args = getArguments();
        if (args != null) {
            // Preferir Parcelable si tu User lo implementa
            user = args.getParcelable("user");
            if (user == null) {
                Object obj = args.getSerializable("user");
                if (obj instanceof User) user = (User) obj;
            }
            if (args.containsKey("active")) {
                isActive = args.getBoolean("active", true);
            }
        }

        // 3) Pintar datos
        renderUser();

        // 4) Botón atrás
        btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp()
        );

        // 5) Activar / Desactivar (con feedback)
        btnDeactivate.setOnClickListener(v -> {
            isActive = false;
            updateButtons();
            Snackbar.make(root, "🛑 Usuario desactivado", Snackbar.LENGTH_LONG).show();
        });

        btnActivate.setOnClickListener(v -> {
            isActive = true;
            updateButtons();
            Snackbar.make(root, "✅ Usuario activado", Snackbar.LENGTH_LONG).show();
        });

        if (savedInstanceState != null) {
            isActive = savedInstanceState.getBoolean("active_state", isActive);
        }
        updateButtons();
    }

    private void renderUser() {
        if (user == null) return;

        // Usa name + lastName del modelo; si lastName viene null, intenta separar el name
        String first = nvl(user.getName());
        String last  = nvl(user.getLastName());
        if (last.isEmpty()) {
            int sp = first.indexOf(' ');
            if (sp > 0) {
                last = first.substring(sp + 1).trim();
                first = first.substring(0, sp).trim();
            }
        }

        etFirstName.setText(first);
        etLastName.setText(last);

        String docType = user.getDocType() != null ? user.getDocType() : "DNI";
        etDocType.setText(docType);

        etDocNumber.setText(nvl(user.getDni()));
        etBirth.setText(nvl(user.getBirth()));    // ✅ antes usabas getBirthDate()
        etEmail.setText(nvl(user.getEmail()));
        etPhone.setText(nvl(user.getPhone()));
        etAddress.setText(nvl(user.getAddress()));
    }

    private void updateButtons() {
        btnActivate.setEnabled(!isActive);
        btnDeactivate.setEnabled(isActive);
    }

    private static String nvl(String s) { return s == null ? "" : s; }

    @Override
    public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        out.putBoolean("active_state", isActive);
    }
}
