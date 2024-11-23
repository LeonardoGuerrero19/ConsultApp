package com.example.consultapp.ui.horarios;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.consultapp.R;
import com.example.consultapp.databinding.FragmentHorariosBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HorariosFragment extends Fragment {

    private FragmentHorariosBinding binding;
    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;
    private LinearLayout linearLayoutHorarios;
    private String fechaSeleccionada;
    private List<String> horariosSeleccionados = new ArrayList<>();
    private List<Button> botonesHorarios = new ArrayList<>(); // Lista para guardar referencias a los botones

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HorariosViewModel horariosViewModel =
                new ViewModelProvider(this).get(HorariosViewModel.class);

        binding = FragmentHorariosBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Button btnGuardarHorario = binding.btnGuardarHorario;
        CalendarView calendarView = binding.calendarView;

        // Inicializar Firebase Auth y Database
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        // Inicializar el LinearLayout para los botones
        linearLayoutHorarios = binding.linearLayoutHorarios;

        // Obtener el ID del médico logueado
        String medicoId = mAuth.getCurrentUser().getUid();

        // Llamar al método para obtener los datos del médico logueado
        getMedicoDataFromDatabase(medicoId);

        // Listener para el calendario
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Formato de la fecha seleccionada
            fechaSeleccionada = dayOfMonth + "/" + (month + 1) + "/" + year; // Mes es 0-indexed
            Toast.makeText(getContext(), "Fecha seleccionada: " + fechaSeleccionada, Toast.LENGTH_SHORT).show();
        });

        // Listener para el botón de guardar
        btnGuardarHorario.setOnClickListener(v -> {
            if (fechaSeleccionada != null && !horariosSeleccionados.isEmpty()) {
                guardarHorariosEnRealtime(medicoId);
            } else {
                Toast.makeText(getContext(), "Seleccione una fecha y horarios", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    private void getMedicoDataFromDatabase(String medicoId) {
        dbRef.child("Medicos").child(medicoId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<String> horarios = (List<String>) snapshot.child("horarios").getValue();

                    if (horarios != null && !horarios.isEmpty()) {
                        linearLayoutHorarios.removeAllViews();
                        botonesHorarios.clear(); // Limpiar lista de botones

                        LinearLayout horizontalLayout = new LinearLayout(getContext());
                        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
                        horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                        int count = 0;

                        for (String horario : horarios) {
                            Button horarioButton = new Button(getContext());
                            horarioButton.setText(horario);

                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                            params.setMargins(20, 20, 20, 20);
                            horarioButton.setLayoutParams(params);

                            horarioButton.setBackgroundResource(R.drawable.boton_selector);

                            horarioButton.setOnClickListener(v -> {
                                if (horarioButton.isSelected()) {
                                    horarioButton.setSelected(false);
                                    horariosSeleccionados.remove(horario);
                                } else {
                                    horarioButton.setSelected(true);
                                    horariosSeleccionados.add(horario);
                                }
                            });

                            // Guardar referencia del botón en la lista
                            botonesHorarios.add(horarioButton);

                            horizontalLayout.addView(horarioButton);
                            count++;

                            if (count % 3 == 0) {
                                linearLayoutHorarios.addView(horizontalLayout);
                                horizontalLayout = new LinearLayout(getContext());
                                horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
                                horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            }
                        }

                        if (count % 3 != 0) {
                            int remainingButtons = 3 - (count % 3);

                            for (int i = 0; i < remainingButtons; i++) {
                                View emptyView = new View(getContext());
                                LinearLayout.LayoutParams emptyParams = new LinearLayout.LayoutParams(
                                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                                emptyView.setLayoutParams(emptyParams);
                                horizontalLayout.addView(emptyView);
                            }

                            linearLayoutHorarios.addView(horizontalLayout);
                        }
                    } else {
                        Toast.makeText(getContext(), "No se encontraron horarios disponibles", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error al cargar los datos del médico", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarHorariosEnRealtime(String medicoId) {
        Map<String, Object> data = new HashMap<>();
        data.put("fecha", fechaSeleccionada);
        data.put("horarios", horariosSeleccionados);

        dbRef.child("Medicos").child(medicoId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String nombre = snapshot.child("nombre").getValue(String.class);
                    String especializacion = snapshot.child("especializacion").getValue(String.class);

                    data.put("nombre", nombre);
                    data.put("especializacion", especializacion);

                    dbRef.child("horarios_medicos").push().setValue(data)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Horarios guardados con éxito", Toast.LENGTH_SHORT).show();
                                limpiarSelecciones(); // Llamar a método para deseleccionar botones
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error al guardar horarios: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error al cargar datos del médico", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void limpiarSelecciones() {
        fechaSeleccionada = null;
        horariosSeleccionados.clear();

        // Deseleccionar todos los botones
        for (Button button : botonesHorarios) {
            button.setSelected(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
