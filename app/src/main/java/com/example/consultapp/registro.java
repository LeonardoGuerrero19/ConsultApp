package com.example.consultapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class registro extends AppCompatActivity {

    EditText nombre, correo, contrasena;
    Button btnRegistro;
    FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    RequestQueue requestQueue;
    TextView textIniciaSesion, textTerminosCondiciones;
    CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

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

                if (nombreUser.isEmpty() || correoUser.isEmpty() || contraUser.isEmpty()) {
                    Snackbar.make(view, "Introduzca correctamente los datos", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (contraUser.length() < 8) {
                    Snackbar.make(view, "La contraseña debe tener al menos 8 caracteres.", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (!checkBox.isChecked()) {
                    Snackbar.make(view, "Debe aceptar los términos y condiciones.", Snackbar.LENGTH_SHORT).show();
                    return;
                }

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

                            if (estado.equals("valid") || estado.equals("webmail")) {
                                registerUser(nombreUser, correoUser, contraUser, view);
                            } else {
                                Snackbar.make(view, "El correo no existe o no es válido.", Snackbar.LENGTH_SHORT).show();
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

    private boolean containsBadWords(String input) {
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
        return false;
    }

    private void registerUser(String nombreUser, String correoUser, String contraUser, View view) {
        String passwordValidationMessage = validatePassword(contraUser);
        if (!passwordValidationMessage.isEmpty()) {
            Snackbar.make(view, passwordValidationMessage, Snackbar.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(correoUser, contraUser)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Envía el correo de verificación
                                sendVerificationEmail(user, view);

                                // Genera y guarda el número de cuenta
                                saveUserToRealtimeDB(user.getUid(), nombreUser, correoUser, view);

                                // Envía el número de cuenta por correo adicional
                                String numeroCuenta = generarNumeroCuenta();
                                sendAccountNumberEmail(correoUser, numeroCuenta);

                                // Cierra sesión automáticamente
                                mAuth.signOut();

                                // Redirige al usuario a la pantalla de login
                                startActivity(new Intent(registro.this, login.class));
                                finish();
                            }
                        } else {
                            Exception exception = task.getException();
                            if (exception instanceof FirebaseAuthUserCollisionException) {
                                Snackbar.make(view, "Este correo ya está registrado.", Snackbar.LENGTH_SHORT).show();
                            } else {
                                Snackbar.make(view, "Error al registrar: " + exception.getMessage(), Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Snackbar.make(view, "Error al registrar: " + e.getMessage(), Snackbar.LENGTH_SHORT).show());
    }

    private void sendVerificationEmail(FirebaseUser user, View view) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Snackbar.make(view, "Registro exitoso. Verifica tu correo para activar la cuenta.", Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(view, "Error al enviar el correo de verificación.", Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendAccountNumberEmail(String email, String accountNumber) {
        new Thread(() -> {
            try {
                Log.d("Email", "Preparando para enviar correo a: " + email);

                String host = "smtp.gmail.com"; // Servidor SMTP de Gmail
                String user = "aconsultapp@gmail.com"; // Tu correo Gmail
                String password = "mlqn jnkz drha ixqv"; // Contraseña de aplicación

                // Configuración de las propiedades
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", host);
                props.put("mail.smtp.port", "587");

                Log.d("Email", "Propiedades del correo configuradas.");

                // Sesión de correo
                Session session = Session.getInstance(props,
                        new javax.mail.Authenticator() {
                            @Override
                            protected PasswordAuthentication getPasswordAuthentication() {
                                Log.d("Email", "Autenticando con el correo: " + user);
                                return new PasswordAuthentication(user, password);
                            }
                        });

                // Construcción del mensaje
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(user));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
                message.setSubject("Tu número de cuenta");

                // Contenido HTML del correo
                String htmlContent = "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "    <style>" +
                        "        body { font-family: Arial, sans-serif; background-color: #f4f4f9; margin: 0; padding: 0; }" +
                        "        .container { max-width: 600px; margin: 20px auto; background: white; padding: 20px; border-radius: 10px; box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1); }" +
                        "        .header { background-color: #00d4aa; color: white; padding: 10px 20px; text-align: center; border-radius: 10px 10px 0 0; }" +
                        "        .content { padding: 20px; font-size: 16px; line-height: 1.5; color: #333; }" +
                        "        .footer { text-align: center; padding: 10px; font-size: 12px; color: #777; border-top: 1px solid #eee; }" +
                        "        .highlight { color: #00d4aa; font-size: 24px; font-weight: bold; text-align: center; margin: 20px 0; }" +
                        "    </style>" +
                        "</head>" +
                        "<body>" +
                        "    <div class='container'>" +
                        "        <div class='header'>" +
                        "            <h1>¡Bienvenido a ConsultApp!</h1>" +
                        "        </div>" +
                        "        <div class='content'>" +
                        "            <p>Hola,</p>" +
                        "            <p>Gracias por registrarte en nuestra plataforma. Tu cuenta ha sido creada exitosamente.</p>" +
                        "            <p><strong>Tu número de cuenta es:</strong></p>" +
                        "            <h2 class='highlight'>" + accountNumber + "</h2>" +
                        "            <p>Puedes utilizar este número para acceder a nuestras funcionalidades.</p>" +
                        "        </div>" +
                        "        <div class='footer'>" +
                        "            <p>&copy; 2024 ConsultApp. Todos los derechos reservados.</p>" +
                        "        </div>" +
                        "    </div>" +
                        "</body>" +
                        "</html>";

                // Establecer el contenido HTML
                message.setContent(htmlContent, "text/html; charset=utf-8");

                Log.d("Email", "Correo construido con HTML.");

                // Envío del correo
                Transport.send(message);

                Log.d("Email", "Correo enviado exitosamente a: " + email);
            } catch (Exception e) {
                Log.e("EmailError", "Error al enviar correo", e);
            }
        }).start();
    }



    private String generarNumeroCuenta() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int randomNum = (int) (Math.random() * 9000) + 1000; // Genera un número aleatorio de 4 dígitos
        return year + String.valueOf(randomNum); // Combina el año con el número aleatorio
    }

    private void saveUserToRealtimeDB(String userId, String nombreUser, String correoUser, View view) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int randomNum = (int) (Math.random() * 9000) + 1000;
        String numeroCuenta = year + String.valueOf(randomNum);

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", userId);
        userMap.put("nombre", nombreUser);
        userMap.put("correo", correoUser);
        userMap.put("rol", "usuario");
        userMap.put("numeroCuenta", numeroCuenta);

        databaseReference.child("users").child(userId).setValue(userMap)
                .addOnSuccessListener(aVoid -> {
                    Snackbar.make(view, "Usuario registrado correctamente.", Snackbar.LENGTH_SHORT).show();
                    startActivity(new Intent(registro.this, login.class));
                })
                .addOnFailureListener(e -> Snackbar.make(view, "Error al guardar los datos en Realtime Database: " + e.getMessage(), Snackbar.LENGTH_SHORT).show());
    }

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
        return "";
    }
}
