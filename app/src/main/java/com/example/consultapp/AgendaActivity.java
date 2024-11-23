package com.example.consultapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

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

public class AgendaActivity extends AppCompatActivity {

    private TextView nombreServicio;
    private CalendarView calendarView;
    private LinearLayout linearHorarios;
    private Button btnGuardarCita;
    private Button btnRegresar;
    private Button botonSeleccionado = null; // Botón seleccionado actualmente

    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;

    private String fechaSeleccionada; // Variable global
    private String horarioSeleccionado; // Variable para almacenar el horario seleccionado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        // Inicializar vistas
        nombreServicio = findViewById(R.id.nombreServicio);
        linearHorarios = findViewById(R.id.linearHorarios);
        calendarView = findViewById(R.id.calendarView);
        btnGuardarCita = findViewById(R.id.btnGuardarCita);
        btnRegresar = findViewById(R.id.btnRegresar);

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
        dbRef.child("horarios_medicos")
                .orderByChild("especializacion")
                .equalTo(nombreServicio)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> horariosDisponibles = new ArrayList<>();
                        for (DataSnapshot horarioSnapshot : snapshot.getChildren()) {
                            String fechaHorario = horarioSnapshot.child("fecha").getValue(String.class);
                            if (fechaHorario != null && fechaHorario.equals(fecha)) {
                                List<String> horarios = (List<String>) horarioSnapshot.child("horarios").getValue();
                                if (horarios != null) {
                                    horariosDisponibles.addAll(horarios);
                                }
                            }
                        }
                        actualizarBotonesHorarios(horariosDisponibles);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AgendaActivity.this, "Error al cargar horarios", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void actualizarBotonesHorarios(List<String> horarios) {
        linearHorarios.removeAllViews(); // Limpiar las vistas existentes
        LinearLayout currentRow = null;
        int count = 0;

        for (String horario : horarios) {
            Button button = new Button(this);
            button.setText(horario);
            button.setBackgroundResource(R.drawable.boton_selector); // Diseño inicial

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, // Ancho dinámico
                    LinearLayout.LayoutParams.WRAP_CONTENT, // Altura automática
                    1f // Peso para distribuir uniformemente
            );
            params.setMargins(20, 20, 20, 20); // Márgenes
            button.setLayoutParams(params);

            button.setOnClickListener(v -> {
                if (botonSeleccionado != null) {
                    botonSeleccionado.setBackgroundResource(R.drawable.boton_no_seleccionado);
                }
                botonSeleccionado = button;
                button.setBackgroundResource(R.drawable.boton_seleccionado);
                horarioSeleccionado = horario;
                Toast.makeText(AgendaActivity.this, "Horario seleccionado: " + horario, Toast.LENGTH_SHORT).show();
            });

            if (count % 3 == 0) {
                currentRow = new LinearLayout(this);
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                currentRow.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                linearHorarios.addView(currentRow);
            }

            if (currentRow != null) {
                currentRow.addView(button);
            }

            count++;
        }

        if (count % 3 != 0 && currentRow != null) {
            int emptyViews = 3 - (count % 3);
            for (int i = 0; i < emptyViews; i++) {
                View emptyView = new View(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                );
                emptyView.setLayoutParams(params);
                currentRow.addView(emptyView);
            }
        }
    }

    private void guardarCita(String nombreServicio, String fecha) {
        if (horarioSeleccionado == null) {
            Toast.makeText(this, "Por favor selecciona un horario", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        // Buscar el médico correspondiente a la especialización y horario
        dbRef.child("horarios_medicos")
                .orderByChild("especializacion")
                .equalTo(nombreServicio)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String nombreMedico = null;

                        for (DataSnapshot horarioSnapshot : snapshot.getChildren()) {
                            String fechaHorario = horarioSnapshot.child("fecha").getValue(String.class);
                            List<String> horarios = (List<String>) horarioSnapshot.child("horarios").getValue();

                            if (fechaHorario != null && fechaHorario.equals(fecha) && horarios != null && horarios.contains(horarioSeleccionado)) {
                                nombreMedico = horarioSnapshot.child("nombre").getValue(String.class); // Suponiendo que "nombre" es el campo con el nombre del médico
                                break;
                            }
                        }

                        if (nombreMedico == null) {
                            Toast.makeText(AgendaActivity.this, "No se encontró un médico para esta cita", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Generar un nuevo ID único para la cita
                        String citaId = dbRef.child("citas").push().getKey();
                        if (citaId == null) {
                            Toast.makeText(AgendaActivity.this, "Error al generar cita", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Crear datos de la cita
                        Map<String, Object> citaData = new HashMap<>();
                        citaData.put("cita_id", citaId);
                        citaData.put("usuario_id", userId);
                        citaData.put("servicio", nombreServicio);
                        citaData.put("fecha", fecha);
                        citaData.put("horario", horarioSeleccionado);
                        citaData.put("estado", "proxima");
                        citaData.put("doctor", nombreMedico); // Agregar el nombre del médico

                        // Guardar en la base de datos
                        dbRef.child("citas").child(citaId).setValue(citaData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(AgendaActivity.this, "Cita guardada exitosamente", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(AgendaActivity.this, "Error al guardar la cita", Toast.LENGTH_SHORT).show();
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AgendaActivity.this, "Error al buscar el médico", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
