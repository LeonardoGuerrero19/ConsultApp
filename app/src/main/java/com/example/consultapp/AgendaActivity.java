package com.example.consultapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
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
        linearHorarios.removeAllViews(); // Limpiar los botones existentes

        for (String horario : horarios) {
            // Crear un nuevo botón
            Button button = new Button(this);
            button.setText(horario);
            button.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            // Configurar el evento de clic para el botón
            button.setOnClickListener(v -> {
                horarioSeleccionado = horario; // Guardar el horario seleccionado
                Toast.makeText(AgendaActivity.this, "Horario seleccionado: " + horario, Toast.LENGTH_SHORT).show();
            });

            // Agregar el botón al LinearLayout
            linearHorarios.addView(button);
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
                    Toast.makeText(AgendaActivity.this, "Cita guardada exitosamente", Toast.LENGTH_SHORT).show();
                    finish(); // Cerrar la actividad actual
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AgendaActivity.this, "Error al guardar la cita", Toast.LENGTH_SHORT).show();
                });
    }

}
