package com.example.proyecto_consultapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class activity_login extends AppCompatActivity {

    EditText correo, contrasena;
    Button btn_loguear, btn_registrar, btn_loguin_google;
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

        correo = findViewById(R.id.Correo);
        contrasena = findViewById(R.id.Contraseña);
        btn_loguear = findViewById(R.id.btnIniciarSesion);
        btn_registrar= findViewById(R.id.btn_registrar);
        btn_loguin_google = findViewById(R.id.btn_loguin_google);

        btn_loguear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String correoUser = correo.getText().toString().trim();
                String contraUser = contrasena.getText().toString().trim();

                if (correoUser.isEmpty() && contraUser.isEmpty()){
                    Toast.makeText(activity_login.this, "Ingrese todos los datos", Toast.LENGTH_SHORT).show();

                } else {
                    loginUser(correoUser, contraUser);
                }
            }
        });
        btn_registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Iniciar la actividad de registro
                startActivity(new Intent(activity_login.this, activity_registro.class));
            }
        });

        btn_loguin_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
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
                        Toast.makeText(activity_login.this, "Por favor, verifica tu correo electrónico.", Toast.LENGTH_SHORT).show();
                    } else {
                        checkUserRole(user.getUid()); // Verificar el rol del usuario
                    }
                } else {
                    Toast.makeText(activity_login.this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(activity_login.this, "Error al iniciar sesion", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void checkUserRole(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String rol = document.getString("rol"); // Asegúrate de que el campo esté correctamente escrito
                        if ("administrador".equals(rol)) {
                            // Redirigir a AdminActivity
                            Intent intent = new Intent(activity_login.this, activity_vistaAdmin.class);
                            startActivity(intent);
                            finish();
                        } else if ("medico".equals(rol)) {
                            // Redirigir a MedicoActivity
                            Intent intent = new Intent(activity_login.this, activity_vistaMedico.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Redirigir a la actividad principal para otros roles (usuarios)
                            startActivity(new Intent(activity_login.this, activity_vistas.class));
                            finish();
                        }
                    } else {
                        Toast.makeText(activity_login.this, "Error: No se encontró el documento de rol", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(activity_login.this, "Error al obtener los datos del usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Si el usuario está autenticado, verificar su rol en Firestore antes de redirigir
            checkUserRole(user.getUid());
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Resultados de Google Sign-In
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            Toast.makeText(activity_login.this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Inicio de sesión exitoso
                            FirebaseUser user = mAuth.getCurrentUser();
                            finish();
                            startActivity(new Intent(activity_login.this, activity_vistas.class));
                            Toast.makeText(activity_login.this, "Bienvenido", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(activity_login.this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}