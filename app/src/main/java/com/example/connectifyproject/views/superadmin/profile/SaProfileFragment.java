// com/example/connectifyproject/views/superadmin/profile/SaProfileFragment.java
package com.example.connectifyproject.views.superadmin.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.connectifyproject.R;

public class SaProfileFragment extends Fragment {
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup c, @Nullable Bundle s) {
        return inflater.inflate(R.layout.fragment_sa_profile, c, false);
    }
}
