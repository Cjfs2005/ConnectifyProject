package com.example.connectifyproject.views.superadmin.profile;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.connectifyproject.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Lee argumentos: name, dni, company, role, birth, email, phone, address, photoUri
 * y los coloca en el layout aun si los ids varian.
 */
public class SaUserDetailFragment extends Fragment {

    public SaUserDetailFragment() {}

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sa_user_detail, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        Bundle args = getArguments() != null ? getArguments() : new Bundle();

        // ---- Foto de perfil (busca por varios ids posibles) ----
        ImageView iv = findImageView(v,
                "ivAvatar","iv_avatar","ivUser","iv_user","image_profile","imageUser","imgUser");
        String photoUri = args.getString("photoUri", null);
        if (iv != null && photoUri != null && !photoUri.isEmpty()) {
            try { iv.setImageURI(Uri.parse(photoUri)); } catch (Throwable ignore) {}
        }

        // ---- Setear textos en campos (TextInputEditText o EditText dentro de TextInputLayout) ----
        setText(v, args.getString("name", ""),
                new String[]{"etName","et_name","tietName","tiet_name"},
                new String[]{"tilName","til_name"});

        // Apellido si lo manejas aparte (si no, ignora)
        setText(v, extractLastName(args.getString("name", "")),
                new String[]{"etLastName","et_last_name","tietLastName","tiet_last_name"},
                new String[]{"tilLastName","til_last_name"});

        // Tipo de documento (si usas este campo de texto; si es Spinner, omite)
        setText(v, args.getString("role", ""),
                new String[]{"etDocType","et_doc_type","tietDocType","tiet_doc_type"},
                new String[]{"tilDocType","til_doc_type"});

        // DNI / número doc
        setText(v, args.getString("dni", ""),
                new String[]{"etDni","et_dni","etDocument","et_document","tietDni","tiet_document"},
                new String[]{"tilDni","til_dni","tilDocument","til_document"});

        // Fecha de nacimiento
        setText(v, args.getString("birth", ""),
                new String[]{"etBirth","et_birth","etFecha","et_fecha","tietBirth","tietFecha"},
                new String[]{"tilBirth","til_birth","tilFecha","til_fecha"});

        // Correo
        setText(v, args.getString("email", ""),
                new String[]{"etEmail","et_email","tietEmail","tiet_email"},
                new String[]{"tilEmail","til_email"});

        // Teléfono
        setText(v, args.getString("phone", ""),
                new String[]{"etPhone","et_phone","tietPhone","tiet_phone"},
                new String[]{"tilPhone","til_phone"});

        // Domicilio
        setText(v, args.getString("address", ""),
                new String[]{"etAddress","et_address","tietAddress","tiet_address"},
                new String[]{"tilAddress","til_address"});

        // ---- Botón back (si existe con cualquiera de estos ids) ----
        View btnBack = findView(v, "btnBack","ibBack","btn_back","toolbar_back","action_back");
        if (btnBack != null) {
            btnBack.setOnClickListener(view -> Navigation.findNavController(view).popBackStack());
        }
    }

    // Helpers -----------------------------------------------------------------

    /** Busca un view por una lista de ids posibles. Devuelve el primero que exista. */
    @Nullable
    private View findView(@NonNull View root, String... candidates) {
        for (String name : candidates) {
            int id = getId(root, name);
            if (id != 0) {
                View found = root.findViewById(id);
                if (found != null) return found;
            }
        }
        return null;
    }

    @Nullable
    private ImageView findImageView(@NonNull View root, String... candidates) {
        View v = findView(root, candidates);
        return (v instanceof ImageView) ? (ImageView) v : null;
    }

    /** Coloca texto en un TextInputEditText o en el EditText interno de un TextInputLayout. */
    private void setText(@NonNull View root, @Nullable String value,
                         String[] editIds, String[] tilIds) {
        // 1) intenta con TextInputEditText / EditText directos
        for (String name : editIds) {
            int id = getId(root, name);
            if (id != 0) {
                View v = root.findViewById(id);
                if (v instanceof TextInputEditText) {
                    ((TextInputEditText) v).setText(value == null ? "" : value);
                    return;
                } else if (v instanceof android.widget.EditText) {
                    ((android.widget.EditText) v).setText(value == null ? "" : value);
                    return;
                }
            }
        }
        // 2) intenta con TextInputLayout (y su EditText interno)
        for (String name : tilIds) {
            int id = getId(root, name);
            if (id != 0) {
                View v = root.findViewById(id);
                if (v instanceof TextInputLayout && ((TextInputLayout) v).getEditText() != null) {
                    ((TextInputLayout) v).getEditText().setText(value == null ? "" : value);
                    return;
                }
            }
        }
    }

    private int getId(@NonNull View root, @NonNull String name) {
        try {
            return root.getResources().getIdentifier(
                    name, "id", root.getContext().getPackageName());
        } catch (Throwable ignore) { return 0; }
    }

    private String extractLastName(@Nullable String full) {
        if (full == null) return "";
        String[] p = full.trim().split("\\s+");
        return p.length == 0 ? "" : p[p.length - 1];
    }
}
