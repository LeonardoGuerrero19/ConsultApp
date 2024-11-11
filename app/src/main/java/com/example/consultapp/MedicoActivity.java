package com.example.consultapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class MedicoActivity extends AppCompatActivity {

    private Button btn_cerrarS;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medico);
        mAuth = FirebaseAuth.getInstance();

        btn_cerrarS = findViewById(R.id.btn_cerrarS);



        // Configurar el botón para cerrar sesión
        btn_cerrarS.setOnClickListener(view -> {
            mAuth.signOut();
            Intent intent = new Intent(MedicoActivity.this, login.class);
            startActivity(intent);
            finish(); // Cierra la actividad actual
        });
    }
}