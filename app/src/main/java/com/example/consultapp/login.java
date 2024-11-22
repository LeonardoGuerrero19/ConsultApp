package com.example.consultapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class login extends AppCompatActivity {

    private EditText correo, contrasena;
    private Button btnIniciarSesion;
    private TextView txtRegistrate;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        correo = findViewById(R.id.correo);
        contrasena = findViewById(R.id.contrasena);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        txtRegistrate = findViewById(R.id.txtRegistrate);

        btnIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String correoUser = correo.getText().toString().trim();
                String contraUser = contrasena.getText().toString().trim();

                if (correoUser.isEmpty() || contraUser.isEmpty()) {
                    Toast.makeText(login.this, "Ingrese todos los datos", Toast.LENGTH_SHORT).show();
                } else {
                    loginUser(correoUser, contraUser);
                }
            }
        });

        txtRegistrate.setOnClickListener(v -> startActivity(new Intent(login.this, registro.class)));
    }

    private void loginUser(String correoUser, String contraUser) {
        auth.signInWithEmailAndPassword(correoUser, contraUser).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null && !user.isEmailVerified()) {
                    auth.signOut();
                    Toast.makeText(login.this, "Por favor, verifica tu correo electrónico.", Toast.LENGTH_SHORT).show();
                } else {
                    checkUserRole(user.getUid());
                }
            } else {
                Toast.makeText(login.this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(login.this, "Error de autenticación", Toast.LENGTH_SHORT).show());
    }

    private void checkUserRole(String uid) {
        databaseReference.child("users").child(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DataSnapshot snapshot = task.getResult();
                String rol = snapshot.child("rol").getValue(String.class); // Extrae el valor correctamente
                if (rol != null) {
                    redirigirPorRol(rol); // Pasa el rol como String
                } else {
                    Toast.makeText(login.this, "El rol del usuario no está definido.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(login.this, "Usuario no encontrado en 'users'", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(login.this, "Error al verificar usuario", Toast.LENGTH_SHORT).show();
        });
    }



    private void redirigirPorRol(String rol) {
        if ("administrador".equals(rol)) {
            // Redirigir a AdminActivity
            Intent intent = new Intent(login.this, AdminActivity.class);
            startActivity(intent);
            finish();
        } else if ("medico".equals(rol)) {
            // Redirigir a MedicoActivity
            Intent intent = new Intent(login.this, MedicoActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Redirigir a una vista general para usuarios
            Intent intent = new Intent(login.this, UserActivity.class);
            startActivity(intent);
            finish();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            checkUserRole(user.getUid());
        }
    }
}
