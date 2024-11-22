package com.example.consultapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServicioDetalle extends AppCompatActivity {

    private TextView servicioTextView, descripcionTextView, doctores;
    private EditText editarDescripcionServicio;
    private DatabaseReference databaseReference;
    private ImageButton editarServicio;
    private ImageButton eliminarServicio;
    private DoctorAdapter doctorAdapter;
    private List<String> doctorList = new ArrayList<>();

    // Variable para rastrear si estamos en modo edición
    private boolean enModoEdicion = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servicio_detalle);

        // Inicializar Firebase Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Obtener el nombre del servicio desde el Intent
        String nombreServicio = getIntent().getStringExtra("NOMBRE_SERVICIO");

        // Obtener las vistas
        servicioTextView = findViewById(R.id.servicioTextView);
        descripcionTextView = findViewById(R.id.descripcion_TextView);
        editarDescripcionServicio = findViewById(R.id.editarDescripcionServicio);
        editarServicio = findViewById(R.id.editarServicio);
        eliminarServicio = findViewById(R.id.eliminarServicio);

        RecyclerView doctoresRecyclerView = findViewById(R.id.doctoresRecyclerView);
        doctorAdapter = new DoctorAdapter(doctorList);
        doctoresRecyclerView.setAdapter(doctorAdapter);
        doctoresRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Mostrar el nombre del servicio en el TextView
        servicioTextView.setText(nombreServicio);

        // Cargar la descripción y los doctores relacionados al servicio
        cargarDescripcionServicio(nombreServicio);
        cargarDoctores(nombreServicio);

        // Alternar entre editar y guardar
        editarServicio.setOnClickListener(v -> {
            if (!enModoEdicion) {
                activarEdicion();
            } else {
                String nuevaDescripcion = editarDescripcionServicio.getText().toString();
                if (!nuevaDescripcion.isEmpty()) {
                    actualizarDescripcionServicio(nombreServicio, nuevaDescripcion);
                }
            }
        });

        eliminarServicio.setOnClickListener(v -> eliminarServicio(nombreServicio));
    }

    private void cargarDescripcionServicio(String nombreServicio) {
        databaseReference.child("Servicios").child("DetalleServicios").child(nombreServicio).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String descripcion = snapshot.child("descripcion").getValue(String.class);
                    descripcionTextView.setText(descripcion != null ? descripcion : "Sin descripción disponible");
                } else {
                    Toast.makeText(ServicioDetalle.this, "Servicio no encontrado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ServicioDetalle.this, "Error al obtener la descripción", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarDoctores(String especializacion) {
        databaseReference.child("users").orderByChild("especializacion").equalTo(especializacion).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                doctorList.clear(); // Limpiar la lista para evitar duplicados
                for (DataSnapshot doctorSnapshot : snapshot.getChildren()) {
                    String nombre = doctorSnapshot.child("nombre").getValue(String.class);
                    if (nombre != null) {
                        doctorList.add(nombre);
                    }
                }
                doctorAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ServicioDetalle.this, "Error al obtener los doctores", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarDescripcionServicio(String nombreServicio, String nuevaDescripcion) {
        databaseReference.child("Servicios").child("DetalleServicios").child(nombreServicio).child("descripcion").setValue(nuevaDescripcion)
                .addOnSuccessListener(aVoid -> {
                    descripcionTextView.setText(nuevaDescripcion);
                    descripcionTextView.setVisibility(View.VISIBLE);
                    editarDescripcionServicio.setVisibility(View.GONE);

                    // Cambiar a modo no edición
                    enModoEdicion = false;
                    editarServicio.setImageResource(R.drawable.round_edit_24); // Cambia el ícono a "Editar"
                    Toast.makeText(ServicioDetalle.this, "Descripción actualizada", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(ServicioDetalle.this, "Error al actualizar la descripción", Toast.LENGTH_SHORT).show());
    }

    private void eliminarServicio(String nombreServicio) {
        // Eliminar de la lista de servicios generales
        databaseReference.child("Servicios").child("TodosLosServicios").child(nombreServicio).removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Eliminar el detalle del servicio
                    databaseReference.child("Servicios").child("DetalleServicios").child(nombreServicio).removeValue()
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(ServicioDetalle.this, "Servicio eliminado correctamente", Toast.LENGTH_SHORT).show();
                                finish(); // Finalizar la actividad actual
                            })
                            .addOnFailureListener(e -> Toast.makeText(ServicioDetalle.this, "Error al eliminar el detalle del servicio", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(ServicioDetalle.this, "Error al eliminar el servicio de la lista general", Toast.LENGTH_SHORT).show());
    }

    private void activarEdicion() {
        enModoEdicion = true;
        editarServicio.setImageResource(R.drawable.round_edit_24); // Cambia el ícono a "Guardar"
        descripcionTextView.setVisibility(View.GONE);
        editarDescripcionServicio.setVisibility(View.VISIBLE);
        editarDescripcionServicio.setText(descripcionTextView.getText());
    }
}
