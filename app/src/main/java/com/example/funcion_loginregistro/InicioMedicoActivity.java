package com.example.funcion_loginregistro;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class InicioMedicoActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView tvCitas;
    private Button btn_cerrarS, btn_horarios_medico;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_medico);

        // Iniciar Firestore y Firebase Auth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Referencias a los elementos de la vista
        tvCitas = findViewById(R.id.tvCitas);
        btn_cerrarS = findViewById(R.id.btn_cerrarS);
        btn_horarios_medico = findViewById(R.id.btn_horarios_medico);

        // Configurar el botón para cerrar sesión
        btn_cerrarS.setOnClickListener(view -> {
            mAuth.signOut();
            Intent intent = new Intent(InicioMedicoActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Cierra la actividad actual
        });

        // Configurar el botón para redirigir a MedicoActivity (Horarios del médico)
        btn_horarios_medico.setOnClickListener(v -> {
            Intent intent = new Intent(InicioMedicoActivity.this, AgregarHorarioMedicoActivity.class);
            startActivity(intent);
        });

        // Cargar citas del médico
        cargarCitasMedico();
    }

    private void cargarCitasMedico() {
        String uid = mAuth.getCurrentUser().getUid(); // Obtener el UID del médico logueado

        // Buscar el documento del médico actual
        db.collection("user").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Obtener la especialización del médico
                    String especializacion = document.getString("especializacion"); // Asegúrate de que el nombre del campo es correcto

                    // Consulta para obtener las citas donde el campo 'especialización' sea igual a la especialización del médico logueado
                    db.collection("citas")
                            .whereEqualTo("especializacion", especializacion) // Cambiar de 'servicio' a 'especializacion'
                            .get()
                            .addOnCompleteListener(taskCitas -> {
                                if (taskCitas.isSuccessful()) {
                                    List<DocumentSnapshot> citasList = taskCitas.getResult().getDocuments();

                                    if (!citasList.isEmpty()) {
                                        StringBuilder citasInfo = new StringBuilder();
                                        for (DocumentSnapshot cita : citasList) {
                                            String servicio = cita.getString("servicio"); // Asegúrate de que este campo exista también
                                            String fecha = cita.getString("fecha");
                                            String horario = cita.getString("horario");

                                            citasInfo.append("Servicio: ").append(servicio)
                                                    .append("\nFecha: ").append(fecha)
                                                    .append("\nHorario: ").append(horario)
                                                    .append("\n\n");
                                        }

                                        // Mostrar citas en el TextView
                                        tvCitas.setText(citasInfo.toString());
                                    } else {
                                        tvCitas.setText("No tienes citas programadas.");
                                    }
                                } else {
                                    Toast.makeText(InicioMedicoActivity.this, "Error al cargar las citas", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(InicioMedicoActivity.this, "No se encontró al médico", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(InicioMedicoActivity.this, "Error al cargar la información del médico", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
