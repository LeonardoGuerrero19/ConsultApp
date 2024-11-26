package com.example.consultapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MoreDoctoresActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private LinearLayout linearPersonalMedico;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_doctores);

        // Inicializar FirebaseAuth y Realtime Database
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        linearPersonalMedico = findViewById(R.id.linearPersonalMedico);

        cargarPersonalMedico();
    }

    private void cargarPersonalMedico() {
        DatabaseReference medicosRef = databaseReference.child("Medicos");

        medicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Limpiar las vistas previas para evitar duplicados
                linearPersonalMedico.removeAllViews();

                // Iterar sobre los médicos en la base de datos
                if (snapshot.exists()) {
                    int contador = 0;

                    for (DataSnapshot medicoSnapshot : snapshot.getChildren()) {

                        String nombre = medicoSnapshot.child("nombre").getValue(String.class);
                        String especialidad = medicoSnapshot.child("especializacion").getValue(String.class);
                        String cedula = medicoSnapshot.child("cedula").getValue(String.class);
                        String fotoPerfil = medicoSnapshot.child("fotoPerfil").getValue(String.class); // Obtener la URL de la foto de perfil
                        String medicoId = medicoSnapshot.getKey(); // Obtener el ID único del médico

                        // Crear un layout dinámico para cada médico
                        View medicoView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_medico, linearPersonalMedico, false);

                        // Configurar los TextViews con los datos del médico
                        TextView tvNombre = medicoView.findViewById(R.id.tvNombreMedico);
                        TextView tvEspecialidad = medicoView.findViewById(R.id.tvEspecialidad);
                        TextView tvCedula = medicoView.findViewById(R.id.tvCedula);
                        ImageView ivFotoPerfil = medicoView.findViewById(R.id.imageView); // ImageView donde se mostrará la foto de perfil
                        Button btnVerPerfil = medicoView.findViewById(R.id.btnVerPerfilMedico); // Botón de "Ver perfil"

                        tvNombre.setText("Dr. " + nombre);
                        tvEspecialidad.setText(especialidad);
                        tvCedula.setText(cedula);

                        // Cargar la foto del médico usando Glide
                        if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
                            Glide.with(getApplicationContext())
                                    .load(fotoPerfil)
                                    .into(ivFotoPerfil);
                        }

                        // Agregar el layout del médico al contenedor
                        linearPersonalMedico.addView(medicoView);

                        // Configurar el botón de "Ver perfil"
                        btnVerPerfil.setOnClickListener(v -> {
                            // Redirigir a la actividad de perfil del médico
                            Intent intent = new Intent(getApplicationContext(), MedicoPerfilActivity.class);
                            intent.putExtra("nombre", nombre);
                            intent.putExtra("especialidad", especialidad);
                            intent.putExtra("cedula", cedula);
                            intent.putExtra("fotoPerfil", fotoPerfil);
                            intent.putExtra("medicoId", medicoId); // Pasar el ID del médico
                            startActivity(intent);
                        });

                        contador++; // Incrementar el contador
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Error al cargar los médicos", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
