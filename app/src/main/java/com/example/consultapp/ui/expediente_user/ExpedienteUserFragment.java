package com.example.consultapp.ui.expediente_user;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.consultapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ExpedienteUserFragment extends Fragment {

    // Vistas
    private TextView nombreTextView, cuentaTextView;
    private FirebaseAuth auth;
    private DatabaseReference dbRef; // Referencia a la base de datos en tiempo real
    private static final int PICK_IMAGE_REQUEST = 1; // Código para seleccionar imagen
    private Uri imageUri = null; // URI de la imagen seleccionada
    private StorageReference storageReference;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_expediente_user, container, false);

        // Inicializa Firebase Storage
        storageReference = FirebaseStorage.getInstance().getReference();

        // Inicializa vistas
        nombreTextView = root.findViewById(R.id.nombre);
        cuentaTextView = root.findViewById(R.id.numeroCuenta);
        ImageButton btnEditar = root.findViewById(R.id.btn_editar); // Botón para abrir el diálogo
        ImageView imageViewPreview = root.findViewById(R.id.imageView); // Vista previa de la imagen
        ImageButton selectImageButton = root.findViewById(R.id.selectImageButton); // Botón para seleccionar imagen

        // Inicializa Firebase
        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        // Carga el nombre del usuario
        loadUserName();

        // Configura el botón para mostrar el diálogo
        btnEditar.setOnClickListener(v -> showBottomDialog());

        // Configura el clic en el botón de seleccionar imagen
        selectImageButton.setOnClickListener(v -> openImageSelector());

        return root;
    }

    // Método para cargar el nombre y otros datos del usuario
    private void loadUserName() {
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference userRef = dbRef.child("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String nombre = snapshot.child("nombre").getValue(String.class);
                    String cuenta = snapshot.child("numeroCuenta").getValue(String.class);
                    String edad = snapshot.child("edad").getValue(String.class);
                    String fechaNacimiento = snapshot.child("fecha_nacimiento").getValue(String.class);
                    String genero = snapshot.child("genero").getValue(String.class);
                    String telefono = snapshot.child("telefono").getValue(String.class);
                    String fotoPerfilUrl = snapshot.child("fotoPerfil").getValue(String.class); // URL de la imagen

                    // Mostrar datos en los TextView
                    nombreTextView.setText(nombre != null ? nombre : "Nombre no disponible");
                    cuentaTextView.setText(cuenta != null ? cuenta : "Número de cuenta no disponible");
                    ((TextView) requireView().findViewById(R.id.edad))
                            .setText(edad != null ? edad + " años" : "Edad no especificada");
                    ((TextView) requireView().findViewById(R.id.fechaNacimiento))
                            .setText(fechaNacimiento != null ? fechaNacimiento : "Fecha de nacimiento no registrada");
                    ((TextView) requireView().findViewById(R.id.genero))
                            .setText(genero != null ? genero : "Género no especificado");
                    ((TextView) requireView().findViewById(R.id.telefono))
                            .setText(telefono != null ? telefono : "Teléfono no registrado");

                    // Cargar la imagen usando Glide
                    if (fotoPerfilUrl != null && !fotoPerfilUrl.isEmpty()) {
                        ImageView imageViewPreview = requireView().findViewById(R.id.imageView);
                        Glide.with(ExpedienteUserFragment.this)
                                .load(fotoPerfilUrl)
                                .transform(new RoundedCorners(150)) // Redondear las esquinas
                                .placeholder(R.drawable.round_person_outline_24)
                                .into(imageViewPreview);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Error al cargar los datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Mostrar el diálogo para editar perfil
    private void showBottomDialog() {
        // Crear un diálogo personalizado
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheet_layout_perfil_user);

        // Referencias a los elementos del layout
        EditText nombreUser = dialog.findViewById(R.id.nombre_user);
        EditText edadUser = dialog.findViewById(R.id.edad_user);
        EditText fechaNacimiento = dialog.findViewById(R.id.nombre_especialidad);
        EditText generoUser = dialog.findViewById(R.id.genero_user);
        EditText telefonoUser = dialog.findViewById(R.id.telefono_user);
        Button botonEditarPerfilModal = dialog.findViewById(R.id.btnEditar);

        // Obtener los datos del usuario desde Realtime Database
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference userRef = dbRef.child("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Cargar los datos actuales en los EditText
                    nombreUser.setText(snapshot.child("nombre").getValue(String.class));
                    edadUser.setText(snapshot.child("edad").getValue(String.class));
                    fechaNacimiento.setText(snapshot.child("fecha_nacimiento").getValue(String.class));
                    generoUser.setText(snapshot.child("genero").getValue(String.class));
                    telefonoUser.setText(snapshot.child("telefono").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Error al cargar los datos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Configurar el clic en el botón del modal
        botonEditarPerfilModal.setOnClickListener(v -> {
            String nombre = nombreUser.getText().toString().trim();
            String edad = edadUser.getText().toString().trim();
            String fechaNac = fechaNacimiento.getText().toString().trim();
            String genero = generoUser.getText().toString().trim();
            String telefono = telefonoUser.getText().toString().trim();
            editarPerfilDesdeModal(nombre, edad, fechaNac, genero, telefono);
            dialog.dismiss();  // Cerrar el modal después de agregar
        });

        // Mostrar el diálogo con animación y estilo
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            dialog.getWindow().setGravity(Gravity.BOTTOM);
        }
    }

    // Método para editar el perfil desde el modal
    private void editarPerfilDesdeModal(String nombre, String edad, String fechaNac, String genero, String telefono) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), "No se ha iniciado sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        Map<String, Object> datosActualizados = new HashMap<>();
        if (!nombre.isEmpty()) datosActualizados.put("nombre", nombre);
        if (!edad.isEmpty()) datosActualizados.put("edad", edad);
        if (!fechaNac.isEmpty()) datosActualizados.put("fecha_nacimiento", fechaNac);
        if (!genero.isEmpty()) datosActualizados.put("genero", genero);
        if (!telefono.isEmpty()) datosActualizados.put("telefono", telefono);

        // Actualizar los datos en la base de datos
        DatabaseReference userRef = dbRef.child("users").child(userId);
        userRef.updateChildren(datosActualizados).addOnSuccessListener(unused -> {
            Toast.makeText(requireContext(), "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
            loadUserName();  // Recargar los datos
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Error al actualizar el perfil", Toast.LENGTH_SHORT).show();
        });
    }

    // Método para abrir el selector de imágenes
    private void openImageSelector() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Manejar el resultado de la selección de imagen
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData(); // Obtener la URI de la imagen seleccionada
            Glide.with(requireContext()).load(imageUri).transform(new RoundedCorners(150)).into((ImageView) requireView().findViewById(R.id.imageView)); // Mostrarla en la vista previa
            uploadImageToFirebase(); // Subir la imagen a Firebase
        }
    }

    // Subir la imagen seleccionada a Firebase Storage
    private void uploadImageToFirebase() {
        if (imageUri != null) {
            StorageReference fileReference = storageReference.child("FotosPerfil/" + System.currentTimeMillis() + ".jpg");

            fileReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String userId = auth.getCurrentUser().getUid();
                    dbRef.child("users").child(userId).child("fotoPerfil").setValue(uri.toString()); // Guardar la URL de la imagen en la base de datos
                    Toast.makeText(requireContext(), "Imagen subida exitosamente", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(requireContext(), "No se ha seleccionado ninguna imagen", Toast.LENGTH_SHORT).show();
        }
    }
}
