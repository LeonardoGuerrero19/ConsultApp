package com.example.funcion_loginregistro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class IniciopActivity extends AppCompatActivity {
    Button btn_cerrarS;
    TextView texto;
    FirebaseFirestore mFirestore;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        btn_cerrarS = findViewById(R.id.btn_cerrarS);
        texto = findViewById(R.id.texto);

        // Obtener el usuario actual
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail(); // Obtener el correo del usuario
            String nombreUsuario = email != null ? email.split("@")[0] : "Usuario"; // Extraer el nombre del correo

            texto.setText("Hola, bienvenido " + nombreUsuario); // Mostrar el saludo
        } else {
            Toast.makeText(this, "No hay usuario autenticado.", Toast.LENGTH_SHORT).show();
        }

        btn_cerrarS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                finish();
                startActivity(new Intent(IniciopActivity.this, LoginActivity.class));
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && !user.isEmailVerified()) {
            // El usuario no ha verificado su correo
            mAuth.signOut(); // Cerrar sesión
            startActivity(new Intent(IniciopActivity.this, LoginActivity.class)); // Redirigir al login
            finish(); // Finaliza la actividad actual
        } else if (user != null) {
            // Mostrar el saludo
            String email = user.getEmail();
            texto.setText("Hola, bienvenido " + email);
        }
    }

}
