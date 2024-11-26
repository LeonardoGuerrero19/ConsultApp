package com.example.consultapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PerfilEspecialidadActivity extends AppCompatActivity {

    private TextView tvServicioNombre, tvServicioDescripcion, tvMedicos;
    private LinearLayout linearMedicos;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_especialidad);

        // Inicializar las vistas
        tvServicioNombre = findViewById(R.id.tvServicioNombre);
        tvServicioDescripcion = findViewById(R.id.tvServicioDescripcion);
        tvMedicos = findViewById(R.id.tvMedicos);
        linearMedicos = findViewById(R.id.linearMedicos);

        // Inicializar Firebase Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Obtener el nombre del servicio desde el Intent
        String nombreServicio = getIntent().getStringExtra("nombreServicio");

        // Mostrar el nombre del servicio
        tvServicioNombre.setText(nombreServicio);

        // Obtener la descripción y los médicos asociados a este servicio
        obtenerServicioYMedicos(nombreServicio);
    }

    private void obtenerServicioYMedicos(String nombreServicio) {
        // Obtener la descripción del servicio
        databaseReference.child("Servicios").child("TodosLosServicios").child(nombreServicio)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String descripcion = snapshot.child("descripcion").getValue(String.class);
                            if (descripcion != null) {
                                tvServicioDescripcion.setText(descripcion);
                            } else {
                                tvServicioDescripcion.setText("Descripción no disponible");
                            }
                        } else {
                            tvServicioDescripcion.setText("Servicio no encontrado");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(PerfilEspecialidadActivity.this, "Error al cargar los datos del servicio", Toast.LENGTH_SHORT).show();
                    }
                });

        // Obtener médicos con esta especialización
        databaseReference.child("Medicos").orderByChild("especializacion").equalTo(nombreServicio)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        linearMedicos.removeAllViews(); // Limpiar antes de agregar nuevos médicos
                        if (snapshot.exists()) {
                            for (DataSnapshot medicoSnapshot : snapshot.getChildren()) {
                                String medicoNombre = medicoSnapshot.child("nombre").getValue(String.class);
                                if (medicoNombre != null) {
                                    // Inflar el diseño del botón desde el XML
                                    LayoutInflater inflater = LayoutInflater.from(PerfilEspecialidadActivity.this);
                                    Button medicoButton = (Button) inflater.inflate(R.layout.button_medico, null);

                                    medicoButton.setText(medicoNombre); // Asignar nombre al botón

                                    // Establecer el listener para el botón
                                    medicoButton.setOnClickListener(v -> {
                                        // Obtener el ID del médico
                                        String medicoId = medicoSnapshot.getKey();  // Esto es el ID único del médico en Firebase

                                        // Redirigir a la actividad de agendar cita con el ID y la información del médico
                                        Intent intent = new Intent(PerfilEspecialidadActivity.this, AgendaActivity.class);
                                        intent.putExtra("nombreMedico", medicoNombre); // Pasar el nombre del médico
                                        intent.putExtra("medicoId", medicoId); // Pasar el ID del médico
                                        intent.putExtra("especializacion", nombreServicio); // Pasar la especialización
                                        startActivity(intent);
                                    });


                                    // Agregar el botón al layout
                                    linearMedicos.addView(medicoButton);
                                }
                            }
                        } else {
                            tvMedicos.setText("No hay médicos disponibles para este servicio.");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(PerfilEspecialidadActivity.this, "Error al cargar los médicos", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}