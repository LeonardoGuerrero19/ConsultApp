package com.example.funcion_loginregistro;

import android.os.Bundle;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistrarMedicoActivity extends AppCompatActivity {
    private EditText etNombreMedico, etCorreoMedico, etContrasenaMedico, etTelefonoMedico;
    private Spinner spinnerEspecializacion;
    private Button btnRegistrarMedico, btnCerrarS;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_medico);

        // Inicializar FirebaseAuth y Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Referencias a los elementos en el layout
        etNombreMedico = findViewById(R.id.nombreMedico);
        etCorreoMedico = findViewById(R.id.correoMedico);
        etContrasenaMedico = findViewById(R.id.contrasenaMedico);
        etTelefonoMedico = findViewById(R.id.telefonoMedico);  // Campo de teléfono
        spinnerEspecializacion = findViewById(R.id.spinner_especializacion);
        btnRegistrarMedico = findViewById(R.id.btn_registrarMedico);
        btnCerrarS = findViewById(R.id.btn_cerrarS);

        // Cargar especializaciones desde Firestore
        cargarEspecializacionesDesdeFirestore();

        // Cerrar sesión
        btnCerrarS.setOnClickListener(view -> {
            mAuth.signOut();
            Intent intent = new Intent(RegistrarMedicoActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Cierra la actividad actual
        });

        // Registrar médico
        btnRegistrarMedico.setOnClickListener(v -> {
            String nombre = etNombreMedico.getText().toString();
            String correo = etCorreoMedico.getText().toString();
            String contrasena = etContrasenaMedico.getText().toString();
            String telefono = etTelefonoMedico.getText().toString();
            String especializacion = spinnerEspecializacion.getSelectedItem().toString();

            if (!nombre.isEmpty() && !correo.isEmpty() && !contrasena.isEmpty() && !telefono.isEmpty() && !especializacion.isEmpty()) {
                registrarMedico(nombre, correo, contrasena, telefono, especializacion);
            } else {
                Toast.makeText(RegistrarMedicoActivity.this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarEspecializacionesDesdeFirestore() {
        db.collection("Servicios").document("Todos los servicios")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            List<String> especializaciones = (List<String>) document.get("Servicios");
                            if (especializaciones != null) {
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, especializaciones);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinnerEspecializacion.setAdapter(adapter);
                            } else {
                                Toast.makeText(RegistrarMedicoActivity.this, "No se encontraron especializaciones", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(RegistrarMedicoActivity.this, "El documento no existe", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(RegistrarMedicoActivity.this, "Error al cargar especializaciones", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registrarMedico(String nombre, String correo, String contrasena, String telefono, String especializacion) {
        // Crear usuario en Firebase Authentication
        mAuth.createUserWithEmailAndPassword(correo, contrasena).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Obtener UID del usuario registrado
                String uid = mAuth.getCurrentUser().getUid();

                // Crear mapa de datos para Firestore
                Map<String, Object> medico = new HashMap<>();
                medico.put("nombre", nombre);
                medico.put("correo", correo);
                medico.put("telefono", telefono);
                medico.put("especializacion", especializacion);
                medico.put("rol", "medico");

                // Guardar datos en Firestore en la colección "user"
                db.collection("user").document(uid).set(medico).addOnSuccessListener(aVoid -> {
                    // Enviar correo de verificación
                    enviarCorreoVerificacion();
                    Toast.makeText(RegistrarMedicoActivity.this, "Médico registrado con éxito. Se ha enviado un correo de verificación.", Toast.LENGTH_SHORT).show();
                    // Limpiar campos
                    limpiarCampos();
                }).addOnFailureListener(e -> {
                    Toast.makeText(RegistrarMedicoActivity.this, "Error al registrar médico", Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(RegistrarMedicoActivity.this, "Error en el registro", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enviarCorreoVerificacion() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(RegistrarMedicoActivity.this, "Correo de verificación enviado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegistrarMedicoActivity.this, "Error al enviar el correo de verificación", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void limpiarCampos() {
        etNombreMedico.setText("");
        etCorreoMedico.setText("");
        etContrasenaMedico.setText("");
        etTelefonoMedico.setText("");
        spinnerEspecializacion.setSelection(0); // Restablecer el spinner a la primera opción
    }
}
