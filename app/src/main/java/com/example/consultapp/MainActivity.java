package com.example.consultapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressBar);

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Crear un ExecutorService para tareas en segundo plano
        executorService = Executors.newSingleThreadExecutor();

        // Mostrar la pantalla de carga y verificar el estado del usuario después de un retraso
        new Handler().postDelayed(() -> verificarUsuario(), 1000); // Un retraso de 1 segundo para simular carga
    }

    // Verificar si el usuario está autenticado y redirigir según el rol
    private void verificarUsuario() {
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            if (user.isEmailVerified()) {
                // Si el usuario está verificado, verificar el rol en segundo plano
                verificarRolEnSegundoPlano(user.getUid());
            } else {
                // Si no está verificado, redirigir a login
                redirigirALogin();
            }
        } else {
            // Si no está autenticado, redirigir a login
            redirigirALogin();
        }
    }

    private void verificarRolEnSegundoPlano(String uid) {
        executorService.execute(() -> {
            // Primero verificamos si el usuario está en la rama 'Medicos'
            databaseReference.child("Medicos").child(uid).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    // Si el usuario está en la rama 'Medicos', obtenemos su rol
                    DataSnapshot snapshot = task.getResult();
                    String rol = snapshot.child("rol").getValue(String.class);

                    if (rol != null) {
                        // Si tiene un rol, redirigir según el rol
                        runOnUiThread(() -> redirigirPorRol(rol));
                    } else {
                        // Si no tiene rol, redirigir a 'usuario' por defecto
                        runOnUiThread(() -> redirigirPorRol("usuario"));
                    }
                } else {
                    // Si el usuario no está en 'Medicos', verificar en 'users'
                    verificarEnUsers(uid);
                }
            });
        });
    }

    private void verificarEnUsers(String uid) {
        // Verificar en la rama 'users' si no está en 'Medicos'
        databaseReference.child("users").child(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                // Si el usuario existe en 'users', obtener su rol
                DataSnapshot snapshot = task.getResult();
                String rol = snapshot.child("rol").getValue(String.class);

                if (rol != null) {
                    // Redirigir según el rol
                    runOnUiThread(() -> redirigirPorRol(rol));
                } else {
                    // Si no tiene rol, redirigir a 'usuario' por defecto
                    runOnUiThread(() -> redirigirPorRol("usuario"));
                }
            } else {
                // Si no se encuentra en 'users', redirigir a 'usuario'
                runOnUiThread(() -> redirigirPorRol("usuario"));
            }
        });
    }

    private void redirigirALogin() {
        runOnUiThread(() -> {
            Intent intent = new Intent(MainActivity.this, login.class);
            startActivity(intent);
            finish();
        });
    }

    private void redirigirPorRol(String rol) {
        Intent intent;
        if ("administrador".equals(rol)) {
            intent = new Intent(MainActivity.this, AdministradorActivity.class);
        } else if ("medico".equals(rol)) {
            intent = new Intent(MainActivity.this, MedicoActivity.class);
        } else {
            intent = new Intent(MainActivity.this, UserActivity.class); // Actividad de usuario general
        }
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Apagar el ExecutorService cuando la actividad se destruya
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
