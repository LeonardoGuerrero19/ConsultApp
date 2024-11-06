package com.example.funcion_loginregistro;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;


import java.util.ArrayList;
import java.util.List;

public class AgregarServicioActivity extends AppCompatActivity {
    private EditText agregarServicioEditText;
    private Button agregarServicioButton;
    private RecyclerView recyclerViewServicios;
    private ServicioAdapter servicioAdapter;
    private List<String> listaServicios = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_servicio);

        // Inicializar las vistas
        agregarServicioEditText = findViewById(R.id.agregarServicio);
        agregarServicioButton = findViewById(R.id.btn_AgregarServicio);
        recyclerViewServicios = findViewById(R.id.recyclerViewServicios);

        // Configurar el RecyclerView
        recyclerViewServicios.setLayoutManager(new LinearLayoutManager(this));
        servicioAdapter = new ServicioAdapter(listaServicios);
        recyclerViewServicios.setAdapter(servicioAdapter);

        // Configurar el botón para agregar un servicio
        agregarServicioButton.setOnClickListener(v -> agregarServicio());

        // Cargar los servicios existentes desde Firestore
        cargarServicios();
    }

    private void agregarServicio() {
        String agregarServicio = agregarServicioEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(agregarServicio)) {
            FirebaseFirestore.getInstance().collection("Servicios")
                    .document("Todos los servicios")
                    .update("Servicios", FieldValue.arrayUnion(agregarServicio))
                    .addOnSuccessListener(aVoid -> {
                        agregarServicioEditText.setText("");
                        cargarServicios(); // Llamada a cargarServicios para refrescar la lista completa
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al agregar servicio", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Ingrese un nombre de servicio", Toast.LENGTH_SHORT).show();
        }
    }



    private void cargarServicios() {
        FirebaseFirestore.getInstance().collection("Servicios")
                .document("Todos los servicios")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> nombresServicios = (List<String>) documentSnapshot.get("Servicios");
                    if (nombresServicios != null) {
                        listaServicios.clear(); // Limpiar la lista antes de cargar nuevos datos
                        listaServicios.addAll(nombresServicios);
                        servicioAdapter.notifyDataSetChanged(); // Notificar al adaptador para que actualice el RecyclerView
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar servicios", Toast.LENGTH_SHORT).show();
                });
    }

}

