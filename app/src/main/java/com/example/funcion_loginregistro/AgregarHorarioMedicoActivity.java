package com.example.funcion_loginregistro;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgregarHorarioMedicoActivity extends AppCompatActivity {
    private TextView nombreMedico, especializacionMedico;
    private CalendarView calendarView;
    private Spinner spinnerHorarios;
    private Button btnGuardarHorario, btn_cerrarS;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String fechaSeleccionada;
    private List<String> horariosSeleccionados = new ArrayList<>();
    private String nombreMedicoLogueado; // Variable para almacenar el nombre del médico

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medico);

        // Iniciar servicios Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Referencias ids desde layout
        nombreMedico = findViewById(R.id.tvNombreMedico);
        especializacionMedico = findViewById(R.id.nombreServicio);
        calendarView = findViewById(R.id.calendarView);
        spinnerHorarios = findViewById(R.id.spinnerHorarios);
        btnGuardarHorario = findViewById(R.id.btnGuardarHorario);
        btn_cerrarS = findViewById(R.id.btn_cerrarS);

        // Cargar información del médico
        cargarInformacionMedico();

        // Cargar horarios desde Firestore
        cargarHorariosDesdeFirestore();

        // Cargar citas del médico
        cargarCitasDelMedico();

        // Listener para el calendario
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Formato de la fecha seleccionada
            fechaSeleccionada = dayOfMonth + "/" + (month + 1) + "/" + year; // Mes es 0-indexed
            Toast.makeText(AgregarHorarioMedicoActivity.this, "Fecha seleccionada: " + fechaSeleccionada, Toast.LENGTH_SHORT).show();
        });

        // Listener para el botón de guardar
        btnGuardarHorario.setOnClickListener(v -> {
            if (fechaSeleccionada != null && !horariosSeleccionados.isEmpty()) {
                guardarHorariosEnFirestore();
            } else {
                Toast.makeText(AgregarHorarioMedicoActivity.this, "Seleccione una fecha y horarios", Toast.LENGTH_SHORT).show();
            }
        });

        // Cerrar sesión
        btn_cerrarS.setOnClickListener(view -> {
            mAuth.signOut();
            Intent intent = new Intent(AgregarHorarioMedicoActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Cierra la actividad actual
        });
    }

    private void cargarInformacionMedico() {
        String uid = mAuth.getCurrentUser().getUid(); // Obtener UID del médico logueado
        db.collection("user").document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String nombre = document.getString("nombre");
                            String especializacion = document.getString("especializacion");

                            // Actualizar el TextView con el nombre y la especialización
                            String titulo = nombre + " - " + especializacion;
                            nombreMedico.setText(titulo); // Actualiza el TextView
                            nombreMedicoLogueado = nombre; // Almacenar nombre del médico logueado
                        } else {
                            Toast.makeText(AgregarHorarioMedicoActivity.this, "El documento no existe", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(AgregarHorarioMedicoActivity.this, "Error al cargar información del médico", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cargarCitasDelMedico() {
        String uid = mAuth.getCurrentUser().getUid(); // Obtener UID del médico logueado
        db.collection("citas")
                .whereEqualTo("medico", nombreMedicoLogueado) // Filtrar citas por el nombre del médico
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> citas = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            String servicio = document.getString("servicio");
                            String fecha = document.getString("fecha");
                            String horario = document.getString("horario");
                            citas.add("Servicio: " + servicio + ", Fecha: " + fecha + ", Horario: " + horario);
                        }
                        mostrarCitas(citas);
                    } else {
                        Toast.makeText(AgregarHorarioMedicoActivity.this, "Error al cargar citas", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void mostrarCitas(List<String> citas) {
        // Aquí puedes mostrar las citas en la UI, por ejemplo, en un RecyclerView o ListView
        // Para este ejemplo, solo mostraré un Toast
        for (String cita : citas) {
            Toast.makeText(this, cita, Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarHorariosDesdeFirestore() {
        db.collection("horarios").document("LEZc9oqz7gqYLAcp7XlC")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            List<String> horarios = (List<String>) document.get("valor");
                            if (horarios != null) {
                                // Crear un nuevo ArrayList y agregar el placeholder
                                List<String> horariosConPlaceholder = new ArrayList<>();
                                horariosConPlaceholder.add("Seleccione sus horarios"); // Placeholder
                                horariosConPlaceholder.addAll(horarios); // Agregar horarios reales

                                // Configurar el ArrayAdapter con los horarios
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, horariosConPlaceholder);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinnerHorarios.setAdapter(adapter);
                            } else {
                                Toast.makeText(AgregarHorarioMedicoActivity.this, "No se encontraron horarios", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(AgregarHorarioMedicoActivity.this, "El documento no existe", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(AgregarHorarioMedicoActivity.this, "Error al cargar horarios", Toast.LENGTH_SHORT).show();
                    }
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
                                        Toast.makeText(AgregarHorarioMedicoActivity.this, "Horarios guardados con éxito", Toast.LENGTH_SHORT).show();
                                        // Limpiar selecciones
                                        fechaSeleccionada = null;
                                        horariosSeleccionados.clear();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(AgregarHorarioMedicoActivity.this, "Error al guardar horarios: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(AgregarHorarioMedicoActivity.this, "El documento no existe", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(AgregarHorarioMedicoActivity.this, "Error al cargar información del médico", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
