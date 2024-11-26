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
    private LinearLayout linearHorarios, linearMedicos;
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
        linearMedicos = findViewById(R.id.linearMedicos);
        calendarView = findViewById(R.id.calendarView);
        btnGuardarCita = findViewById(R.id.btnGuardarCita);

        // Obtener el nombre del servicio desde el Intent
        String servicioSeleccionado = getIntent().getStringExtra("nombre_servicio");

        // Mostrar el nombre del servicio
        if (servicioSeleccionado != null) {
            nombreServicio.setText(servicioSeleccionado);
            cargarMedicosPorEspecializacion(servicioSeleccionado);
        } else {
            nombreServicio.setText("Servicio no disponible");
        }

        // Configurar el CalendarView para capturar la fecha seleccionada
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            fechaSeleccionada = dayOfMonth + "/" + (month + 1) + "/" + year;
            Toast.makeText(this, "Fecha seleccionada: " + fechaSeleccionada, Toast.LENGTH_SHORT).show();
        });

        // Configurar el botón para guardar la cita
        btnGuardarCita.setOnClickListener(view -> guardarCita(servicioSeleccionado, fechaSeleccionada));
    }

    private void cargarMedicosPorEspecializacion(String especializacion) {
        dbRef.child("Medicos")
                .orderByChild("especializacion")
                .equalTo(especializacion)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Map<String, String>> medicos = new ArrayList<>();
                        for (DataSnapshot medicoSnapshot : snapshot.getChildren()) {
                            String nombre = medicoSnapshot.child("nombre").getValue(String.class);
                            String id = medicoSnapshot.getKey();
                            if (nombre != null && id != null) {
                                Map<String, String> medico = new HashMap<>();
                                medico.put("nombre", nombre);
                                medico.put("id", id);
                                medicos.add(medico);
                            }
                        }
                        actualizarListaMedicos(medicos);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AgendaActivity.this, "Error al cargar médicos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void actualizarListaMedicos(List<Map<String, String>> medicos) {
        linearMedicos.removeAllViews(); // Limpiar las vistas existentes

        for (Map<String, String> medico : medicos) {
            String nombreMedico = medico.get("nombre");
            String idMedico = medico.get("id");

            // Inflar el diseño del botón desde XML
            View botonView = getLayoutInflater().inflate(R.layout.boton_medico, linearMedicos, false);
            Button botonMedico = botonView.findViewById(R.id.botonMedico);

            // Configurar el texto y el clic
            botonMedico.setText(nombreMedico);
            botonMedico.setOnClickListener(v -> mostrarHorariosPorFechaYMedico(idMedico, nombreMedico));

            // Agregar el botón al LinearLayout
            linearMedicos.addView(botonMedico);
        }
    }

    private void mostrarHorariosPorFechaYMedico(String idMedico, String nombreMedico) {
        linearHorarios.removeAllViews(); // Limpiar vistas anteriores

        if (fechaSeleccionada == null) {
            Toast.makeText(this, "Selecciona una fecha primero", Toast.LENGTH_SHORT).show();
            return;
        }

        dbRef.child("Citas")  // Consultamos las citas existentes
                .orderByChild("fecha")
                .equalTo(fechaSeleccionada)  // Filtrar por la fecha seleccionada
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Lista para almacenar los horarios ocupados
                        List<String> horariosOcupados = new ArrayList<>();

                        // Recorrer las citas existentes para obtener los horarios ocupados
                        for (DataSnapshot citaSnapshot : snapshot.getChildren()) {
                            String horarioExistente = citaSnapshot.child("horario").getValue(String.class);
                            String doctorExistente = citaSnapshot.child("doctor").getValue(String.class);

                            if (doctorExistente != null && doctorExistente.equals(nombreMedico) && horarioExistente != null) {
                                horariosOcupados.add(horarioExistente);
                            }
                        }

                        // Ahora cargamos los horarios disponibles
                        dbRef.child("horarios_medicos")
                                .orderByChild("fecha")
                                .equalTo(fechaSeleccionada) // Filtrar por la fecha seleccionada
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        boolean horarioEncontrado = false;
                                        LinearLayout horariosLayout = null; // Inicializar el LinearLayout para los botones de horarios
                                        int contadorBotones = 0; // Contador de botones agregados

                                        for (DataSnapshot medicoSnapshot : snapshot.getChildren()) {
                                            String nombreMedicoBD = medicoSnapshot.child("nombre").getValue(String.class);
                                            if (nombreMedicoBD != null && nombreMedicoBD.equals(nombreMedico)) {
                                                horarioEncontrado = true;

                                                // Recorrer los horarios del médico
                                                for (DataSnapshot horarioSnapshot : medicoSnapshot.child("horarios").getChildren()) {
                                                    String horario = horarioSnapshot.getValue(String.class);
                                                    if (horario != null && !horariosOcupados.contains(horario)) {  // Verificar si el horario está ocupado
                                                        // Si ya hemos agregado 3 botones, crear un nuevo LinearLayout
                                                        if (contadorBotones % 3 == 0) {
                                                            if (horariosLayout != null) {
                                                                linearHorarios.addView(horariosLayout); // Agregar la fila anterior de botones
                                                            }
                                                            horariosLayout = new LinearLayout(AgendaActivity.this);
                                                            horariosLayout.setOrientation(LinearLayout.HORIZONTAL); // Alinear horizontalmente
                                                            horariosLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                                            ));
                                                        }

                                                        // Crear un Button para cada horario
                                                        Button botonHorario = new Button(AgendaActivity.this);
                                                        botonHorario.setText(horario);
                                                        botonHorario.setBackgroundResource(R.drawable.boton_selector);

                                                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                                0, // Ancho dinámico
                                                                LinearLayout.LayoutParams.WRAP_CONTENT, // Altura automática
                                                                1f // Peso para distribuir uniformemente
                                                        );
                                                        params.setMargins(20, 50, 20, 20); // Márgenes
                                                        botonHorario.setLayoutParams(params);

                                                        botonHorario.setOnClickListener(v -> {
                                                            horarioSeleccionado = horario;

                                                            // Cambiar el estado de los botones
                                                            if (botonSeleccionado != null) {
                                                                botonSeleccionado.setSelected(false); // Deseleccionar el botón anterior
                                                            }
                                                            botonHorario.setSelected(true); // Seleccionar el botón actual
                                                            botonSeleccionado = botonHorario;

                                                            Toast.makeText(AgendaActivity.this, "Horario seleccionado: " + horarioSeleccionado, Toast.LENGTH_SHORT).show();
                                                        });

                                                        // Agregar el botón al LinearLayout de horarios
                                                        horariosLayout.addView(botonHorario);
                                                        contadorBotones++; // Incrementar contador
                                                    }
                                                }
                                            }
                                        }

                                        // Asegurarse de agregar el último LinearLayout de horarios
                                        if (horariosLayout != null) {
                                            linearHorarios.addView(horariosLayout);
                                        }

                                        if (!horarioEncontrado) {
                                            mostrarMensajeNoHayHorarios();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(AgendaActivity.this, "Error al cargar horarios", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AgendaActivity.this, "Error al verificar citas existentes", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void guardarCita(String nombreServicio, String fecha) {
        if (horarioSeleccionado == null) {
            Toast.makeText(this, "Por favor selecciona un horario", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        // Verificar si ya existe una cita para este usuario en la misma fecha y horario
        dbRef.child("citas")
                .orderByChild("usuario_id")
                .equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean citaDuplicada = false;

                        for (DataSnapshot citaSnapshot : snapshot.getChildren()) {
                            String fechaExistente = citaSnapshot.child("fecha").getValue(String.class);
                            String horarioExistente = citaSnapshot.child("horario").getValue(String.class);

                            if (fecha.equals(fechaExistente) && horarioSeleccionado.equals(horarioExistente)) {
                                citaDuplicada = true;
                                break;
                            }
                        }

                        if (citaDuplicada) {
                            Toast.makeText(AgendaActivity.this, "Ya tienes una cita en este horario", Toast.LENGTH_SHORT).show();
                        } else {
                            crearCita(nombreServicio, fecha, userId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AgendaActivity.this, "Error al verificar citas existentes", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void crearCita(String nombreServicio, String fecha, String userId) {
        dbRef.child("horarios_medicos")
                .orderByChild("especializacion")
                .equalTo(nombreServicio)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String nombreMedico = null;
                        String idMedico = null;

                        // Realizar asignación fuera del contexto lambda
                        for (DataSnapshot horarioSnapshot : snapshot.getChildren()) {
                            String fechaHorario = horarioSnapshot.child("fecha").getValue(String.class);
                            List<String> horarios = (List<String>) horarioSnapshot.child("horarios").getValue();

                            if (fechaHorario != null && fechaHorario.equals(fecha) && horarios != null && horarios.contains(horarioSeleccionado)) {
                                nombreMedico = horarioSnapshot.child("nombre").getValue(String.class);
                                idMedico = horarioSnapshot.getKey(); // Obtener el ID del médico
                                break;
                            }
                        }

                        // Asegurarse de que las variables estén inicializadas antes de la siguiente lambda
                        if (nombreMedico == null || idMedico == null) {
                            Toast.makeText(AgendaActivity.this, "No se encontró un médico para esta cita", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Guardar la cita
                        guardarCitaEnBaseDeDatos(nombreServicio, fecha, userId, idMedico, nombreMedico);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AgendaActivity.this, "Error al buscar el médico", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void guardarCitaEnBaseDeDatos(String nombreServicio, String fecha, String userId, String idMedico, String nombreMedico) {
        String citaId = dbRef.child("citas").push().getKey();
        if (citaId == null) {
            Toast.makeText(AgendaActivity.this, "Error al generar cita", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> citaData = new HashMap<>();
        citaData.put("cita_id", citaId);
        citaData.put("usuario_id", userId);
        citaData.put("servicio", nombreServicio);
        citaData.put("fecha", fecha);
        citaData.put("horario", horarioSeleccionado);
        citaData.put("estado", "proxima");
        citaData.put("doctor", nombreMedico);

        dbRef.child("citas").child(citaId).setValue(citaData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AgendaActivity.this, "Cita guardada exitosamente", Toast.LENGTH_SHORT).show();

                    // Crear notificación para el médico
                    String mensaje = "Tienes una nueva cita el " + fecha + " a las " + horarioSeleccionado;
                    crearNotificacion(idMedico, mensaje, fecha, horarioSeleccionado, nombreMedico);

                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AgendaActivity.this, "Error al guardar la cita", Toast.LENGTH_SHORT).show();
                });
    }

    private void crearNotificacion(String doctorId, String mensaje, String fecha, String hora, String nombreMedico) {
        String notificacionId = dbRef.child("notificaciones").push().getKey(); // Generar ID único para la notificación

        if (notificacionId == null) {
            Toast.makeText(this, "Error al generar notificación", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> notificacionData = new HashMap<>();
        notificacionData.put("mensaje", mensaje);
        notificacionData.put("fecha", fecha);
        notificacionData.put("hora", hora);
        notificacionData.put("estado", "no_leido");
        notificacionData.put("nombre_medico", nombreMedico);
        notificacionData.put("doctor_id", doctorId);

        dbRef.child("notificaciones").child(notificacionId).setValue(notificacionData)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Notificación creada", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error al crear notificación", Toast.LENGTH_SHORT).show());
    }

    private void mostrarMensajeNoHayHorarios() {
        TextView noHorarios = new TextView(AgendaActivity.this);
        noHorarios.setText("No hay horarios disponibles para esta fecha y médico.");
        noHorarios.setTextSize(16);
        noHorarios.setPadding(10, 5, 10, 5);
        linearHorarios.addView(noHorarios);
    }

}