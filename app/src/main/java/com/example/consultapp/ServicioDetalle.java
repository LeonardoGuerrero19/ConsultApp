package com.example.consultapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ServicioDetalle extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servicio_detalle);

        // Obtener el nombre del servicio desde el Intent
        String nombreServicio = getIntent().getStringExtra("NOMBRE_SERVICIO");

        // Mostrar el nombre del servicio en un TextView
        TextView servicioTextView = findViewById(R.id.servicioTextView);
        servicioTextView.setText(nombreServicio);

    }
}