package com.example.consultapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class PerfilDoc extends AppCompatActivity {

    private TextView nombreTextView, especializacionTextView, horarioTextView, telefonoTextView;
    private ImageView imageViewPerfil;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Button btnDescripcion;
    private Button btn_cerrarS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_doc);

        mAuth = FirebaseAuth.getInstance();  // Inicializa FirebaseAuth
        db = FirebaseFirestore.getInstance();  // Inicializa Firestore

        // Referencias a los TextView y Button
        nombreTextView = findViewById(R.id.nombre);
        especializacionTextView = findViewById(R.id.especializacion);
        horarioTextView = findViewById(R.id.horario);
        telefonoTextView = findViewById(R.id.telefono);
        btnDescripcion = findViewById(R.id.btn_descripcion);
        btn_cerrarS = findViewById(R.id.btn_cerrarS);

        // Llamar al método para cargar los datos del usuario
        cargarDatosUsuario();

        // Configura el clic del botón para mostrar el modal de descripción
        btnDescripcion.setOnClickListener(v -> showBottomDialog());

        // Configurar el botón para cerrar sesión
        btn_cerrarS.setOnClickListener(view -> {
            mAuth.signOut();
            Intent intent = new Intent(PerfilDoc.this, login.class);
            startActivity(intent);
            finish(); // Cierra la actividad actual
        });
    }

    private void cargarDatosUsuario() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Obtén el UID del usuario actual
        String userId = auth.getCurrentUser().getUid();

        // Referencia al documento del usuario en Firestore
        DocumentReference userRef = db.collection("medicos").document(userId);

        // Obtén los datos del usuario desde Firestore
        userRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Extrae los datos del documento
                        String nombre = documentSnapshot.getString("nombre");
                        String especializacion = documentSnapshot.getString("especializacion");
                        String descripcion = documentSnapshot.getString("descripcion");
                        String telefono = documentSnapshot.getString("telefono");
                        List<String> horarios = (List<String>) documentSnapshot.get("horarios");

                        // Muestra los datos en los TextView
                        nombreTextView.setText(nombre);
                        especializacionTextView.setText(especializacion);
                        telefonoTextView.setText(telefono);

                        // Si el array de horarios no es nulo y tiene elementos
                        if (horarios != null && horarios.size() > 0) {
                            // Usa el primer y último elemento del array
                            String horarioInicio = horarios.get(0);
                            String horarioSalida = horarios.get(horarios.size() - 1);

                            // Muestra el rango de horarios
                            horarioTextView.setText(horarioInicio + " - " + horarioSalida);
                        } else {
                            horarioTextView.setText("Horario no disponible");
                        }

                        // Si la descripción está disponible, actualiza el texto del botón
                        if (descripcion != null && !descripcion.isEmpty()) {
                            btnDescripcion.setText(descripcion);  // Cambia el texto del botón
                        } else {
                            btnDescripcion.setText("Añade una descripción");  // Texto por defecto si no hay descripción
                        }
                    } else {
                        Toast.makeText(this, "Datos del usuario no encontrados", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar datos del usuario", Toast.LENGTH_SHORT).show();
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
                // Obtener el ID del usuario actual
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference userRef = db.collection("medicos").document(userId);

                // Actualizar la descripción en Firestore
                userRef.update("descripcion", descripcion)
                        .addOnSuccessListener(aVoid -> {
                            // Mostrar un mensaje de éxito
                            Toast.makeText(this, "Descripción guardada con éxito", Toast.LENGTH_SHORT).show();

                            // Actualiza el texto del botón con la nueva descripción
                            btnDescripcion.setText(descripcion);

                            // Cerrar el diálogo
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            // Mostrar un mensaje de error
                            Toast.makeText(this, "Error al guardar la descripción", Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Si la descripción está vacía
                Toast.makeText(this, "Por favor, ingresa una descripción", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
