package com.example.proyecto_consultapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class activity_registro extends AppCompatActivity {
    EditText nombre, correo, contrasena;
    Button btn_registro, btn_iniciarsesion;
    FirebaseFirestore mFirestore;
    FirebaseAuth mAuth;
    RequestQueue requestQueue;


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
        btn_registro = findViewById(R.id.btnRegistrar);
        btn_iniciarsesion = findViewById(R.id.btnIniciarSesion);

        btn_registro.setOnClickListener(new View.OnClickListener() {
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
                                RegisterUser(nombreUser, correoUser, contraUser); // Registro si es válido
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

        btn_iniciarsesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Inicia la actividad activity_login.java
                Intent intent = new Intent(activity_registro.this, activity_login.class);
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
    private void RegisterUser(String nombreUser, String correoUser, String contraUser) {
        mAuth.createUserWithEmailAndPassword(correoUser, contraUser).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        // Enviar correo de verificación
                        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Snackbar.make(findViewById(R.id.btnRegistrar), "Registro exitoso. Verifique su correo para activar la cuenta.", Snackbar.LENGTH_SHORT).show();
                                } else {
                                    Snackbar.make(findViewById(R.id.btnRegistrar), "Error al enviar el correo de verificación.", Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                    // Guardar los datos del usuario en Firestore
                    String id = user.getUid();
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", id);
                    map.put("nombre", nombreUser);
                    map.put("correo", correoUser);
                    map.put("rol", "usuario"); // Asignar rol de "usuario"

                    mFirestore.collection("user").document(id).set(map)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    // Redirigir a LoginActivity después del registro exitoso
                                    Intent intent = new Intent(activity_registro.this, activity_login.class);
                                    startActivity(intent);
                                    finish(); // Cierra la actividad actual
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Snackbar.make(findViewById(R.id.btnRegistrar), "Error al guardar", Snackbar.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Snackbar.make(findViewById(R.id.btnRegistrar), "Error al registrar: " + task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(findViewById(R.id.btnRegistrar), "Error al registrar: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        });
    }


}