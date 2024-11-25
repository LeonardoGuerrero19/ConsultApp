package com.example.consultapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class InformeMedicoActivity extends AppCompatActivity {

    private static final String TAG = "InformeMedicoActivity";
    private DatabaseReference dbRef;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informe_medico);

        // Inicializar Firebase Realtime Database y Storage
        dbRef = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference("InformesPDF");

        // Obtener datos del Intent
        String usuarioId = getIntent().getStringExtra("usuarioId");
        String numeroCuenta = getIntent().getStringExtra("numeroCuenta");
        String citaId = getIntent().getStringExtra("citaId");
        String nombreDoctor = getIntent().getStringExtra("nombreDoctor");
        String nombrePaciente = getIntent().getStringExtra("nombre");

        // Referencia a los campos
        TextView tvCuenta = findViewById(R.id.tvCuenta);
        EditText etPeso = findViewById(R.id.peso);
        EditText etAltura = findViewById(R.id.estatura);
        EditText etAlergias = findViewById(R.id.etAlergias);
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
            String alergias = etAlergias.getText().toString().trim();
            String motivo = etMotivo.getText().toString().trim();
            String padecimiento = etPadecimiento.getText().toString().trim();
            String medicamento = etMedicamento.getText().toString().trim();

            if (peso.isEmpty() && altura.isEmpty() && alergias.isEmpty() && motivo.isEmpty() && padecimiento.isEmpty() && medicamento.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Crear un mapa con los datos
            Map<String, Object> informeData = new HashMap<>();
            informeData.put("numeroCuenta", numeroCuenta);
            informeData.put("peso", peso);
            informeData.put("altura", altura);
            informeData.put("alergias", alergias);
            informeData.put("motivo", motivo);
            informeData.put("padecimiento", padecimiento);
            informeData.put("medicamento", medicamento);
            informeData.put("nombreDoctor", nombreDoctor);
            informeData.put("nombre", nombrePaciente);

            // Generar un ID único para el informe
            String informeId = dbRef.child("informe").push().getKey();

            if (informeId != null) {
                // Guardar el informe en Realtime Database
                dbRef.child("Informes").child(informeId).setValue(informeData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Datos guardados exitosamente", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Informe agregado con ID: " + informeId);

                            // Generar y guardar el PDF
                            generarYGuardarPDF(informeId, informeData);

                            // Actualizar el estado de la cita
                            if (citaId != null) {
                                actualizarEstadoCita(citaId);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Error al guardar informe", e);
                        });
            } else {
                Toast.makeText(this, "Error al generar ID del informe", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generarYGuardarPDF(String informeId, Map<String, Object> informeData) {
        // Crear un documento PDF
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();

        // Crear una página en el documento
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Configurar estilos de texto
        Paint titlePaint = new Paint();
        titlePaint.setTextSize(16);
        titlePaint.setFakeBoldText(true);

        Paint contentPaint = new Paint();
        contentPaint.setTextSize(12);

        // Cargar el logo
        Bitmap logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo); // Cambia "consultapp_logo" al nombre de tu archivo de logo
        Bitmap scaledLogo = Bitmap.createScaledBitmap(logoBitmap, 80, 80, false); // Ajustar tamaño del logo

        // Dibujar el logo en la parte superior derecha
        canvas.drawBitmap(scaledLogo, pageInfo.getPageWidth() - 100, 10, paint);

        // Dibujar el título
        canvas.drawText("ConsultApp", 10, 40, titlePaint);
        canvas.drawText("Gestión y administración de citas médicas", 10, 60, contentPaint);

        // Dibujar una línea separadora
        canvas.drawLine(10, 80, pageInfo.getPageWidth() - 10, 80, paint);

        // Dibujar el contenido del informe
        int y = 100; // Posición inicial en Y
        for (Map.Entry<String, Object> entry : informeData.entrySet()) {
            canvas.drawText(entry.getKey() + ": " + entry.getValue(), 10, y, contentPaint);
            y += 20; // Incrementar la posición en Y
        }

        // Terminar la página
        pdfDocument.finishPage(page);

        // Crear archivo local para el PDF
        File pdfFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "Informe_" + informeId + ".pdf");
        try {
            pdfDocument.writeTo(new FileOutputStream(pdfFile));
            Log.d(TAG, "Archivo PDF creado en: " + pdfFile.getPath());

            // Subir el archivo PDF a Firebase Storage
            subirPDFaFirebase(pdfFile, informeId);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar el PDF", Toast.LENGTH_SHORT).show();
        } finally {
            pdfDocument.close();
        }
    }


    private void subirPDFaFirebase(File pdfFile, String informeId) {
        StorageReference fileRef = storageRef.child(informeId + ".pdf");

        fileRef.putFile(Uri.fromFile(pdfFile))
                .addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(this, "PDF subido a Firebase Storage", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Archivo PDF subido correctamente");

                    // Finalizar la actividad después de subir exitosamente
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al subir el PDF a Firebase Storage", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al subir el PDF", e);
                });
    }

    private void actualizarEstadoCita(String citaId) {
        dbRef.child("citas").child(citaId).child("estado").setValue("realizada")
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
