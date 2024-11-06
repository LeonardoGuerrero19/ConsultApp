package com.example.funcion_loginregistro;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class IniciopActivity extends AppCompatActivity {
    Button btn_cerrarS;
    TextView texto;
    FirebaseFirestore mFirestore;
    FirebaseAuth mAuth;
    LinearLayout btnContainer, citasContainer;  // Contenedores para botones y citas

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Inicializar vistas
        btn_cerrarS = findViewById(R.id.btn_cerrarS);
        texto = findViewById(R.id.texto);
        btnContainer = findViewById(R.id.btn_container);
        citasContainer = findViewById(R.id.citas_container);  // Contenedor para las citas

        // Obtener el usuario actual
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            String nombreUsuario = email != null ? email.split("@")[0] : "Usuario";
            texto.setText("Hola, bienvenido " + nombreUsuario);
        }

        btn_cerrarS.setOnClickListener(view -> {
            mAuth.signOut();
            startActivity(new Intent(IniciopActivity.this, LoginActivity.class));
            finish();
        });

        // Cargar servicios y citas
        cargarServicios();
        cargarCitas();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarCitas(); // Cargar citas cada vez que se vuelve a esta actividad
    }

    private void cargarServicios() {
        mFirestore.collection("Servicios").document("Todos los servicios").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    List<String> nombresServicios = (List<String>) document.get("Servicios");
                    if (nombresServicios != null) {
                        for (String nombreServicio : nombresServicios) {
                            agregarBotonServicio(nombreServicio);
                        }
                    }
                }
            }
        });
    }

    private void cargarCitas() {
        String userId = mAuth.getCurrentUser().getUid();
        mFirestore.collection("citas")
                .whereEqualTo("usuario_id", userId)
                .get()
                .addOnCompleteListener(task -> {
                    // Limpiar citasContainer antes de cargar nuevas citas
                    citasContainer.removeAllViews();

                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            Log.d("IniciopActivity", "No hay citas para el usuario: " + userId);
                            Toast.makeText(IniciopActivity.this, "No tienes citas.", Toast.LENGTH_SHORT).show();
                        } else {
                            for (DocumentSnapshot document : task.getResult()) {
                                String servicio = document.getString("servicio");
                                String fecha = document.getString("fecha");
                                String horario = document.getString("horario");

                                // Log para verificar los datos obtenidos
                                Log.d("IniciopActivity", "Cita obtenida: " + servicio + ", " + fecha + ", " + horario);

                                // Mostrar la cita en la pantalla
                                mostrarCita(servicio, "No disponible", fecha, horario);
                            }
                        }
                    } else {
                        Toast.makeText(IniciopActivity.this, "Error al cargar citas.", Toast.LENGTH_SHORT).show();
                        Log.d("IniciopActivity", "Error en la carga de citas: " + task.getException());
                    }
                });
    }

    private void mostrarCita(String servicio, String nombreMedico, String fecha, String horario) {
        LinearLayout citaLayout = new LinearLayout(this);
        citaLayout.setOrientation(LinearLayout.VERTICAL);
        citaLayout.setPadding(16, 16, 16, 16);

        TextView servicioView = new TextView(this);
        servicioView.setText("Servicio: " + servicio);
        servicioView.setTextSize(18);

        TextView medicoView = new TextView(this);
        medicoView.setText("Médico: " + nombreMedico);
        medicoView.setTextSize(18);

        TextView fechaView = new TextView(this);
        fechaView.setText("Fecha: " + fecha);
        fechaView.setTextSize(18);

        TextView horarioView = new TextView(this);
        horarioView.setText("Horario: " + horario);
        horarioView.setTextSize(18);

        citaLayout.addView(servicioView);
        citaLayout.addView(medicoView);
        citaLayout.addView(fechaView);
        citaLayout.addView(horarioView);

        citasContainer.addView(citaLayout);
    }

    private void agregarBotonServicio(String nombreServicio) {
        Button boton = new Button(this);
        boton.setText(nombreServicio);
        boton.setOnClickListener(view -> {
            Intent intent = new Intent(IniciopActivity.this, CrearCitaActivity.class);
            intent.putExtra("servicio", nombreServicio);
            startActivity(intent);
        });

        btnContainer.addView(boton);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null && !mAuth.getCurrentUser().isEmailVerified()) {
            mAuth.signOut();
            startActivity(new Intent(IniciopActivity.this, LoginActivity.class));
            finish();
        }
    }
}
