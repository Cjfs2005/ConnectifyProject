package com.example.connectifyproject.views.superadmin.requests;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.connectifyproject.R;
import com.example.connectifyproject.model.User;
import com.example.connectifyproject.utils.AuthConstants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SaGuideRequestDetailFragment extends Fragment {

    private static final String TAG = "SaGuideRequestDetail";
    private FirebaseFirestore db;
    private User user;
    private long requestedAt;

    private ImageView ivProfilePhoto;
    private TextInputEditText etReqDate, etReqTime;
    private TextInputEditText etFullName, etDocType, etDocNumber,
            etBirth, etEmail, etPhone, etAddress, etLanguages;

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

        db = FirebaseFirestore.getInstance();

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

        ivProfilePhoto = root.findViewById(R.id.ivProfilePhoto);
        
        etReqDate  = root.findViewById(R.id.etReqDate);
        etReqTime  = root.findViewById(R.id.etReqTime);

        etFullName  = root.findViewById(R.id.etFullName);
        etDocType   = root.findViewById(R.id.etDocType);
        etDocNumber = root.findViewById(R.id.etDocNumber);
        etBirth     = root.findViewById(R.id.etBirth);
        etEmail     = root.findViewById(R.id.etEmail);
        etPhone     = root.findViewById(R.id.etPhone);
        etAddress   = root.findViewById(R.id.etAddress);
        etLanguages = root.findViewById(R.id.etLanguages);

        setReadOnlyFields();
        render();

        // Back
        btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp()
        );

        // Aceptar con confirmación
        btnAccept.setOnClickListener(v -> showAcceptDialog());

        // Rechazar con confirmación
        btnDeny.setOnClickListener(v -> showRejectDialog());
    }

    private void setReadOnlyFields() {
        setReadOnly(etReqDate, etReqTime, etFullName, etDocType, etDocNumber,
                etBirth, etEmail, etPhone, etAddress, etLanguages);
    }

    private void render() {
        // Request date - formato DD/MM/YYYY
        Date date = requestedAt > 0 ? new Date(requestedAt) : new Date();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat tf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        etReqDate.setText(df.format(date));
        etReqTime.setText(tf.format(date));

        if (user == null) return;

        // Load profile photo
        Glide.with(this)
                .load(user.getPhotoUri())
                .circleCrop()
                .placeholder(R.drawable.ic_account_circle_24)
                .error(R.drawable.ic_account_circle_24)
                .into(ivProfilePhoto);

        // Full name
        String fullName = (user.getName() != null ? user.getName() : "") + " " + 
                         (user.getLastName() != null ? user.getLastName() : "");
        etFullName.setText(fullName.trim());
        
        // Document
        etDocType.setText(user.getDocType() != null ? user.getDocType() : "DNI");
        etDocNumber.setText(nvl(user.getDni()));
        
        // Birth date
        etBirth.setText(nvl(user.getBirth()));
        
        // Email
        etEmail.setText(nvl(user.getEmail()));
        
        // Address
        etAddress.setText(nvl(user.getAddress()));

        // Load additional data from Firestore (phone with country code & idiomas)
        loadAdditionalData();
    }

    private void loadAdditionalData() {
        if (user == null || user.getUid() == null) {
            etPhone.setText(nvl(user.getPhone()));
            etLanguages.setText("");
            return;
        }

        db.collection(AuthConstants.COLLECTION_USUARIOS)
                .document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        // Phone with country code
                        String phone = document.getString(AuthConstants.FIELD_TELEFONO);
                        String countryCode = document.getString(AuthConstants.FIELD_CODIGO_PAIS);
                        if (countryCode != null && phone != null && !countryCode.isEmpty() && !phone.isEmpty()) {
                            etPhone.setText(countryCode + " " + phone);
                        } else if (phone != null) {
                            etPhone.setText(phone);
                        } else {
                            etPhone.setText(nvl(user.getPhone()));
                        }
                        
                        // Idiomas
                        List<String> idiomas = (List<String>) document.get(AuthConstants.FIELD_IDIOMAS);
                        if (idiomas != null && !idiomas.isEmpty()) {
                            etLanguages.setText(String.join(", ", idiomas));
                        } else {
                            etLanguages.setText("");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading additional data", e);
                    etPhone.setText(nvl(user.getPhone()));
                    etLanguages.setText("");
                });
    }

    private void showAcceptDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Habilitar guía")
                .setMessage("¿Aceptar la solicitud de este guía?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Aceptar", (d, w) -> enableGuide())
                .show();
    }

    private void showRejectDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Rechazar solicitud")
                .setMessage("¿Rechazar la solicitud? El usuario permanecerá sin habilitar.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Rechazar", (d, w) -> {
                    // Solo cierra la pantalla, el usuario queda con habilitado=false
                    Snackbar.make(requireView(), "Solicitud rechazada", Snackbar.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(this).navigateUp();
                })
                .show();
    }

    private void enableGuide() {
        if (user == null || user.getUid() == null) {
            Snackbar.make(requireView(), "Error: usuario inválido", Snackbar.LENGTH_SHORT).show();
            return;
        }

        db.collection(AuthConstants.COLLECTION_USUARIOS)
                .document(user.getUid())
                .update(AuthConstants.FIELD_HABILITADO, true)
                .addOnSuccessListener(unused -> {
                    Snackbar.make(requireView(), "Guía habilitado exitosamente", Snackbar.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(this).navigateUp();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error enabling guide", e);
                    Snackbar.make(requireView(), "Error al habilitar guía", Snackbar.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar referencias para evitar memory leaks
        ivProfilePhoto = null;
        etReqDate = null;
        etReqTime = null;
        etFullName = null;
        etDocType = null;
        etDocNumber = null;
        etBirth = null;
        etEmail = null;
        etPhone = null;
        etAddress = null;
        etLanguages = null;
        btnAccept = null;
        btnDeny = null;
        btnBack = null;
        db = null;
        user = null;
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
