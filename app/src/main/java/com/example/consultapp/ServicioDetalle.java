package com.example.consultapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ServicioDetalle extends AppCompatActivity {

    private TextView servicioTextView, descripcionTextView, doctores;
    private EditText editarDescripcionServicio;
    private FirebaseFirestore db;
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

        // Inicializar Firebase Firestore
        db = FirebaseFirestore.getInstance();

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

        // Mostrar el EditText solo cuando se haga clic en el TextView
        descripcionTextView.setOnClickListener(v -> activarEdicion());
        editarDescripcionServicio.setOnClickListener(v -> activarEdicion());

        // Alternar entre editar y guardar
        editarServicio.setOnClickListener(v -> {
            if (!enModoEdicion) {
                // Activar el modo edición
                enModoEdicion = true;
                descripcionTextView.setVisibility(View.GONE);
                editarDescripcionServicio.setVisibility(View.VISIBLE);
                editarDescripcionServicio.setText(descripcionTextView.getText());
                editarServicio.setImageResource(R.drawable.round_edit_24); // Cambia el icono a "Guardar"
            } else {
                // Guardar los cambios
                String nuevaDescripcion = editarDescripcionServicio.getText().toString();
                if (!nuevaDescripcion.isEmpty()) {
                    actualizarDescripcionServicio(nuevaDescripcion);
                }
                enModoEdicion = false;
                editarServicio.setImageResource(R.drawable.round_edit_24); // Cambia el icono a "Editar"
            }
        });

        eliminarServicio.setOnClickListener(v -> eliminarServicio());
    }

    private void cargarDescripcionServicio(String nombreServicio) {
        DocumentReference servicioRef = db.collection("Servicios").document(nombreServicio);
        servicioRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String descripcion = documentSnapshot.getString("descripcion");
                        descripcionTextView.setText(descripcion != null ? descripcion : "Sin descripción disponible");
                    } else {
                        Toast.makeText(ServicioDetalle.this, "Servicio no encontrado", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(ServicioDetalle.this, "Error al obtener la descripción", Toast.LENGTH_SHORT).show());
    }

    private void cargarDoctores(String especializacion) {
        db.collection("user")
                .whereEqualTo("especializacion", especializacion)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    doctorList.clear(); // Limpiar la lista para evitar duplicados
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String nombre = document.getString("nombre");
                        if (nombre != null) {
                            doctorList.add(nombre); // Agregar el nombre del doctor a la lista
                        }
                    }
                    doctorAdapter.notifyDataSetChanged(); // Notificar al adaptador sobre el cambio de datos
                })
                .addOnFailureListener(e -> Toast.makeText(ServicioDetalle.this, "Error al obtener los doctores", Toast.LENGTH_SHORT).show());
    }


    private void actualizarDescripcionServicio(String nuevaDescripcion) {
        DocumentReference servicioRef = db.collection("Servicios").document(servicioTextView.getText().toString());
        servicioRef.update("descripcion", nuevaDescripcion)
                .addOnSuccessListener(aVoid -> {
                    descripcionTextView.setText(nuevaDescripcion);
                    descripcionTextView.setVisibility(View.VISIBLE);
                    editarDescripcionServicio.setVisibility(View.GONE);

                    // Cambiar a modo no edición
                    enModoEdicion = false;
                    editarServicio.setImageResource(R.drawable.round_edit_24); // Cambia el ícono a "Editar"
                })
                .addOnFailureListener(e -> Toast.makeText(ServicioDetalle.this, "Error al actualizar la descripción", Toast.LENGTH_SHORT).show());
    }


    private void activarEdicion() {
        if (!enModoEdicion) {
            enModoEdicion = true;
            editarServicio.setImageResource(R.drawable.round_edit_24); // Cambia el ícono a "Guardar"
        }
        descripcionTextView.setVisibility(View.GONE);
        editarDescripcionServicio.setVisibility(View.VISIBLE);
        editarDescripcionServicio.setText(descripcionTextView.getText());
    }


    private void eliminarServicio() {
        String nombreServicio = servicioTextView.getText().toString();
        if (nombreServicio != null && !nombreServicio.isEmpty()) {
            DocumentReference listaServiciosRef = db.collection("Servicios").document("Todos los servicios");
            listaServiciosRef.update("Servicios", FieldValue.arrayRemove(nombreServicio))
                    .addOnSuccessListener(aVoid -> {
                        DocumentReference servicioRef = db.collection("Servicios").document(nombreServicio);
                        servicioRef.delete()
                                .addOnSuccessListener(aVoid1 -> {
                                    Toast.makeText(ServicioDetalle.this, "Servicio eliminado correctamente", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(ServicioDetalle.this, HomeFragment.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(ServicioDetalle.this, "Error al eliminar el documento del servicio", Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> Toast.makeText(ServicioDetalle.this, "Error al eliminar el servicio de la lista", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(ServicioDetalle.this, "No se ha encontrado el servicio", Toast.LENGTH_SHORT).show();
        }
    }
}
