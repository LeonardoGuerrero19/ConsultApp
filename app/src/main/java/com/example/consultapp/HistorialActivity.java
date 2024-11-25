package com.example.consultapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HistorialActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        // Obtener referencias a los elementos de la UI
        TextView txtNumeroCuenta = findViewById(R.id.txtNumeroCuenta);


        // Obtener los datos del intent
        Intent intent = getIntent();
        String numeroCuenta = intent.getStringExtra("numeroCuenta");

        // Establecer los valores en la UI
        txtNumeroCuenta.setText(numeroCuenta);

    }
}