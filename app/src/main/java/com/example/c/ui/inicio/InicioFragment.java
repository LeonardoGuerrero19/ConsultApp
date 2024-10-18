package com.example.c.ui.inicio;

import android.content.Intent; // Importa Intent
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // Importa Button
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

// Asegúrate de importar tu archivo R
import com.example.c.PerfilMedicoActivity;
import com.example.c.PersonalMedicoActivity;
import com.example.c.databinding.FragmentInicioBinding;
// Asegúrate de importar tu actividad


public class InicioFragment extends Fragment {

    private FragmentInicioBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        InicioViewModel inicioViewModel =
                new ViewModelProvider(this).get(InicioViewModel.class);

        binding = FragmentInicioBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textInicio;
        inicioViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // Encuentra el botón en el layout
        Button btnVerPerfil = binding.btnVerPerfil1; // Cambia esto si usas el binding

        // Configura el OnClickListener
        btnVerPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PerfilMedicoActivity.class);

                // Si necesitas pasar datos, puedes hacerlo aquí
                intent.putExtra("nombreMedico", "Dra. Fátima");
                intent.putExtra("especialidadMedico", "Patología");
                intent.putExtra("descripcionMedico", "Descripción detallada aquí.");
                intent.putExtra("horarioMedico", "10:00 am - 14:00 pm");
                intent.putExtra("telefonoMedico", "314-103-5-610");

                // Inicia la actividad
                startActivity(intent);
            }
        });

        Button btnVerTodo = binding.txtMore3; // Asegúrate de que este ID coincida con el de tu layout

        btnVerTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PersonalMedicoActivity.class);
                startActivity(intent);
            }
        });


        return root;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
