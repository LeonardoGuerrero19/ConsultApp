package com.example.funcion_loginregistro;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class InicioAdminActivity extends AppCompatActivity {
    Button btnCerrarS, btn_registrarMedico, btn_AgregarServicio;
    Intent i;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_admin);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btn_registrarMedico = findViewById(R.id.btn_registrarMedico);
        btn_AgregarServicio = findViewById(R.id.btn_AgregarServicio);
        btnCerrarS = findViewById(R.id.btn_cerrarS);

        btn_AgregarServicio.setOnClickListener(view -> {
            Intent intent = new Intent(InicioAdminActivity.this, AgregarServicioActivity.class);
            startActivity(intent);
            finish();
        });

        btn_registrarMedico.setOnClickListener(view -> {
            Intent intent = new Intent(InicioAdminActivity.this, RegistrarMedicoActivity.class);
            startActivity(intent);
            finish();
        });
        btnCerrarS.setOnClickListener(view -> {
            mAuth.signOut();
            Intent intent = new Intent(InicioAdminActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Cierra la actividad actual
        });
    }
}