package com.example.consultapp;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistroDoctor extends AppCompatActivity {

    private EditText etNombreMedico, etCorreoMedico, etContrasenaMedico, etTelefonoMedico;
    private Spinner spinnerEspecializacion;
    private Button horarioButton, btnRegistrarMedico;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String horarioSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_doctor);

        // Inicializar FirebaseAuth y Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Referencias a los elementos en el layout
        etNombreMedico = findViewById(R.id.nombreMedico);
        spinnerEspecializacion = findViewById(R.id.spinner_especializacion);
        etCorreoMedico = findViewById(R.id.correoMedico);
        etContrasenaMedico = findViewById(R.id.contrasenaMedico);
        etTelefonoMedico = findViewById(R.id.telefonoMedico);
        horarioButton = findViewById(R.id.horarioButton);
        btnRegistrarMedico = findViewById(R.id.btn_registrarMedico);

        // Listener para mostrar el diálogo para seleccionar el horario
        horarioButton.setOnClickListener(v -> showBottomDialog());

        // Cargar especializaciones desde Firestore
        cargarEspecializacionesDesdeFirestore();

        // Registrar médico
        btnRegistrarMedico.setOnClickListener(v -> {
            String nombre = etNombreMedico.getText().toString();
            String especializacion = spinnerEspecializacion.getSelectedItem().toString();
            String correo = etCorreoMedico.getText().toString();
            String telefono = etTelefonoMedico.getText().toString();
            String contrasena = etContrasenaMedico.getText().toString();

            if (!nombre.isEmpty() && !especializacion.isEmpty() && !correo.isEmpty() && !contrasena.isEmpty() && !telefono.isEmpty() && horarioSeleccionado != null) {
                registrarMedico(nombre, especializacion, correo, contrasena, telefono, horarioSeleccionado);
            } else {
                Toast.makeText(RegistroDoctor.this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showBottomDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheet_layout_doctor);

        // Obtener referencias a los TimePicker del modal
        TimePicker timePickerInicio = dialog.findViewById(R.id.timePickerInicio);
        TimePicker timePickerFin = dialog.findViewById(R.id.timePickerFin);

        // Acción para actualizar el horario cuando el usuario cierre el diálogo
        Button btnAceptar = dialog.findViewById(R.id.btnAceptar); // Asumiendo que tienes un botón para aceptar el horario
        btnAceptar.setOnClickListener(v -> {
            // Obtener los valores de los TimePicker
            int horaInicio = timePickerInicio.getHour();
            int minutoInicio = timePickerInicio.getMinute();
            int horaFin = timePickerFin.getHour();
            int minutoFin = timePickerFin.getMinute();

            // Actualizar el horario en la variable global
            horarioSeleccionado = String.format("%02d:%02d - %02d:%02d", horaInicio, minutoInicio, horaFin, minutoFin);

            // Actualizar el horario en el botón
            horarioButton.setText(horarioSeleccionado);

            // Cerrar el diálogo
            dialog.dismiss();
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void cargarEspecializacionesDesdeFirestore() {
        db.collection("Servicios").document("Todos los servicios")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> especializaciones = (List<String>) task.getResult().get("Servicios");
                        if (especializaciones != null) {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, especializaciones);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerEspecializacion.setAdapter(adapter);
                        } else {
                            Toast.makeText(RegistroDoctor.this, "No se encontraron especializaciones", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(RegistroDoctor.this, "Error al cargar especializaciones", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registrarMedico(String nombre, String especializacion, String correo, String contrasena, String telefono, String horario) {
        mAuth.createUserWithEmailAndPassword(correo, contrasena).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String uid = mAuth.getCurrentUser().getUid();

                // Crear mapa de datos para Firestore
                Map<String, Object> medico = new HashMap<>();
                medico.put("id", uid);
                medico.put("nombre", nombre);
                medico.put("especializacion", especializacion);
                medico.put("correo", correo);
                medico.put("telefono", telefono);
                medico.put("rol", "medico");
                medico.put("horario", horario);  // Guardar horario en Firestore

                // Guardar datos en Firestore en la colección "user"
                db.collection("user").document(uid).set(medico).addOnSuccessListener(aVoid -> {
                    enviarCorreoVerificacion();
                    Toast.makeText(RegistroDoctor.this, "Médico registrado con éxito", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Toast.makeText(RegistroDoctor.this, "Error al registrar médico", Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(RegistroDoctor.this, "Error en el registro", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enviarCorreoVerificacion() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(RegistroDoctor.this, "Correo de verificación enviado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegistroDoctor.this, "Error al enviar el correo de verificación", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
