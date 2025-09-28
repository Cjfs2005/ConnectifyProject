package com.example.connectifyproject.views.superadmin.logs;

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
import com.example.connectifyproject.model.Role;
import com.example.connectifyproject.model.User;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SaLogDetailFragment extends Fragment {

    private User user;
    private long at;
    private String action;
    private Role role;

    private ImageButton btnBack;

    private TextInputEditText etAction, etDate, etTime;
    private TextInputEditText etFirstName, etLastName, etUserRole, etDocType, etDocNumber,
            etBirth, etEmail, etPhone, etAddress;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sa_log_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            user = args.getParcelable("user");
            at = args.getLong("at", System.currentTimeMillis());
            action = args.getString("action", "");
            String r = args.getString("role", Role.CLIENT.name());
            role = Role.valueOf(r);
        }

        btnBack = root.findViewById(R.id.btnBack);

        etAction = root.findViewById(R.id.etAction);
        etDate   = root.findViewById(R.id.etDate);
        etTime   = root.findViewById(R.id.etTime);

        etFirstName = root.findViewById(R.id.etFirstName);
        etLastName  = root.findViewById(R.id.etLastName);
        etUserRole  = root.findViewById(R.id.etUserRole);
        etDocType   = root.findViewById(R.id.etDocType);
        etDocNumber = root.findViewById(R.id.etDocNumber);
        etBirth     = root.findViewById(R.id.etBirth);
        etEmail     = root.findViewById(R.id.etEmail);
        etPhone     = root.findViewById(R.id.etPhone);
        etAddress   = root.findViewById(R.id.etAddress);

        render();

        btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp()
        );
    }

    private void render() {
        // Acción/fecha/hora
        etAction.setText(action);
        Date d = new Date(at);
        etDate.setText(new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(d));
        etTime.setText(new SimpleDateFormat("hh:mm a", Locale.US).format(d));

        if (user == null) return;

        // Nombre / apellido
        String first = user.getName() == null ? "" : user.getName();
        String last  = user.getLastName() == null ? "" : user.getLastName();
        if (last.isEmpty()) {
            int sp = first.indexOf(' ');
            if (sp > 0) {
                last  = first.substring(sp + 1).trim();
                first = first.substring(0, sp).trim();
            }
        }
        etFirstName.setText(first);
        etLastName.setText(last);

        // Rol
        etUserRole.setText(roleToHuman(role));

        // Documentos / contacto
        etDocType.setText(user.getDocType() != null ? user.getDocType() : "DNI");
        etDocNumber.setText(nvl(user.getDni()));
        etBirth.setText(nvl(user.getBirth()));
        etEmail.setText(nvl(user.getEmail()));
        etPhone.setText(nvl(user.getPhone()));
        etAddress.setText(nvl(user.getAddress()));

        setReadOnly(etAction, etDate, etTime, etFirstName, etLastName, etUserRole,
                etDocType, etDocNumber, etBirth, etEmail, etPhone, etAddress);
    }

    private static String roleToHuman(Role r) {
        switch (r) {
            case ADMIN: return "Administrador";
            case GUIDE: return "Guía";
            case CLIENT:
            default: return "Cliente";
        }
    }

    private static String nvl(String s) { return s == null ? "" : s; }

    private void setReadOnly(TextInputEditText... edits) {
        for (TextInputEditText e : edits) {
            e.setFocusable(false);
            e.setClickable(false);
            e.setLongClickable(false);
        }
    }
}
