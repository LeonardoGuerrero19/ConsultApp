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

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;

public class login extends AppCompatActivity {

    private EditText correo, contrasena;
    private Button btnIniciarSesion;
    private TextView txtRegistrate;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance(); // Para autenticación
        db = FirebaseFirestore.getInstance(); // Para base de datos

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

        // Evento de clic para registrar
        txtRegistrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(login.this, registro.class));
            }
        });
    }

    private void loginUser(String correoUser, String contraUser) {
        auth.signInWithEmailAndPassword(correoUser, contraUser).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null && !user.isEmailVerified()) { // Verificar si el correo está verificado
                        auth.signOut();
                        Toast.makeText(login.this, "Por favor, verifica tu correo electrónico.", Toast.LENGTH_SHORT).show();
                    } else {
                        checkUserRole(user.getUid()); // Verificar el rol
                    }
                } else {
                    Toast.makeText(login.this, "Error al iniciar sesión 1", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(login.this, "Error al iniciar sesión 2", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserRole(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Buscar primero en la colección "user"
        db.collection("user").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Usuario encontrado en "user"
                        redirigirPorRol(document);
                    } else {
                        // Intentar buscar en la colección "medicos"
                        db.collection("medicos").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        // Usuario encontrado en "medicos"
                                        redirigirPorRol(document);
                                    } else {
                                        Toast.makeText(login.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(login.this, "Error al buscar en 'medicos'", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                } else {
                    Toast.makeText(login.this, "Error al buscar en 'user'", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Método para redirigir basado en el rol
    private void redirigirPorRol(DocumentSnapshot document) {
        String rol = document.getString("rol");
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
            // Si el usuario esta logueado verificar y redirigir dependiendo su rol.
            checkUserRole(user.getUid());
        }
    }
}
