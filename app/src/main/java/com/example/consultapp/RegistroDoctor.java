package com.example.consultapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistroDoctor extends AppCompatActivity {

    private EditText etNombreMedico, etCedula, etCorreoMedico, etContrasenaMedico, etTelefonoMedico;
    private Spinner spinnerEspecializacion;
    private Button horarioButton, btnRegistrarMedico;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private List<String> horarioSeleccionado;
    private TextView verifyLink;

    // Variables para guardar las credenciales del administrador
    private String adminEmail;
    private String adminPassword = "Administracion123"; // Cambia esto por la contraseña real

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_doctor);

        // Inicializar FirebaseAuth y Realtime Database
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        // Guardar el correo del administrador
        FirebaseAuth currentUser = FirebaseAuth.getInstance();
        if (currentUser.getCurrentUser() != null) {
            adminEmail = currentUser.getCurrentUser().getEmail();
        }

        // Referencias a los elementos en el layout
        etNombreMedico = findViewById(R.id.nombreMedico);
        etCedula = findViewById(R.id.cedula);
        spinnerEspecializacion = findViewById(R.id.spinner_especializacion);
        etCorreoMedico = findViewById(R.id.correoMedico);
        etContrasenaMedico = findViewById(R.id.contrasenaMedico);
        etTelefonoMedico = findViewById(R.id.telefonoMedico);
        horarioButton = findViewById(R.id.horarioButton);
        btnRegistrarMedico = findViewById(R.id.btn_registrarMedico);

        // Listener para mostrar el diálogo para seleccionar el horario
        horarioButton.setOnClickListener(v -> showBottomDialog());

        // Cargar especializaciones desde Realtime Database
        cargarEspecializacionesDesdeRealtime();

        // Initialize views
        verifyLink = findViewById(R.id.verify_link);

        // Set up the link text and behavior
        verifyLink.setText("Verificar");
        verifyLink.setMovementMethod(LinkMovementMethod.getInstance());
        verifyLink.setOnClickListener(v -> {
            String url = "https://www.cedulaprofesional.sep.gob.mx/cedula/presidencia/indexAvanzada.action"; // URL to open
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        // Registrar médico
        btnRegistrarMedico.setOnClickListener(v -> {
            String nombre = etNombreMedico.getText().toString();
            String cedula = etCedula.getText().toString();
            String especializacion = spinnerEspecializacion.getSelectedItem().toString();
            String correo = etCorreoMedico.getText().toString();
            String telefono = etTelefonoMedico.getText().toString();
            String contrasena = etContrasenaMedico.getText().toString();

            if (!nombre.isEmpty() && !cedula.isEmpty() && !especializacion.isEmpty() && !correo.isEmpty() && !contrasena.isEmpty() && !telefono.isEmpty() && horarioSeleccionado != null) {
                registrarMedico(nombre, cedula, especializacion, correo, contrasena, telefono, horarioSeleccionado);
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
        Button btnAceptar = dialog.findViewById(R.id.btnAceptarHorario);
        btnAceptar.setOnClickListener(v -> {
            int horaInicio = timePickerInicio.getHour();
            int minutoInicio = timePickerInicio.getMinute();
            int horaFin = timePickerFin.getHour();
            int minutoFin = timePickerFin.getMinute();

            List<String> horarios = generarHorarios(horaInicio, minutoInicio, horaFin, minutoFin);

            if (horarios.isEmpty()) {
                Toast.makeText(this, "El rango de horario no es válido.", Toast.LENGTH_SHORT).show();
                return;
            }

            horarioSeleccionado = horarios;

            // Actualizar el texto del botón para mostrar el rango
            horarioButton.setText(String.format("%02d:%02d - %02d:%02d", horaInicio, minutoInicio, horaFin, minutoFin));

            dialog.dismiss();
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void cargarEspecializacionesDesdeRealtime() {
        databaseReference.child("Servicios").child("TodosLosServicios").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                List<String> especializaciones = new ArrayList<>();
                for (com.google.firebase.database.DataSnapshot especialidadSnapshot : snapshot.getChildren()) {
                    String especializacion = especialidadSnapshot.getValue(String.class);
                    if (especializacion != null) {
                        especializaciones.add(especializacion);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(RegistroDoctor.this, R.layout.item_spinner2, especializaciones);
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                spinnerEspecializacion.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                Toast.makeText(RegistroDoctor.this, "Error al cargar especializaciones", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registrarMedico(String nombre, String cedula, String especializacion, String correo, String contrasena, String telefono, List<String> horarios) {
        mAuth.createUserWithEmailAndPassword(correo, contrasena).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String uid = mAuth.getCurrentUser().getUid();

                // Crear mapa de datos para Realtime Database
                Map<String, Object> medico = new HashMap<>();
                medico.put("id", uid);
                medico.put("nombre", nombre);
                medico.put("cedula", cedula);
                medico.put("especializacion", especializacion);
                medico.put("correo", correo);
                medico.put("telefono", telefono);
                medico.put("rol", "medico");
                medico.put("horarios", horarios);

                // Guardar datos en la rama "Medicos"
                databaseReference.child("Medicos").child(uid).setValue(medico)
                        .addOnSuccessListener(aVoid -> {
                            enviarCorreoVerificacion();
                            Toast.makeText(RegistroDoctor.this, "Médico registrado con éxito", Toast.LENGTH_SHORT).show();

                            // Restaurar la sesión del administrador
                            mAuth.signOut();
                            mAuth.signInWithEmailAndPassword(adminEmail, adminPassword).addOnCompleteListener(adminTask -> {
                                if (adminTask.isSuccessful()) {
                                    Toast.makeText(RegistroDoctor.this, "Sesión restaurada para el administrador", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(RegistroDoctor.this, "Error al restaurar sesión del administrador", Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(RegistroDoctor.this, "Error al registrar médico", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(RegistroDoctor.this, "Error en el registro", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enviarCorreoVerificacion() {
        if (mAuth.getCurrentUser() != null) {
            mAuth.getCurrentUser().sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegistroDoctor.this, "Correo de verificación enviado", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegistroDoctor.this, "Error al enviar el correo de verificación", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private List<String> generarHorarios(int horaInicio, int minutoInicio, int horaFin, int minutoFin) {
        List<String> horarios = new ArrayList<>();

        int inicioEnMinutos = horaInicio * 60 + minutoInicio;
        int finEnMinutos = horaFin * 60 + minutoFin;

        if (inicioEnMinutos >= finEnMinutos) {
            return horarios; // Devuelve una lista vacía si el rango no es válido
        }

        for (int tiempo = inicioEnMinutos; tiempo <= finEnMinutos; tiempo += 30) {
            int horas = tiempo / 60;
            int minutos = tiempo % 60;

            String periodo = (horas >= 12) ? "PM" : "AM";
            int horasFormato12 = (horas == 0 || horas == 12) ? 12 : horas % 12;
            horarios.add(String.format("%02d:%02d %s", horasFormato12, minutos, periodo));
        }

        return horarios;
    }
}
