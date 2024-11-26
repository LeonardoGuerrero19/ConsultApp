package com.example.consultapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;


import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

public class PerfilDoc extends AppCompatActivity {

    private TextView nombreTextView, especializacionTextView, horarioTextView;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private Button btnDescripcion, btnTelefono;
    private static final int PICK_IMAGE_REQUEST = 1; // Código de solicitud para seleccionar la imagen
    private Uri imageUri = null; // URI de la imagen seleccionada
    private StorageReference storageReference;
    private Button btn_cerrarS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_doc);

        mAuth = FirebaseAuth.getInstance(); // Inicializa FirebaseAuth
        databaseReference = FirebaseDatabase.getInstance().getReference(); // Inicializa Realtime Database

        // Inicializa Firebase Storage
        storageReference = FirebaseStorage.getInstance().getReference();

        // Referencias a los TextView y Button
        nombreTextView = findViewById(R.id.nombre);
        especializacionTextView = findViewById(R.id.especializacion);
        horarioTextView = findViewById(R.id.horario);
        btnTelefono = findViewById(R.id.btnTelefono);
        btnDescripcion = findViewById(R.id.btn_descripcion);
        btn_cerrarS = findViewById(R.id.btn_cerrarS);
        ImageView imageViewPreview = findViewById(R.id.imageView); // O el ID adecuado del ImageView en tu layout
        ImageButton selectImageButton = findViewById(R.id.selectImageButton); // Botón para seleccionar imagen

        // Llamar al método para cargar los datos del usuario
        cargarDatosUsuario();

        // Configura el clic del botón para mostrar el modal de descripción
        btnDescripcion.setOnClickListener(v -> showBottomDialog());

        // Configura el clic del botón para mostrar el modal de descripción
        btnTelefono.setOnClickListener(v -> showBottomDialog1());

        // Configura el clic en el botón de seleccionar imagen
        selectImageButton.setOnClickListener(v -> openImageSelector());

        // Configurar el botón para cerrar sesión
        btn_cerrarS.setOnClickListener(view -> {
            mAuth.signOut();
            Intent intent = new Intent(PerfilDoc.this, login.class);
            startActivity(intent);
            finish(); // Cierra la actividad actual
        });
    }

    private void cargarDatosUsuario() {
        String userId = mAuth.getCurrentUser().getUid();

        // Referencia al nodo del usuario en Realtime Database
        databaseReference.child("Medicos").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Extraer los datos del snapshot
                    String nombre = snapshot.child("nombre").getValue(String.class);
                    String especializacion = snapshot.child("especializacion").getValue(String.class);
                    String descripcion = snapshot.child("descripcion").getValue(String.class);
                    String telefono = snapshot.child("telefono").getValue(String.class);
                    String fotoPerfilUrl = snapshot.child("fotoPerfil").getValue(String.class); // URL de la imagen
                    List<String> horarios = (List<String>) snapshot.child("horarios").getValue();

                    // Mostrar los datos en los TextView
                    nombreTextView.setText(nombre);
                    especializacionTextView.setText(especializacion);
                    btnTelefono.setText(telefono);

                    if (horarios != null && !horarios.isEmpty()) {
                        // Usa el primer y último elemento de los horarios
                        String horarioInicio = horarios.get(0);
                        String horarioSalida = horarios.get(horarios.size() - 1);
                        horarioTextView.setText(horarioInicio + " - " + horarioSalida);
                    } else {
                        horarioTextView.setText("Horario no disponible");
                    }

                    // Actualiza el texto del botón con la descripción
                    if (descripcion != null && !descripcion.isEmpty()) {
                        btnDescripcion.setText(descripcion);
                    } else {
                        btnDescripcion.setText("Añade una descripción");
                    }

                    // Actualiza el texto del botón con el telefono
                    if (telefono != null && !telefono.isEmpty()) {
                        btnTelefono.setText(telefono);
                    } else {
                        btnTelefono.setText("Añade un número de télefono");
                    }

                    // Convertir 18dp a píxeles
                    float radius = 18 * getResources().getDisplayMetrics().density;

                    // Cargar la imagen usando Glide
                    if (fotoPerfilUrl != null && !fotoPerfilUrl.isEmpty()) {
                        ImageView imageViewPreview = findViewById(R.id.imageView);
                        Glide.with(PerfilDoc.this)
                                .load(fotoPerfilUrl)
                                .apply(RequestOptions.bitmapTransform(new RoundedCorners((int) radius))) // Aplicar borde redondeado
                                .placeholder(R.drawable.round_person_outline_24)
                                .into(imageViewPreview);
                    }
                } else {
                    Toast.makeText(PerfilDoc.this, "Datos del usuario no encontrados", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PerfilDoc.this, "Error al cargar datos del usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showBottomDialog() {
        // Crear el diálogo
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheet_layout_descripcionmedico);

        // Obtener referencias a los elementos del layout
        final EditText descripcionEditText = dialog.findViewById(R.id.descripcion_TextView);
        Button btnGuardarDescripcion = dialog.findViewById(R.id.btnAgregarDescripcion);

        // Mostrar el diálogo
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        // Acción del botón de guardar
        btnGuardarDescripcion.setOnClickListener(v -> {
            String descripcion = descripcionEditText.getText().toString().trim();

            if (!descripcion.isEmpty()) {
                String userId = mAuth.getCurrentUser().getUid();

                // Actualizar la descripción en Realtime Database
                databaseReference.child("Medicos").child(userId).child("descripcion").setValue(descripcion)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(PerfilDoc.this, "Descripción guardada con éxito", Toast.LENGTH_SHORT).show();
                            btnDescripcion.setText(descripcion);
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(PerfilDoc.this, "Error al guardar la descripción", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(this, "Por favor, ingresa una descripción", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showBottomDialog1() {
        // Crear el diálogo
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheet_layout_telefonomedico);

        // Obtener referencias a los elementos del layout
        final EditText telefonoEditText = dialog.findViewById(R.id.telefono_TextView);
        Button btnEditarTelefono = dialog.findViewById(R.id.btnEditarTelefono);

        // Mostrar el diálogo
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        // Acción del botón de guardar
        btnEditarTelefono.setOnClickListener(v -> {
            String telefono = telefonoEditText.getText().toString().trim();

            if (!telefono.isEmpty()) {
                String userId = mAuth.getCurrentUser().getUid();

                // Actualizar la descripción en Realtime Database
                databaseReference.child("Medicos").child(userId).child("telefono").setValue(telefono)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(PerfilDoc.this, "Número de télefono guardado con éxito", Toast.LENGTH_SHORT).show();
                            btnTelefono.setText(telefono);
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(PerfilDoc.this, "Error al guardar el telefono", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(this, "Por favor, ingresa un número de telefono", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImageSelector() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Método para manejar la selección de la imagen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData(); // Obtiene el URI de la imagen seleccionada

            // Muestra la imagen seleccionada en el ImageView (opcional)
            ImageView imageViewPreview = findViewById(R.id.imageView);
            imageViewPreview.setImageURI(imageUri);

            // Llama al método para subir la imagen
            subirImagen();
        }
    }

    // Método para subir la imagen a Firebase Storage
    private void subirImagen() {
        if (imageUri != null) {
            // Ruta donde se guardará la imagen en Firebase Storage
            String userId = mAuth.getCurrentUser().getUid();
            StorageReference fileReference = storageReference.child("FotosPerfil/" + userId + ".jpg");

            // Sube la imagen a Firebase Storage
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Obtiene la URL de descarga de la imagen subida
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();

                            // Guarda la URL de la imagen en la base de datos
                            databaseReference.child("Medicos").child(userId).child("fotoPerfil").setValue(downloadUrl)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(PerfilDoc.this, "Imagen subida con éxito", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(PerfilDoc.this, "Error al guardar la URL en la base de datos", Toast.LENGTH_SHORT).show();
                                    });
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(PerfilDoc.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show();
        }
    }
}
