package com.example.funcion_loginregistro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class MedicoActivity extends AppCompatActivity {

    private EditText nombreMedico, telefonoMedico;
    private Button btnGuardarMedico, btnCerrarSesion;
    private FirebaseFirestore db;
    private Spinner spinnerEspecializacionMedico, spinnerHorarioMedico;
    private FirebaseAuth mAuth;

    // Lista para guardar los horarios seleccionados
    private List<String> horariosSeleccionados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medico);

        // Inicializar Firebase Firestore y Auth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Inicializar la lista de horarios seleccionados
        horariosSeleccionados = new ArrayList<>();

        // Referencias a los elementos de la interfaz
        nombreMedico = findViewById(R.id.et_nombre_medico);
        telefonoMedico = findViewById(R.id.et_telefono_medico);
        btnGuardarMedico = findViewById(R.id.btn_guardar_medico);
        btnCerrarSesion = findViewById(R.id.btn_cerrarS);
        spinnerEspecializacionMedico = findViewById(R.id.spinner_especializacion_medico);
        spinnerHorarioMedico = findViewById(R.id.spinner_horarios);

        // Cargar especializaciones y horarios desde Firestore
        cargarEspecializacionesDesdeFirestore();
        cargarHorariosDesdeFirestore();

        // Listener para el botón de guardar
        btnGuardarMedico.setOnClickListener(view -> {
            String nombre = nombreMedico.getText().toString().trim();
            String especializacion = spinnerEspecializacionMedico.getSelectedItem().toString();
            String telefono = telefonoMedico.getText().toString().trim();

            // Validar si los campos están vacíos
            if (nombre.isEmpty() || especializacion.isEmpty() || telefono.isEmpty()) {
                Toast.makeText(MedicoActivity.this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            } else if (!validarTelefono(telefono)) {
                Toast.makeText(MedicoActivity.this, "Número de teléfono inválido", Toast.LENGTH_SHORT).show();
            } else {
                verificarTelefonoDuplicado(telefono, nombre, especializacion, horariosSeleccionados);
            }
        });

        // Listener para cerrar sesión
        btnCerrarSesion.setOnClickListener(view -> {
            mAuth.signOut();
            finish();
            startActivity(new Intent(MedicoActivity.this, LoginActivity.class));
        });

        // Listener para guardar automáticamente el horario seleccionado
        // Listener para guardar automáticamente el horario seleccionado
        spinnerHorarioMedico.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String horarioSeleccionado = spinnerHorarioMedico.getSelectedItem().toString();

                // Verifica si el usuario seleccionó un horario válido (excluyendo el placeholder)
                if (!horarioSeleccionado.equals("Seleccione sus horarios")) {
                    // Agregar el horario a la lista de horarios seleccionados
                    if (!horariosSeleccionados.contains(horarioSeleccionado)) {
                        horariosSeleccionados.add(horarioSeleccionado);
                        Toast.makeText(MedicoActivity.this, "Horario agregado: " + horarioSeleccionado, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // No hacer nada
            }
        });

    }
    private boolean validarTelefono(String telefono) {
        return telefono.matches("\\d{10}"); // Ejemplo: Validar que sea un número de 10 dígitos
    }

    // Método para verificar si el teléfono ya está registrado en Firestore
    private void verificarTelefonoDuplicado(String telefono, String nombre, String especializacion, List<String> horarios) {
        db.collection("medicos")
                .whereEqualTo("telefono", telefono)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Comprobar si la lista de documentos está vacía
                        if (task.getResult().isEmpty()) {
                            // Si no hay duplicados, guardar el médico en Firestore
                            guardarMedicoEnFirestore(nombre, especializacion, telefono, horarios);
                        } else {
                            // Si hay duplicados, mostrar mensaje
                            Toast.makeText(MedicoActivity.this, "El teléfono ya está registrado", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Manejar error de consulta
                        Toast.makeText(MedicoActivity.this, "Error al verificar el teléfono", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void cargarEspecializacionesDesdeFirestore() {
        db.collection("servicios").document("rySIMH9TTamnp7dxQuDj")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            List<String> especializaciones = (List<String>) document.get("valor");
                            if (especializaciones != null) {
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, especializaciones);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinnerEspecializacionMedico.setAdapter(adapter);
                            } else {
                                Toast.makeText(MedicoActivity.this, "No se encontraron especializaciones", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MedicoActivity.this, "El documento no existe", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MedicoActivity.this, "Error al cargar especializaciones", Toast.LENGTH_SHORT).show();
                    }
                });
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
                                spinnerHorarioMedico.setAdapter(adapter);
                            } else {
                                Toast.makeText(MedicoActivity.this, "No se encontraron horarios", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MedicoActivity.this, "El documento no existe", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MedicoActivity.this, "Error al cargar horarios", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void guardarMedicoEnFirestore(String nombre, String especializacion, String telefono, List<String> horarios) {
        Map<String, Object> medico = new HashMap<>();
        medico.put("nombre", nombre);
        medico.put("especializacion", especializacion);
        medico.put("telefono", telefono);
        medico.put("horarios", horarios);  // Guardar la lista de horarios seleccionados

        db.collection("medicos").add(medico)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(MedicoActivity.this, "Datos guardados exitosamente", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MedicoActivity.this, "Error al guardar los datos", Toast.LENGTH_SHORT).show();
                });
    }
}
