package com.example.consultapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class EditarPersonalMedico extends AppCompatActivity {

    private TextView nombre;
    private EditText especializacion, telefono;
    private Button btn_guardar, horario;
    private String medicoId;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_personal_medico);

        // Obtener el ID del médico del Intent
        medicoId = getIntent().getStringExtra("medicoId");

        // Referencias a los elementos del layout
        nombre = findViewById(R.id.medicoTextView);
        especializacion = findViewById(R.id.editEspecializacion);
        telefono = findViewById(R.id.editTelefono);
        horario = findViewById(R.id.editHorario);
        btn_guardar = findViewById(R.id.btnGuardarCambios);

        // Inicializar la referencia de la base de datos
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Cargar los datos del médico desde Realtime Database
        cargarDatosMedico();

        // Acción para guardar los cambios
        btn_guardar.setOnClickListener(view -> {
            String nuevaEspecializacion = especializacion.getText().toString();
            String nuevoTelefono = telefono.getText().toString();
            String nuevoHorario = horario.getText().toString();

            // Validar que los campos no estén vacíos
            if (nuevaEspecializacion.isEmpty() || nuevoTelefono.isEmpty() || nuevoHorario.isEmpty()) {
                Toast.makeText(EditarPersonalMedico.this, "Todos los campos deben ser llenados", Toast.LENGTH_SHORT).show();
                return;
            }

            // Actualizar los datos en Realtime Database
            actualizarDatosMedico(nuevaEspecializacion, nuevoTelefono, Arrays.asList(nuevoHorario.split(",")));
        });
    }

    private void cargarDatosMedico() {
        databaseReference.child("users").child(medicoId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Medico medico = snapshot.getValue(Medico.class);
                    if (medico != null) {
                        nombre.setText(medico.getNombre());
                        especializacion.setText(medico.getEspecializacion());
                        telefono.setText(medico.getTelefono());
                        horario.setText(medico.getHorarios() != null ? String.join(", ", medico.getHorarios()) : "");
                    }
                } else {
                    Toast.makeText(EditarPersonalMedico.this, "Médico no encontrado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(EditarPersonalMedico.this, "Error al cargar los datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarDatosMedico(String nuevaEspecializacion, String nuevoTelefono, List<String> nuevoHorario) {
        // Crear un mapa con los datos actualizados
        Medico medicoActualizado = new Medico(
                nombre.getText().toString(),
                nuevaEspecializacion,
                nuevoTelefono,
                nuevoHorario
        );

        // Actualizar en ambas ramas: "users" y "Medicos"
        DatabaseReference usersRef = databaseReference.child("users").child(medicoId);
        DatabaseReference medicosRef = databaseReference.child("Medicos").child(medicoId);

        usersRef.setValue(medicoActualizado)
                .addOnSuccessListener(aVoid -> medicosRef.setValue(medicoActualizado)
                        .addOnSuccessListener(aVoid1 -> {
                            Toast.makeText(EditarPersonalMedico.this, "Datos actualizados correctamente en ambas ramas", Toast.LENGTH_SHORT).show();
                            finish(); // Cerrar la actividad después de guardar los cambios
                        })
                        .addOnFailureListener(e -> Toast.makeText(EditarPersonalMedico.this, "Error al actualizar en la rama Medicos", Toast.LENGTH_SHORT).show()))
                .addOnFailureListener(e -> Toast.makeText(EditarPersonalMedico.this, "Error al actualizar en la rama users", Toast.LENGTH_SHORT).show());
    }
}
