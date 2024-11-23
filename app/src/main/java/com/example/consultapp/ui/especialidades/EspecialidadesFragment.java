package com.example.consultapp.ui.especialidades;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;


import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.consultapp.R;
import com.example.consultapp.Servicio;
import com.example.consultapp.ServicioAdapter;
import com.example.consultapp.databinding.FragmentEspecialidadesAdminBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EspecialidadesFragment extends Fragment {

    private FragmentEspecialidadesAdminBinding binding; // Binding para acceder a las vistas
    private ServicioAdapter servicioAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inicializa el binding
        binding = FragmentEspecialidadesAdminBinding.inflate(inflater, container, false);


        // Configura el FloatingActionButton y su clic
        FloatingActionButton fab = binding.floatingActionButton;
        fab.setOnClickListener(view -> showBottomDialog());

        // Retorna la vista raíz de binding
        return binding.getRoot();
    }


    private void showBottomDialog() {
        // Crear el diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.bottomsheet_layout, null);

        EditText nombreEspecialidad = view.findViewById(R.id.nombre_especialidad);
        EditText descripcionEspecialidad = view.findViewById(R.id.descripcion_especialidad);
        Button btnAgregarEspecialidad = view.findViewById(R.id.btnAgregarEspecialidad);

        builder.setView(view);
        AlertDialog dialog = builder.create();

        // Evento para el botón "Agregar"
        btnAgregarEspecialidad.setOnClickListener(v -> {
            String nombre = nombreEspecialidad.getText().toString().trim();
            String descripcion = descripcionEspecialidad.getText().toString().trim();

            if (!nombre.isEmpty() && !descripcion.isEmpty()) {
                // Llamar al método para registrar en Firebase
                registrarServicioEnFirebase(nombre, descripcion);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void registrarServicioEnFirebase(String nombre, String descripcion) {
        DatabaseReference serviciosRef = FirebaseDatabase.getInstance().getReference("Servicios/DetalleServicios");

        // Crear un objeto de servicio
        HashMap<String, String> servicio = new HashMap<>();
        servicio.put("nombre", nombre);
        servicio.put("descripcion", descripcion);

        // Subir los datos a Firebase (usa el nombre del servicio como ID único)
        serviciosRef.child(nombre).setValue(servicio)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Servicio agregado correctamente", Toast.LENGTH_SHORT).show();
                        cargarServicios(); // Actualiza la vista dinámica
                    } else {
                        Toast.makeText(this, "Error al agregar el servicio", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cargarServicios() {
        DatabaseReference serviciosRef = FirebaseDatabase.getInstance().getReference("Servicios/DetalleServicios");

        serviciosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                linearServicios.removeAllViews(); // Limpia los botones previos

                for (DataSnapshot servicioSnapshot : dataSnapshot.getChildren()) {
                    String nombre = servicioSnapshot.child("nombre").getValue(String.class);
                    String descripcion = servicioSnapshot.child("descripcion").getValue(String.class);

                    // Crear un botón para cada servicio
                    Button boton = new Button(EspecialidadesFragment.this);
                    boton.setText(nombre);

                    // Mostrar la descripción al hacer clic
                    boton.setOnClickListener(v ->
                            Toast.makeText(EspecialidadesFragment.this, descripcion, Toast.LENGTH_LONG).show()
                    );

                    // Agregar botón al LinearLayout
                    linearServicios.addView(boton);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EspecialidadesFragment.this, "Error al cargar servicios: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Liberar el binding cuando la vista se destruya
    }
}