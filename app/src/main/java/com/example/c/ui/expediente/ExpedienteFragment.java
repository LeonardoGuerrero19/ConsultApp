package com.example.c.ui.expediente;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.c.databinding.FragmentExpedienteBinding;

public class ExpedienteFragment extends Fragment {

    private FragmentExpedienteBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ExpedienteViewModel expedienteViewModel =
                new ViewModelProvider(this).get(ExpedienteViewModel.class);

        binding = FragmentExpedienteBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textExpediente;
        expedienteViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Libera la referencia al binding
    }
}
