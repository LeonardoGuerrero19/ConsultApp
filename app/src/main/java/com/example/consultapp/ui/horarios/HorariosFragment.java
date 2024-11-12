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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HorariosFragment extends Fragment {

    private FragmentHorariosBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private LinearLayout linearLayoutHorarios;
    private String fechaSeleccionada;
    private List<String> horariosSeleccionados = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HorariosViewModel horariosViewModel =
                new ViewModelProvider(this).get(HorariosViewModel.class);

        binding = FragmentHorariosBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Button btnGuardarHorario = binding.btnGuardarHorario;
        CalendarView calendarView = binding.calendarView;

        // Inicializar Firebase Auth y Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicializar el LinearLayout para los botones
        linearLayoutHorarios = binding.linearLayoutHorarios;

        // Obtener el ID del usuario logueado
        String userId = mAuth.getCurrentUser().getUid();

        // Llamar al método para obtener los datos del usuario logueado
        getUserDataFromDatabase(userId);

        // Listener para el calendario
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Formato de la fecha seleccionada
            fechaSeleccionada = dayOfMonth + "/" + (month + 1) + "/" + year; // Mes es 0-indexed
            Toast.makeText(getContext(), "Fecha seleccionada: " + fechaSeleccionada, Toast.LENGTH_SHORT).show();
        });

        // Listener para el botón de guardar
        btnGuardarHorario.setOnClickListener(v -> {
            if (fechaSeleccionada != null && !horariosSeleccionados.isEmpty()) {
                guardarHorariosEnFirestore();
            } else {
                Toast.makeText(getContext(), "Seleccione una fecha y horarios", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    // Modificar el código para agregar los botones en grupos de tres
    private void getUserDataFromDatabase(String userId) {
        // Obtener la referencia del documento del usuario en Firestore
        DocumentReference userDocRef = db.collection("user").document(userId);

        // Obtener los datos del usuario (nombre, especialización y horarios)
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Obtener el nombre y especialización del usuario
                String nombre = documentSnapshot.getString("nombre");
                String especializacion = documentSnapshot.getString("especializacion");

                // Obtener los horarios desde Firestore
                List<String> horarios = (List<String>) documentSnapshot.get("horario");

                if (horarios != null && !horarios.isEmpty()) {
                    // Limpiar el LinearLayout antes de agregar los nuevos botones
                    linearLayoutHorarios.removeAllViews();

                    // Crear un contenedor horizontal para los botones
                    LinearLayout horizontalLayout = new LinearLayout(getContext());
                    horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
                    horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                    // Variable para contar el número de botones
                    int count = 0;

                    // Crear un botón por cada horario
                    for (String horario : horarios) {
                        Button horarioButton = new Button(getContext());
                        horarioButton.setText(horario);
                        horarioButton.setLayoutParams(new LinearLayout.LayoutParams(
                                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1)); // Cada botón ocupa un 1/3 del espacio
                        horarioButton.setBackgroundColor(getResources().getColor(R.color.aqua));
                        horarioButton.setBackground(getResources().getDrawable(R.drawable.rounded_button));


                        // Acción al hacer clic en el botón
                        horarioButton.setOnClickListener(v -> {
                            if (horariosSeleccionados.contains(horario)) {
                                horariosSeleccionados.remove(horario); // Desmarcar horario
                                horarioButton.setBackgroundResource(R.drawable.boton_no_seleccionado); // Estilo para no seleccionado
                            } else {
                                horariosSeleccionados.add(horario); // Marcar horario
                                horarioButton.setBackgroundResource(R.drawable.boton_seleccionado); // Estilo para seleccionado
                            }
                        });

                        // Agregar el botón al contenedor horizontal
                        horizontalLayout.addView(horarioButton);

                        // Si ya hay tres botones, añadir el contenedor horizontal al LinearLayout principal y crear uno nuevo
                        count++;
                        if (count % 3 == 0) {
                            linearLayoutHorarios.addView(horizontalLayout);
                            horizontalLayout = new LinearLayout(getContext());
                            horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
                            horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        }
                    }

                    // Si el último grupo tiene menos de tres botones, agregar el contenedor restante
                    if (count % 3 != 0) {
                        linearLayoutHorarios.addView(horizontalLayout);
                    }

                } else {
                    Toast.makeText(getContext(), "No se encontraron horarios disponibles", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(e -> {
            // Manejar error
            Toast.makeText(getContext(), "Error al cargar los datos del usuario", Toast.LENGTH_SHORT).show();
        });
    }


    private void guardarHorariosEnFirestore() {
        // Crear un mapa para guardar los datos
        Map<String, Object> data = new HashMap<>();
        data.put("fecha", fechaSeleccionada);
        data.put("horarios", horariosSeleccionados);

        // Agregar nombre y especialización
        String uid = mAuth.getCurrentUser().getUid(); // Obtener UID del médico logueado
        db.collection("user").document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String nombre = document.getString("nombre");
                            String especializacion = document.getString("especializacion");

                            // Agregar nombre y especialización al mapa
                            data.put("nombre", nombre);
                            data.put("especializacion", especializacion);

                            // Guardar en la colección "horarios_medicos" o donde desees
                            db.collection("horarios_medicos").add(data)
                                    .addOnSuccessListener(documentReference -> {
                                        Toast.makeText(getContext(), "Horarios guardados con éxito", Toast.LENGTH_SHORT).show();
                                        // Limpiar selecciones
                                        fechaSeleccionada = null;
                                        horariosSeleccionados.clear();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Error al guardar horarios: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(getContext(), "El documento no existe", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Error al cargar información del médico", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
