package com.example.c;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.c.databinding.FragmentCalendarioMedicoBinding;

import java.util.ArrayList;
import java.util.List;

public class CalendarioMedicoFragment extends Fragment {

    private CalendarioMedicoViewModel mViewModel;
    private FragmentCalendarioMedicoBinding binding;
    private CitaMedicoAdapter adapter;
    private List<CitaMedico> listaCitas;

    public static CalendarioMedicoFragment newInstance() {
        return new CalendarioMedicoFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCalendarioMedicoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(CalendarioMedicoViewModel.class);

        // Inicializar la lista de citas
        listaCitas = new ArrayList<>();
        cargarCitas(); // Método para cargar las citas (puedes modificarlo según tu lógica)

        // Configurar el RecyclerView
        adapter = new CitaMedicoAdapter(listaCitas);
        binding.rvCitas.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCitas.setAdapter(adapter);
    }

    private void cargarCitas() {
        // Agregar datos de ejemplo (esto debería ser reemplazado por tus datos reales)
        listaCitas.add(new CitaMedico("2889845673", "Paciente: Lorena Villarreal", "10:00 - 10:30 AM"));
        listaCitas.add(new CitaMedico("1234567890", "Paciente: Juan Pérez", "11:00 - 11:30 AM"));
        // Agrega más citas según sea necesario
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Evitar fugas de memoria
    }
}