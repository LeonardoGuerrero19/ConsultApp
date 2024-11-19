package com.example.consultapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class registro extends AppCompatActivity {

    EditText nombre, correo, contrasena;
    Button btnRegistro;
    FirebaseFirestore mFirestore;
    FirebaseAuth mAuth;
    RequestQueue requestQueue;
    TextView textIniciaSesion, textTerminosCondiciones;
    CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        requestQueue = Volley.newRequestQueue(this);

        nombre = findViewById(R.id.nombre);
        correo = findViewById(R.id.correo);
        contrasena = findViewById(R.id.contrasena);
        btnRegistro = findViewById(R.id.btnRegistrar);

        textIniciaSesion = findViewById(R.id.txtIniciaSesion);
        textTerminosCondiciones = findViewById(R.id.txtTerminosCondiciones);
        checkBox = findViewById(R.id.checkbox);

        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nombreUser = nombre.getText().toString().trim();
                String correoUser = correo.getText().toString().trim();
                String contraUser = contrasena.getText().toString().trim();

                // Validación de campos vacíos
                if (nombreUser.isEmpty() || correoUser.isEmpty() || contraUser.isEmpty()) {
                    Snackbar.make(view, "Introduzca correctamente los datos", Snackbar.LENGTH_SHORT).show();
                    return; // Detener el proceso si algún campo está vacío
                }

                // Validar que la contraseña tenga al menos 8 caracteres
                if (contraUser.length() < 8) {
                    Snackbar.make(view, "La contraseña debe tener al menos 8 caracteres.", Snackbar.LENGTH_SHORT).show();
                    return; // Detener el proceso de registro
                }

                // Verificación del CheckBox
                if (!checkBox.isChecked()) {
                    Snackbar.make(view, "Debe aceptar los términos y condiciones.", Snackbar.LENGTH_SHORT).show();
                    return; // Detener el proceso si no está marcado
                }

                // Verificar malas palabras usando la lista manual
                if (containsBadWords(nombreUser)) {
                    Snackbar.make(view, "El nombre de usuario contiene palabras inapropiadas.", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                String apiKey = "e8bc47ef92c2a13bcb73233c68e0fc8a7e23092a";
                String url = "https://api.hunter.io/v2/email-verifier?email=" + correoUser + "&api_key=" + apiKey;

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject data = response.getJSONObject("data");
                            String estado = data.getString("status");

                            // Verificar si el correo es 'valid' o 'webmail' (esto indica que es válido)
                            if (estado.equals("valid") || estado.equals("webmail")) {
                                registerUser(nombreUser, correoUser, contraUser, view); // Registro si es válido
                            } else {
                                Snackbar.make(view, "El correo no existe o no es valido.", Snackbar.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Snackbar.make(view, "Error al verificar el correo.", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Snackbar.make(view, "Error en la API de verificación.", Snackbar.LENGTH_SHORT).show();
                    }
                });

                requestQueue.add(jsonObjectRequest);
            }
        });

        textIniciaSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(registro.this, login.class);
                startActivity(intent);
            }
        });

        textTerminosCondiciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(registro.this, politicas.class);
                startActivity(intent);
            }
        });
    }

    // Método para verificar malas palabras usando una lista manual
    private boolean containsBadWords(String input) {
        // Lista de malas palabras en español e inglés
        String[] palabrasBan = {
                "mierda", "cabrón", "pendejo", "puto", "puta", "imbécil", "idiota",
                "tarado", "estúpido", "culo", "tetas", "verga", "pito", "pene",
                "chichi", "nalgas", "negro", "negrata", "chino", "judío", "nazi",
                "maricón", "joto", "follar", "cojer", "sexo", "porno", "cachonda",
                "orgasmo", "marihuana", "cocaína", "droga", "porro", "crack",
                "heroína", "gay", "lesbiana", "transexual", "travesti", "dios",
                "cristo", "infiel", "hereje", "anticristo", "fuck", "bitch",
                "bastard", "shit", "asshole", "dick", "cunt", "@"
        };

        for (String palabraBan : palabrasBan) {
            if (input.toLowerCase().contains(palabraBan)) {
                return true;
            }
        }
        return false; // Si no contiene ninguna mala palabra
    }

    // Método de registro de usuario en Firebase
    private void registerUser(String nombreUser, String correoUser, String contraUser, View view) {
        // Validar la contraseña primero
        String passwordValidationMessage = validatePassword(contraUser);
        if (!passwordValidationMessage.isEmpty()) {
            Snackbar.make(view, passwordValidationMessage, Snackbar.LENGTH_SHORT).show();
            return; // Salir si la contraseña no cumple los requisitos
        }

        // Registrar el usuario con Firebase Authentication
        mAuth.createUserWithEmailAndPassword(correoUser, contraUser)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registro exitoso
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Enviar correo de verificación
                                sendVerificationEmail(user, view);

                                // Guardar en Firestore
                                saveUserToFirestore(user.getUid(), nombreUser, correoUser, view);
                            }
                        } else {
                            // Capturar y manejar el error de colisión de correo (Correo ya registrado)
                            Exception exception = task.getException();
                            if (exception != null) {
                                if (exception instanceof FirebaseAuthUserCollisionException) {
                                    Snackbar.make(view, "Este correo ya está registrado. Intenta con otro o recupéralo.", Snackbar.LENGTH_SHORT).show();
                                } else {
                                    Snackbar.make(view, "Error al registrar: Correo ya registrado ", Snackbar.LENGTH_SHORT).show();
                                }
                            } else {
                                Snackbar.make(view, "Error desconocido al registrar.", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Capturar fallos generales
                        Snackbar.make(view, "Error al registrar: Error general" , Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    // Método para enviar correo de verificación
    private void sendVerificationEmail(FirebaseUser user, View view) {
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Snackbar.make(view, "Registro exitoso. Verifica tu correo para activar la cuenta.", Snackbar.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(view, "Error al enviar el correo de verificación.", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Método para guardar al usuario en Firestore
    private void saveUserToFirestore(String userId, String nombreUser, String correoUser, View view) {
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

        // Crear número de cuenta
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int randomNum = (int) (Math.random() * 9000) + 1000;
        String numeroCuenta = year + String.valueOf(randomNum);

        // Crear el mapa de datos del usuario
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", userId);
        userMap.put("nombre", nombreUser);
        userMap.put("correo", correoUser);
        userMap.put("rol", "usuario");
        userMap.put("numeroCuenta", numeroCuenta);

        mFirestore.collection("user").document(userId).set(userMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Snackbar.make(view, "Usuario registrado correctamente.", Snackbar.LENGTH_SHORT).show();
                        // Redirige a la pantalla de login
                        startActivity(new Intent(registro.this, login.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(view, "Error al guardar los datos en Firestore: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    // Método para validar la contraseña
    private String validatePassword(String password) {
        if (password.length() < 6) {
            return "La contraseña debe tener al menos 6 caracteres.";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "La contraseña debe contener al menos una letra mayúscula.";
        }
        if (!password.matches(".*[a-z].*")) {
            return "La contraseña debe contener al menos una letra minúscula.";
        }
        if (!password.matches(".*\\d.*")) {
            return "La contraseña debe contener al menos un número.";
        }
        return ""; // Si pasa todas las validaciones
    }
}