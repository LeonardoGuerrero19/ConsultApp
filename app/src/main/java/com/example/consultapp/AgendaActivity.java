package com.example.consultapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgendaActivity extends AppCompatActivity {

    private TextView nombreServicio;
    private CalendarView calendarView;
    private LinearLayout linearHorarios;
    private Button btnGuardarCita;
    private Button btnRegresar; // New button
    private Button botonSeleccionado = null; // Botón seleccionado actualmente
    FirebaseFirestore mFirestore;
    FirebaseAuth mAuth;

    private String fechaSeleccionada; // Variable global
    private String horarioSeleccionado; // Variable para almacenar el horario seleccionado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Inicializar vistas
        nombreServicio = findViewById(R.id.nombreServicio);
        linearHorarios = findViewById(R.id.linearHorarios); // Inicializar el LinearLayout
        calendarView = findViewById(R.id.calendarView);
        btnGuardarCita = findViewById(R.id.btnGuardarCita);
        btnRegresar = findViewById(R.id.btnRegresar); // Initialize new button

        // Obtener el nombre del servicio desde el Intent
        String servicioSeleccionado = getIntent().getStringExtra("nombreServicio");

        // Mostrar el nombre del servicio
        if (servicioSeleccionado != null) {
            nombreServicio.setText(servicioSeleccionado);
        } else {
            nombreServicio.setText("Servicio no disponible");
        }

        // Configurar el CalendarView
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            fechaSeleccionada = dayOfMonth + "/" + (month + 1) + "/" + year;
            cargarHorariosParaFecha(servicioSeleccionado, fechaSeleccionada);
        });

        // Configurar el botón para guardar la cita
        btnGuardarCita.setOnClickListener(view -> guardarCita(servicioSeleccionado, fechaSeleccionada));

        btnRegresar.setOnClickListener(view -> {
            Intent intent = new Intent(AgendaActivity.this, UserActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void cargarHorariosParaFecha(String nombreServicio, String fecha) {
        mFirestore.collection("horarios_medicos")
                .whereEqualTo("especializacion", nombreServicio)
                .whereEqualTo("fecha", fecha)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> horariosDisponibles = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            List<String> horarios = (List<String>) document.get("horarios");
                            if (horarios != null) {
                                horariosDisponibles.addAll(horarios);
                            }
                        }
                        actualizarBotonesHorarios(horariosDisponibles);
                    } else {
                        Toast.makeText(AgendaActivity.this, "Error al cargar horarios", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void actualizarBotonesHorarios(List<String> horarios) {
        linearHorarios.removeAllViews(); // Limpiar las vistas existentes
        LinearLayout currentRow = null;
        int count = 0;

        for (String horario : horarios) {
            // Crear un nuevo botón
            Button button = new Button(this);
            button.setText(horario);
            button.setBackgroundResource(R.drawable.boton_selector); // Diseño inicial

            // Configurar el tamaño del botón
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, // Ancho dinámico
                    LinearLayout.LayoutParams.WRAP_CONTENT, // Altura automática
                    1f // Peso para distribuir uniformemente
            );
            params.setMargins(20, 20, 20, 20); // Márgenes
            button.setLayoutParams(params);

            // Configurar el evento de clic para el botón
            button.setOnClickListener(v -> {
                // Cambiar diseño del botón seleccionado
                if (botonSeleccionado != null) {
                    botonSeleccionado.setBackgroundResource(R.drawable.boton_no_seleccionado);
                }
                botonSeleccionado = button; // Actualizar el botón seleccionado
                button.setBackgroundResource(R.drawable.boton_seleccionado);
                horarioSeleccionado = horario; // Guardar el horario seleccionado
                Toast.makeText(AgendaActivity.this, "Horario seleccionado: " + horario, Toast.LENGTH_SHORT).show();
            });

            // Crear una nueva fila si es necesario
            if (count % 3 == 0) {
                currentRow = new LinearLayout(this);
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                currentRow.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                linearHorarios.addView(currentRow); // Agregar la nueva fila al contenedor principal
            }

            // Agregar el botón a la fila actual
            if (currentRow != null) {
                currentRow.addView(button);
            }

            count++;
        }

        // Completar la última fila si tiene menos de 3 botones
        if (count % 3 != 0 && currentRow != null) {
            int emptyViews = 3 - (count % 3); // Calcular cuántos espacios faltan
            for (int i = 0; i < emptyViews; i++) {
                View emptyView = new View(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        0, // Ancho dinámico
                        LinearLayout.LayoutParams.WRAP_CONTENT, // Altura automática
                        1f // Peso para distribuir uniformemente
                );
                emptyView.setLayoutParams(params);
                currentRow.addView(emptyView); // Agregar vistas vacías para completar la fila
            }
        }
    }



    private void guardarCita(String nombreServicio, String fecha) {
        if (horarioSeleccionado == null) {
            Toast.makeText(this, "Por favor selecciona un horario", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener el ID del usuario actual
        String userId = mAuth.getCurrentUser().getUid();

        // Crear un mapa para los datos de la cita
        Map<String, Object> citas = new HashMap<>();
        citas.put("usuario_id", userId);
        citas.put("servicio", nombreServicio);
        citas.put("fecha", fecha);
        citas.put("horario", horarioSeleccionado);
        citas.put("estado", "proxima");

        // Guardar la cita en Firestore en la colección 'citas'
        mFirestore.collection("citas")
                .add(citas)
                .addOnSuccessListener(documentReference -> {
                    // Obtener el ID de la cita generada
                    String citaId = documentReference.getId();

                    // Actualizar la cita con el cita_id
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("cita_id", citaId);

                    // Actualizar el documento de la cita con el cita_id
                    documentReference.update(updateData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(AgendaActivity.this, "Cita guardada exitosamente", Toast.LENGTH_SHORT).show();
                                finish(); // Cerrar la actividad actual
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AgendaActivity.this, "Error al guardar el cita_id", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AgendaActivity.this, "Error al guardar la cita", Toast.LENGTH_SHORT).show();
                });
    }
}
