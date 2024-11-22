package com.example.consultapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class InformeMedicoActivity extends AppCompatActivity {

    private static final String TAG = "InformeMedicoActivity";
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informe_medico);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Obtener datos del Intent
        String usuarioId = getIntent().getStringExtra("usuarioId");
        String numeroCuenta = getIntent().getStringExtra("numeroCuenta");
        String citaId = getIntent().getStringExtra("citaId");
        String nombreDoctor = getIntent().getStringExtra("nombreDoctor");  // Asumimos que el nombre del doctor viene en el Intent
        String nombrePaciente = getIntent().getStringExtra("nombre");


        // Referencia a los campos
        TextView tvCuenta = findViewById(R.id.tvCuenta);
        EditText etPeso = findViewById(R.id.peso);
        EditText etAltura = findViewById(R.id.estatura);
        EditText etMotivo = findViewById(R.id.etMotivoConsulta);
        EditText etPadecimiento = findViewById(R.id.etPrincipioEvolucion);
        EditText etMedicamento = findViewById(R.id.etMedicamentosRecetados);
        Button btnGuardar = findViewById(R.id.btnGuardar);

        // Establecer el número de cuenta en el TextView
        if (numeroCuenta != null) {
            tvCuenta.setText(numeroCuenta);
        } else {
            tvCuenta.setText("No. de cuenta no disponible");
        }

        // Acción para guardar datos
        btnGuardar.setOnClickListener(v -> {
            String peso = etPeso.getText().toString().trim();
            String altura = etAltura.getText().toString().trim();
            String motivo = etMotivo.getText().toString().trim();
            String padecimiento = etPadecimiento.getText().toString().trim();
            String medicamento = etMedicamento.getText().toString().trim();

            if (peso.isEmpty() || altura.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Crear un mapa con los datos
            Map<String, Object> informeData = new HashMap<>();
            informeData.put("numeroCuenta", numeroCuenta);
            informeData.put("peso", peso);
            informeData.put("altura", altura);
            informeData.put("motivo", motivo);
            informeData.put("padecimiento", padecimiento);
            informeData.put("medicamento", medicamento);
            informeData.put("nombreDoctor", nombreDoctor);  // Agregar el nombre del doctor
            informeData.put("nombre", nombrePaciente);

            // Guardar el informe en Firestore
            db.collection("informe")
                    .add(informeData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Datos guardados exitosamente", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Informe agregado con ID: " + documentReference.getId());

                        // Actualizar el estado de la cita
                        if (citaId != null) {
                            actualizarEstadoCita(citaId);
                        } else {
                            Log.e(TAG, "ID de cita no proporcionado");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error al guardar informe", e);
                    });
        });
    }

    private void actualizarEstadoCita(String citaId) {
        db.collection("citas")
                .document(citaId)
                .update("estado", "realizada")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Estado de la cita actualizado a 'realizada'", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Estado de la cita actualizado exitosamente");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al actualizar el estado de la cita", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al actualizar estado de la cita", e);
                });
    }
}
