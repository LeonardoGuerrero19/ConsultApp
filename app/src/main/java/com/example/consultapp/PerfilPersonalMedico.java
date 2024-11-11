package com.example.consultapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

public class PerfilPersonalMedico extends AppCompatActivity {

    private TextView nombre, especializacion;
    private String medicoId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_personal_medico);

        // Obtener el ID del médico del Intent
        medicoId = getIntent().getStringExtra("medicoId");

        // Referencias a los elementos del layout
        nombre = findViewById(R.id.nombre);
        especializacion = findViewById(R.id.especializacion);

        // Cargar los datos del médico desde la base de datos
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user").document(medicoId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Medico medico = documentSnapshot.toObject(Medico.class);
                        if (medico != null) {
                            nombre.setText(medico.getNombre());
                            especializacion.setText(medico.getEspecializacion());
                        }
                    }
                });
    }
}