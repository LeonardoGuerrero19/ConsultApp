package com.example.funcion_loginregistro;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.firestore.DocumentSnapshot;


import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;


public class RegistrarMedicoActivity extends AppCompatActivity {

    private static final int CODIGO_SELECCION_IMAGEN = 1; // Código para seleccionar imagen
    private static final int PERMISO_ALMACENAMIENTO = 101; // Código para permisos de almacenamiento

    private EditText etNombreMedico, etCorreoMedico, etContrasenaMedico, etTelefonoMedico;
    private Spinner spinnerEspecializacion;
    private Button btnRegistrarMedico, btnCerrarS, btnSeleccionarImagen;
    private ImageView imagenPreview; // Vista previa de la imagen seleccionada

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private Uri imagenUri; // Para almacenar la URI de la imagen seleccionada

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_medico);

        // Inicializar FirebaseAuth, Firestore y Storage
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Referencias a los elementos en el layout
        etNombreMedico = findViewById(R.id.nombreMedico);
        etCorreoMedico = findViewById(R.id.correoMedico);
        etContrasenaMedico = findViewById(R.id.contrasenaMedico);
        etTelefonoMedico = findViewById(R.id.telefonoMedico);
        spinnerEspecializacion = findViewById(R.id.spinner_especializacion);
        btnRegistrarMedico = findViewById(R.id.btn_registrarMedico);
        btnCerrarS = findViewById(R.id.btn_cerrarS);
        btnSeleccionarImagen = findViewById(R.id.btn_seleccionar_imagen); // Botón para seleccionar imagen
        imagenPreview = findViewById(R.id.imagen_preview); // Vista previa de la imagen seleccionada

        // Cargar especializaciones desde Firestore
        cargarEspecializacionesDesdeFirestore();

        // Verificar permisos de almacenamiento
        verificarPermisos();

        // Cerrar sesión
        btnCerrarS.setOnClickListener(view -> {
            mAuth.signOut();
            Intent intent = new Intent(RegistrarMedicoActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Seleccionar imagen
        btnSeleccionarImagen.setOnClickListener(v -> seleccionarImagen());

        // Registrar médico
        btnRegistrarMedico.setOnClickListener(v -> {
            String nombre = etNombreMedico.getText().toString();
            String correo = etCorreoMedico.getText().toString();
            String contrasena = etContrasenaMedico.getText().toString();
            String telefono = etTelefonoMedico.getText().toString();
            String especializacion = spinnerEspecializacion.getSelectedItem().toString();

            if (!nombre.isEmpty() && !correo.isEmpty() && !contrasena.isEmpty() && !telefono.isEmpty() && !especializacion.isEmpty()) {
                if (imagenUri != null) {
                    subirImagenAFirebase(nombre, correo, contrasena, telefono, especializacion);
                } else {
                    Toast.makeText(this, "Selecciona una imagen antes de continuar", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void seleccionarImagen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // Si ya tiene permiso, abrir la galería
            Log.d("RegistrarMedicoActivity", "Permiso concedido, abriendo galería...");
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, CODIGO_SELECCION_IMAGEN);
        } else {
            // Solicitar permiso si no tiene
            Log.d("RegistrarMedicoActivity", "Permiso no concedido, solicitando...");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISO_ALMACENAMIENTO);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODIGO_SELECCION_IMAGEN && resultCode == RESULT_OK && data != null) {
            imagenUri = data.getData(); // Obtiene la URI de la imagen seleccionada
            Log.d("RegistrarMedicoActivity", "Imagen seleccionada, URI: " + imagenUri.toString());
            imagenPreview.setImageURI(imagenUri); // Muestra la imagen seleccionada en el ImageView
            Toast.makeText(this, "Imagen seleccionada correctamente", Toast.LENGTH_SHORT).show();
        }
    }

    private void subirImagenAFirebase(String nombre, String correo, String contrasena, String telefono, String especializacion) {
        try {
            // Crear referencia al archivo en Firebase Storage
            String uid = mAuth.getCurrentUser().getUid();
            StorageReference imagenRef = storageRef.child("imagenes/" + uid + ".jpg");

            // Abrir InputStream desde la URI
            InputStream stream = getContentResolver().openInputStream(imagenUri);

            // Subir imagen a Firebase Storage
            Log.d("RegistrarMedicoActivity", "Subiendo imagen a Firebase Storage...");
            UploadTask uploadTask = imagenRef.putStream(stream);
            uploadTask.addOnFailureListener(exception -> {
                Log.e("RegistrarMedicoActivity", "Error al subir la imagen: " + exception.getMessage());
                Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
            }).addOnSuccessListener(taskSnapshot -> {
                Log.d("RegistrarMedicoActivity", "Imagen subida con éxito");
                // Obtener la URL de descarga de la imagen subida
                imagenRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String urlImagen = uri.toString(); // URL pública de la imagen
                    Log.d("RegistrarMedicoActivity", "URL de la imagen subida: " + urlImagen);
                    guardarMedicoEnFirestore(nombre, correo, contrasena, telefono, especializacion, urlImagen);
                });
            });

        } catch (FileNotFoundException e) {
            Log.e("RegistrarMedicoActivity", "Error al abrir la imagen: " + e.getMessage());
            Toast.makeText(this, "No se pudo acceder a la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarMedicoEnFirestore(String nombre, String correo, String contrasena, String telefono, String especializacion, String urlImagen) {
        String uid = mAuth.getCurrentUser().getUid();

        // Crear datos para Firestore
        Map<String, Object> medico = new HashMap<>();
        medico.put("nombre", nombre);
        medico.put("correo", correo);
        medico.put("telefono", telefono);
        medico.put("especializacion", especializacion);
        medico.put("rol", "medico");
        medico.put("fotoUrl", urlImagen); // Guardar la URL de la imagen

        // Guardar datos en Firestore
        db.collection("user").document(uid).set(medico).addOnSuccessListener(aVoid -> {
            Log.d("RegistrarMedicoActivity", "Médico registrado en Firestore");
            Toast.makeText(this, "Médico registrado con éxito.", Toast.LENGTH_SHORT).show();
            limpiarCampos();
        }).addOnFailureListener(e -> {
            Log.e("RegistrarMedicoActivity", "Error al registrar médico en Firestore: " + e.getMessage());
            Toast.makeText(this, "Error al registrar médico en Firestore", Toast.LENGTH_SHORT).show();
        });
    }

    private void cargarEspecializacionesDesdeFirestore() {
        db.collection("especializaciones").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<String> especializaciones = new ArrayList<>();
            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                String especializacion = documentSnapshot.getString("nombre");
                especializaciones.add(especializacion);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, especializaciones);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerEspecializacion.setAdapter(adapter);
        }).addOnFailureListener(e -> {
            Log.e("RegistrarMedicoActivity", "Error al cargar especializaciones: " + e.getMessage());
            Toast.makeText(this, "Error al cargar especializaciones", Toast.LENGTH_SHORT).show();
        });
    }

    private void verificarPermisos() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d("RegistrarMedicoActivity", "Permiso de almacenamiento no concedido.");
            // Aquí podrías solicitar el permiso si es necesario
        } else {
            Log.d("RegistrarMedicoActivity", "Permiso de almacenamiento concedido.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISO_ALMACENAMIENTO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("RegistrarMedicoActivity", "Permiso concedido.");
                // Si el permiso es concedido, puedes proceder con la selección de imagen
                seleccionarImagen();
            } else {
                Log.d("RegistrarMedicoActivity", "Permiso denegado.");
                Toast.makeText(this, "El permiso es necesario para seleccionar una imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void limpiarCampos() {
        etNombreMedico.setText("");
        etCorreoMedico.setText("");
        etContrasenaMedico.setText("");
        etTelefonoMedico.setText("");
        spinnerEspecializacion.setSelection(0);
        imagenPreview.setImageURI(null); // Limpiar la imagen seleccionada
    }
}
