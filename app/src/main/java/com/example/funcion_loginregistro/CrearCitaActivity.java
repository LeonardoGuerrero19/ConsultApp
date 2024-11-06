package com.example.funcion_loginregistro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrearCitaActivity extends AppCompatActivity {
    TextView nombreServicioTextView;
    CalendarView calendarView;
    Spinner spinnerHorarios;
    Button btnGuardarCita, btn_cerrarS;
    FirebaseFirestore mFirestore;
    FirebaseAuth mAuth;

    private String fechaSeleccionada; // Variable global

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_cita);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Inicializar los views
        nombreServicioTextView = findViewById(R.id.nombreServicio);
        calendarView = findViewById(R.id.calendarView);
        spinnerHorarios = findViewById(R.id.spinnerHorarios); // Inicializar el Spinner
        btnGuardarCita = findViewById(R.id.btnGuardarCita);
        btn_cerrarS = findViewById(R.id.btn_cerrarS);

        // Obtener el nombre del servicio del Intent
        Intent intent = getIntent();
        String nombreServicio = intent.getStringExtra("servicio");
        // Agregar este nombre para usarlo después
        nombreServicioTextView.setText(nombreServicio);

        // Aquí puedes cargar los horarios consultados de la base de datos
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Guardar la fecha seleccionada en la variable global
            fechaSeleccionada = dayOfMonth + "/" + (month + 1) + "/" + year;
            // Cargar horarios para la fecha seleccionada y el servicio específico
            cargarHorariosParaFecha(nombreServicio, fechaSeleccionada);
        });


        // Configurar el botón para cerrar sesión
        btn_cerrarS.setOnClickListener(view -> {
            mAuth.signOut();
            startActivity(new Intent(CrearCitaActivity.this, LoginActivity.class));
            finish();
        });

        // Configurar el botón para guardar la cita
        btnGuardarCita.setOnClickListener(view -> guardarCita(nombreServicio, fechaSeleccionada));
    }

    private void cargarHorariosParaFecha(String nombreServicio, String fecha) {
        mFirestore.collection("horarios_medicos")
                .whereEqualTo("especializacion", nombreServicio) // Filtrar por especialización
                .whereEqualTo("fecha", fecha) // Filtrar por fecha
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
                        // Aquí puedes actualizar el spinner con los horarios disponibles
                        actualizarSpinnerHorarios(horariosDisponibles);
                    } else {
                        Toast.makeText(CrearCitaActivity.this, "Error al cargar horarios", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void actualizarSpinnerHorarios(List<String> horarios) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, horarios);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHorarios.setAdapter(adapter); // Actualizar el spinner con los horarios
    }

    private void guardarCita(String nombreServicio, String fecha) {
        // Obtener el horario seleccionado del spinner
        String horarioSeleccionado = spinnerHorarios.getSelectedItem().toString();

        // Obtener el ID del usuario actual
        String userId = mAuth.getCurrentUser().getUid();

        // Crear un mapa para los datos de la cita
        Map<String, Object> citas = new HashMap<>();
        citas.put("usuario_id", userId);
        citas.put("servicio", nombreServicio);
        citas.put("fecha", fecha);
        citas.put("horario", horarioSeleccionado);

        // Guardar la cita en Firestore en la colección 'citas'
        mFirestore.collection("citas")
                .add(citas)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(CrearCitaActivity.this, "Cita guardada exitosamente", Toast.LENGTH_SHORT).show();
                    // Opcional: Redirigir a otra actividad o limpiar el formulario
                    finish(); // Cerrar la actividad actual
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CrearCitaActivity.this, "Error al guardar la cita", Toast.LENGTH_SHORT).show();
                });
    }
}
