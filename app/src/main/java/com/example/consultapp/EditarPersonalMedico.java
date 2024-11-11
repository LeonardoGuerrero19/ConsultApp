package com.example.consultapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class EditarPersonalMedico extends AppCompatActivity {

    private TextView nombre;
    private EditText especializacion, telefono;
    private Button horario, btn_guardar;
    private String medicoId;

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

        // Cargar los datos del médico desde la base de datos
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user").document(medicoId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Medico medico = documentSnapshot.toObject(Medico.class);
                        if (medico != null) {
                            nombre.setText(medico.getNombre());
                            especializacion.setText(medico.getEspecializacion());
                            telefono.setText(medico.getTelefono());
                            horario.setText(medico.getHorario());
                        }
                    }
                });

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

            // Actualizar los datos en Firestore
            db.collection("user").document(medicoId)
                    .update(
                            "especializacion", nuevaEspecializacion,
                            "telefono", nuevoTelefono,
                            "horario", nuevoHorario
                    )
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(EditarPersonalMedico.this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
                        finish();  // Cerrar la actividad después de guardar los cambios
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EditarPersonalMedico.this, "Error al actualizar los datos", Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
