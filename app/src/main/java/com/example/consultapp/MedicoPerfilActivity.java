package com.example.consultapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MedicoPerfilActivity extends AppCompatActivity {
    private RatingBar ratingBar;
    private String medicoId;  // ID del médico en la base de datos de Firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medico_perfil);

        // Obtener el RatingBar
        ratingBar = findViewById(R.id.ratingBar);

        // Obtener los datos del intent
        String nombre = getIntent().getStringExtra("nombre");
        String especialidad = getIntent().getStringExtra("especialidad");
        String cedula = getIntent().getStringExtra("cedula");
        String fotoPerfil = getIntent().getStringExtra("fotoPerfil");

        // Obtener el ID del médico (esto puede provenir de la base de datos o de otra actividad)
        medicoId = getIntent().getStringExtra("medicoId");  // Asegúrate de enviar este ID desde la actividad anterior

        // Mostrar los datos en los elementos de la UI
        TextView tvNombre = findViewById(R.id.tvNombre);
        TextView tvEspecialidad = findViewById(R.id.tvEspecialidad);
        TextView tvCedula = findViewById(R.id.tvCedula);
        ImageView ivFotoPerfil = findViewById(R.id.ivFotoPerfil);
        TextView tvHorario = findViewById(R.id.horario);  // Agregar el TextView para el horario
        TextView tvTelefono = findViewById(R.id.telefono);  // Agregar el TextView para el teléfono

        tvNombre.setText("Dr. " + nombre);
        tvEspecialidad.setText(especialidad);
        tvCedula.setText(cedula);

        // Convertir 18dp a píxeles
        float radius = 18 * getResources().getDisplayMetrics().density;

        // Cargar la foto de perfil con Glide
        Glide.with(this)
                .load(fotoPerfil)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners((int) radius))) // Aplicar borde redondeado
                .placeholder(R.drawable.round_person_outline_24)
                .into(ivFotoPerfil);

        // Obtener el horario y el teléfono
        obtenerDatosMedico(tvHorario, tvTelefono);

        // Obtener el promedio de calificaciones
        obtenerPromedioCalificaciones();
    }

    // Función para obtener el horario y el teléfono
    private void obtenerDatosMedico(TextView tvHorario, TextView tvTelefono) {
        DatabaseReference medicosRef = FirebaseDatabase.getInstance().getReference("Medicos");
        DatabaseReference medicoRef = medicosRef.child(medicoId);

        // Obtener el horario y el teléfono desde Firebase
        medicoRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    // Obtener los horarios y teléfono desde Firebase
                    List<String> horarios = (List<String>) task.getResult().child("horarios").getValue();
                    String telefono = task.getResult().child("telefono").getValue(String.class);

                    // Verificar si hay horarios
                    if (horarios != null && !horarios.isEmpty()) {
                        // Mostrar solo el primer y el último horario
                        String primerHorario = horarios.get(0);
                        String ultimoHorario = horarios.get(horarios.size() - 1);

                        tvHorario.setText(primerHorario + " - " + ultimoHorario);
                    } else {
                        tvHorario.setText("Horario: No disponible");
                    }

                    // Mostrar el teléfono
                    tvTelefono.setText(telefono);
                } else {
                    Toast.makeText(MedicoPerfilActivity.this, "Datos del médico no encontrados", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MedicoPerfilActivity.this, "Error al obtener los datos del médico", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Función para obtener las calificaciones y calcular el promedio
    private void obtenerPromedioCalificaciones() {
        DatabaseReference medicosRef = FirebaseDatabase.getInstance().getReference("Medicos");
        DatabaseReference medicoRef = medicosRef.child(medicoId);

        // Obtener las calificaciones desde Firebase
        medicoRef.child("calificaciones").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Si hay calificaciones, calcular el promedio
                List<Float> calificaciones = new ArrayList<>();
                if (task.getResult().exists()) {
                    // Convertir las calificaciones de Double a Float
                    for (Object calificacionObj : (List<?>) task.getResult().getValue()) {
                        // Si la calificación es un valor Double, la convertimos a Float
                        if (calificacionObj instanceof Double) {
                            calificaciones.add(((Double) calificacionObj).floatValue());
                        } else if (calificacionObj instanceof Float) {
                            calificaciones.add((Float) calificacionObj);
                        }
                    }
                }

                if (!calificaciones.isEmpty()) {
                    // Calcular el promedio
                    float suma = 0;
                    for (float calificacion : calificaciones) {
                        suma += calificacion;
                    }
                    float promedio = suma / calificaciones.size();

                    // Mostrar el promedio en el RatingBar
                    ratingBar.setRating(promedio);
                } else {
                    // Si no hay calificaciones, mostrar un mensaje
                    Toast.makeText(MedicoPerfilActivity.this, "Este médico aún no tiene calificaciones", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MedicoPerfilActivity.this, "Error al obtener calificaciones", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
