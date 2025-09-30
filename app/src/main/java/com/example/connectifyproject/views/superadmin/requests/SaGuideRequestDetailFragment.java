package com.example.connectifyproject.views.superadmin.requests;

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SaGuideRequestDetailFragment extends Fragment {

    private User user;
    private long requestedAt;

    private TextInputEditText etReqDate, etReqTime;
    private TextInputEditText etFirstName, etLastName, etDocType, etDocNumber,
            etBirth, etEmail, etPhone, etAddress;

    private MaterialButton btnAccept, btnDeny;
    private ImageButton btnBack;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sa_request_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        // Args
        Bundle args = getArguments();
        if (args != null) {
            user = args.getParcelable("user");
            requestedAt = args.getLong("requestedAt", 0L);
        }

        // Bind
        btnBack   = root.findViewById(R.id.btnBack);
        btnAccept = root.findViewById(R.id.btnAccept);
        btnDeny   = root.findViewById(R.id.btnDeny);

        etReqDate  = root.findViewById(R.id.etReqDate);
        etReqTime  = root.findViewById(R.id.etReqTime);

        etFirstName = root.findViewById(R.id.etFirstName);
        etLastName  = root.findViewById(R.id.etLastName);
        etDocType   = root.findViewById(R.id.etDocType);
        etDocNumber = root.findViewById(R.id.etDocNumber);
        etBirth     = root.findViewById(R.id.etBirth);
        etEmail     = root.findViewById(R.id.etEmail);
        etPhone     = root.findViewById(R.id.etPhone);
        etAddress   = root.findViewById(R.id.etAddress);

        render();

        // Back
        btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp()
        );

        // Aceptar con confirmación
        btnAccept.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Habilitar guía")
                        .setMessage("¿Aceptar la solicitud?")
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Aceptar", (d, w) -> {
                            // TODO: llamada real al backend
                            Snackbar.make(root, "Guía habilitado", Snackbar.LENGTH_SHORT).show();
                            NavHostFragment.findNavController(this).navigateUp();
                        })
                        .show()
        );

        // Denegar con confirmación
        btnDeny.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Denegar solicitud")
                        .setMessage("¿Denegar la solicitud de guía?")
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Denegar", (d, w) -> {
                            // TODO: llamada real al backend
                            Snackbar.make(root, "Solicitud denegada", Snackbar.LENGTH_SHORT).show();
                            NavHostFragment.findNavController(this).navigateUp();
                        })
                        .show()
        );

        setReadOnly(etReqDate, etReqTime, etFirstName, etLastName, etDocType, etDocNumber,
                etBirth, etEmail, etPhone, etAddress);
    }

    private void render() {
        Date date = requestedAt > 0 ? new Date(requestedAt) : new Date();
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        SimpleDateFormat tf = new SimpleDateFormat("hh:mm a", Locale.US);
        etReqDate.setText(df.format(date));
        etReqTime.setText(tf.format(date));

        if (user == null) return;

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
        etDocType.setText(user.getDocType() != null ? user.getDocType() : "DNI");
        etDocNumber.setText(nvl(user.getDni()));
        etBirth.setText(nvl(user.getBirth()));
        etEmail.setText(nvl(user.getEmail()));
        etPhone.setText(nvl(user.getPhone()));
        etAddress.setText(nvl(user.getAddress()));
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
