package com.example.consultapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.consultapp.ui.home.HomeFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.AuthResult;

public class login extends AppCompatActivity {

    TextView textRegistrate;
    EditText correo, contrasena;
    Button btnLogin;
    FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 9001; // Código de solicitud para Google Sign-In
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        // Inicialización de Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Asegúrate de tener este ID en strings.xml
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        correo = findViewById(R.id.correo);
        contrasena = findViewById(R.id.contrasena);
        btnLogin = findViewById(R.id.btnIniciarSesion);
        textRegistrate = findViewById(R.id.txtRegistrate);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String correoUser = correo.getText().toString().trim();
                String contraUser = contrasena.getText().toString().trim();

                if (correoUser.isEmpty() || contraUser.isEmpty()) {
                    Toast.makeText(login.this, "Ingrese todos los datos", Toast.LENGTH_SHORT).show();
                } else {
                    loginUser(correoUser, contraUser);
                }
            }
        });

        textRegistrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(login.this, registro.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser(String correoUser, String contraUser) {
        mAuth.signInWithEmailAndPassword(correoUser, contraUser).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null && !user.isEmailVerified()) { // Verificar si el correo está verificado
                        mAuth.signOut(); // Cerrar sesión si el correo no está verificado
                        Toast.makeText(login.this, "Por favor, verifica tu correo electrónico.", Toast.LENGTH_SHORT).show();
                    } else {
                        checkUserRole(user.getUid()); // Verificar el rol del usuario
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
        db.collection("user").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String rol = document.getString("rol"); // Asegúrate de que el campo esté correctamente escrito
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
                            // Redirigir a la actividad principal para otros roles (usuarios)
                            startActivity(new Intent(login.this, IniciopActivity.class));
                            finish();
                        }
                    } else {
                        Toast.makeText(login.this, "Error: No se encontró el documento de rol", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(login.this, "Error al obtener los datos del usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Si el usuario está autenticado, verificar su rol en Firestore antes de redirigir
            checkUserRole(user.getUid());
        }
    }
}
