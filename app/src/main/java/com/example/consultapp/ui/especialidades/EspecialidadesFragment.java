package com.example.consultapp.ui.especialidades;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.consultapp.databinding.FragmentEspecialidadesAdminBinding;

public class EspecialidadesFragment extends Fragment {

    private FragmentEspecialidadesAdminBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        EspecialidadesViewModel especialidadesViewModel =
                new ViewModelProvider(this).get(EspecialidadesViewModel.class);

        binding = FragmentEspecialidadesAdminBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}