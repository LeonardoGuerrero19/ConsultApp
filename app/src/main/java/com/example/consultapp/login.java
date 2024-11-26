package com.example.consultapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class login extends AppCompatActivity {

    private EditText correo, contrasena;
    private Button btnIniciarSesion;
    private TextView txtRegistrate;
    private ImageButton togglePasswordVisibility;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    private boolean isPasswordVisible = false; // Estado de visibilidad de la contraseña

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
        togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility);

        // Configurar el botón de alternar visibilidad de contraseña
        togglePasswordVisibility.setOnClickListener(v -> togglePasswordVisibility());

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

    // Alternar entre mostrar y ocultar contraseña
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Ocultar contraseña
            contrasena.setTransformationMethod(PasswordTransformationMethod.getInstance());
            togglePasswordVisibility.setImageResource(R.drawable.baseline_check_box_outline_blank_24);
        } else {
            // Mostrar contraseña
            contrasena.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            togglePasswordVisibility.setImageResource(R.drawable.baseline_check_box_24);
        }
        // Mantener el cursor al final del texto
        contrasena.setSelection(contrasena.getText().length());
        isPasswordVisible = !isPasswordVisible; // Cambiar el estado
    }

    private void loginUser(String correoUser, String contraUser) {
        auth.signInWithEmailAndPassword(correoUser, contraUser).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    if (user.isEmailVerified()) {
                        buscarEnMedicos(user.getUid());
                    } else {
                        Toast.makeText(login.this, "Por favor, verifica tu correo electrónico", Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                Toast.makeText(login.this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserRole(String uid) {
        databaseReference.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String rol = snapshot.child("rol").getValue(String.class);
                    redirigirPorRol(rol != null ? rol : "usuario");
                } else {
                    redirigirPorRol("usuario");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                redirigirPorRol("usuario");
            }
        });
    }

    private void buscarEnMedicos(String uid) {
        databaseReference.child("Medicos").child(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DataSnapshot snapshot = task.getResult();
                String rol = snapshot.child("rol").getValue(String.class);
                if (rol != null) {
                    redirigirPorRol(rol);
                } else {
                    Toast.makeText(login.this, "El rol del médico no está definido en 'Medicos'.", Toast.LENGTH_SHORT).show();
                    redirigirPorRol("usuario");
                }
            } else {
                checkUserRole(uid);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(login.this, "Error al verificar usuario en 'Medicos'", Toast.LENGTH_SHORT).show();
            checkUserRole(uid);
        });
    }

    private void redirigirPorRol(String rol) {
        Intent intent;
        if ("administrador".equals(rol)) {
            intent = new Intent(login.this, AdministradorActivity.class);
        } else if ("medico".equals(rol)) {
            intent = new Intent(login.this, MedicoActivity.class);
        } else {
            intent = new Intent(login.this, UserActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
