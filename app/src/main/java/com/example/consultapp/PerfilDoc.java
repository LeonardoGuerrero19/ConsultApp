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
import androidx.annotation.NonNull;


import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class PerfilDoc extends AppCompatActivity {

    private TextView nombreTextView, especializacionTextView, horarioTextView, telefonoTextView;
    private ImageView imageViewPerfil;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private Button btnDescripcion;
    private Button btn_cerrarS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_doc);

        mAuth = FirebaseAuth.getInstance(); // Inicializa FirebaseAuth
        databaseReference = FirebaseDatabase.getInstance().getReference(); // Inicializa Realtime Database

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
                    List<String> horarios = (List<String>) snapshot.child("horarios").getValue();

                    // Mostrar los datos en los TextView
                    nombreTextView.setText(nombre);
                    especializacionTextView.setText(especializacion);
                    telefonoTextView.setText(telefono);

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
                databaseReference.child("medicos").child(userId).child("descripcion").setValue(descripcion)
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
}
