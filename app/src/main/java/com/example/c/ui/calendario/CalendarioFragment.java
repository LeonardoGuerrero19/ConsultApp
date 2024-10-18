package com.example.c.ui.calendario;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.c.databinding.FragmentCalendarioBinding;

public class CalendarioFragment extends Fragment {

    private FragmentCalendarioBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CalendarioViewModel calendarioViewModel =
                new ViewModelProvider(this).get(CalendarioViewModel.class);

        binding = FragmentCalendarioBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Obtener referencias de los botones
        final TextView textView = binding.textCalendario;
        calendarioViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // Funcionalidad para los botones
        binding.buttonProximas.setOnClickListener(v -> {
            // Acción para "Próximas"
            Toast.makeText(getContext(), "Próximas citas", Toast.LENGTH_SHORT).show();
            // Aquí puedes agregar la lógica para mostrar las próximas citas
        });

        binding.buttonRealizadas.setOnClickListener(v -> {
            // Acción para "Realizadas"
            Toast.makeText(getContext(), "Citas realizadas", Toast.LENGTH_SHORT).show();
            // Aquí puedes agregar la lógica para mostrar las citas realizadas
        });

        binding.buttonCanceladas.setOnClickListener(v -> {
            // Acción para "Canceladas"
            Toast.makeText(getContext(), "Citas canceladas", Toast.LENGTH_SHORT).show();
            // Aquí puedes agregar la lógica para mostrar las citas canceladas
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
