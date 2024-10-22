package com.example.proyecto_consultapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa Firebase
        FirebaseApp.initializeApp(this);

        // Referencia al botón
        Button btnInicio = findViewById(R.id.btnInicio);

        // Configurar el listener para el botón
        btnInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Iniciar la nueva actividad
                Intent intent = new Intent(MainActivity.this, activity_login.class);
                startActivity(intent);
            }
        });
    }
}