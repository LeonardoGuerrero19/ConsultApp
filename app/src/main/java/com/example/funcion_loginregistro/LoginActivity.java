package com.example.funcion_loginregistro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

public class LoginActivity extends AppCompatActivity {

    EditText correo, contrasena;
    Button btn_loguear, btn_registrar, btn_loguin_google;
    FirebaseAuth mAuth;
    FirebaseFirestore mFirestore;
    private static final int RC_SIGN_IN = 9001; // Código de solicitud para Google Sign-In
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Inicialización de Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        correo = findViewById(R.id.correo);
        contrasena = findViewById(R.id.contrasena);
        btn_loguear = findViewById(R.id.btn_login);
        btn_registrar = findViewById(R.id.btn_registrar);
        btn_loguin_google = findViewById(R.id.btn_loguin_google);

        btn_loguear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String usuarioInput = correo.getText().toString().trim();
                String contraUser = contrasena.getText().toString().trim();

                if (usuarioInput.isEmpty() || contraUser.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Ingrese todos los datos", Toast.LENGTH_SHORT).show();
                } else {
                    // Llamada a loginUser si los campos no están vacíos
                    loginUser(usuarioInput, contraUser);
                }
            }
        });

        btn_registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Iniciar la actividad de registro
                startActivity(new Intent(LoginActivity.this, RegistroActivity.class));
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
                        mAuth.signOut();
                        Toast.makeText(LoginActivity.this, "Por favor, verifica tu correo electrónico.", Toast.LENGTH_SHORT).show();
                    } else {
                        checkUserRole(user.getUid()); // Verificar el rol
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
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
                        String rol = document.getString("rol");
                        if ("administrador".equals(rol)) {
                            // Redirigir a AdminActivity
                            Intent intent = new Intent(LoginActivity.this, InicioAdminActivity.class);
                            startActivity(intent);
                            finish();
                        } else if ("medico".equals(rol)) {
                            // Redirigir a MedicoActivity
                            Intent intent = new Intent(LoginActivity.this, InicioMedicoActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            startActivity(new Intent(LoginActivity.this, IniciopActivity.class));
                            finish();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Error: No se encontró el documento de rol", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Error al obtener los datos del usuario", Toast.LENGTH_SHORT).show();
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
            // Si el usuario esta logueado verificar y redirigir dependiendo su rol.
            checkUserRole(user.getUid());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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
            Toast.makeText(LoginActivity.this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            finish();
                            startActivity(new Intent(LoginActivity.this, IniciopActivity.class));
                            Toast.makeText(LoginActivity.this, "Bienvenido", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
