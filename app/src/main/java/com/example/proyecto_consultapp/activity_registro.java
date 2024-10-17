package com.example.proyecto_consultapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class activity_registro extends AppCompatActivity {
    EditText nombre, correo, contrasena;
    Button btn_registro, btn_iniciarsesion;
    FirebaseFirestore mFirestore;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();


        nombre = findViewById(R.id.nombre);
        correo = findViewById(R.id.correo);
        contrasena = findViewById(R.id.contrasena);
        btn_registro = findViewById(R.id.btnRegistrar);
        btn_iniciarsesion = findViewById(R.id.btnIniciarSesion);

        btn_registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nombreUser = nombre.getText().toString().trim();
                String correoUser = correo.getText().toString().trim();
                String contraUser = contrasena.getText().toString().trim();


                if (nombreUser.isEmpty() && correoUser.isEmpty() && contraUser.isEmpty()){
                    Toast.makeText(activity_registro.this, "Introduzca correctamente los datos ", Toast.LENGTH_SHORT).show();
                } else {
                    RegisterUser(nombreUser, correoUser, contraUser);
                }

            }
        });

        btn_iniciarsesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Inicia la actividad activity_login.java
                Intent intent = new Intent(activity_registro.this, activity_login.class);
                startActivity(intent);
            }
        });


    }

    private void RegisterUser(String nombreUser, String correoUser, String contraUser) {
        mAuth.createUserWithEmailAndPassword(correoUser, contraUser).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) { // Verificar que el registro fue exitoso
                    String id = mAuth.getCurrentUser().getUid();
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", id);
                    map.put("nombre", nombreUser);
                    map.put("correo", correoUser);
                    map.put("contrasena", contraUser);

                    mFirestore.collection("user").document(id).set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(activity_registro.this, "Error al guardar los datos", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(activity_registro.this, "Registro guardado con éxito.", Toast.LENGTH_SHORT).show();

                            // Redirigir al activity_login
                            Intent intent = new Intent(activity_registro.this, activity_login.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                } else {
                    Toast.makeText(activity_registro.this, "Error al registrar: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(activity_registro.this, "Error al registrar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}