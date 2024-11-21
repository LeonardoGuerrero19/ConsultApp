package com.example.consultapp.ui.expediente_user;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.consultapp.R;
import com.example.consultapp.databinding.FragmentEspecialidadesBinding;
import com.example.consultapp.databinding.FragmentExpedienteUserBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ExpedienteUserFragment extends Fragment {

    private TextView nombreTextView;
    private FirebaseAuth auth;
    private FragmentExpedienteUserBinding binding;  // Binding para acceder a las vistas
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_expediente_user, container, false);

        // Inicializa las vistas
        nombreTextView = root.findViewById(R.id.nombre);
        ImageButton btnEditar = root.findViewById(R.id.btn_editar); // Botón para abrir el diálogo

        // Inicializa Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Carga el nombre del usuario
        loadUserName();

        // Configura el botón para mostrar el diálogo
        btnEditar.setOnClickListener(v -> showBottomDialog());

        return root;
    }

    private void loadUserName() {
        String userId = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("user").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String nombre = documentSnapshot.getString("nombre");
                String edad = documentSnapshot.getString("edad");
                String fechaNacimiento = documentSnapshot.getString("fecha_nacimiento");
                String genero = documentSnapshot.getString("genero");
                String telefono = documentSnapshot.getString("telefono");

                // Mostrar datos en los TextView
                nombreTextView.setText(nombre != null ? nombre : "Nombre no disponible");
                ((TextView) requireView().findViewById(R.id.edad))
                        .setText(edad != null ? edad + " años" : "Edad no especificada");
                ((TextView) requireView().findViewById(R.id.fechaNacimiento))
                        .setText(fechaNacimiento != null ? fechaNacimiento : "Fecha de nacimiento no registrada");
                ((TextView) requireView().findViewById(R.id.genero))
                        .setText(genero != null ? genero : "Género no especificado");
                ((TextView) requireView().findViewById(R.id.telefono))
                        .setText(telefono != null ? telefono : "Teléfono no registrado");
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Error al cargar los datos", Toast.LENGTH_SHORT).show();
        });
    }

    private void showBottomDialog() {
        // Crear un diálogo personalizado
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheet_layout_perfil_user);

        // Referencias a los elementos del layout
        EditText nombreUser = dialog.findViewById(R.id.nombre_user);
        EditText edadUser = dialog.findViewById(R.id.edad_user);
        EditText fechaNacimiento = dialog.findViewById(R.id.nombre_especialidad); // Cambia el ID si no es correcto
        EditText generoUser = dialog.findViewById(R.id.genero_user);
        EditText telefonoUser = dialog.findViewById(R.id.telefono_user);
        Button botonEditarPerfilModal = dialog.findViewById(R.id.btnEditar);

        // Obtener los datos del usuario desde Firestore
        String userId = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("user").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Cargar los datos actuales en los EditText
                nombreUser.setText(documentSnapshot.getString("nombre"));
                edadUser.setText(documentSnapshot.getString("edad"));
                fechaNacimiento.setText(documentSnapshot.getString("fecha_nacimiento"));
                generoUser.setText(documentSnapshot.getString("genero"));
                telefonoUser.setText(documentSnapshot.getString("telefono"));
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Error al cargar los datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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


    private void editarPerfilDesdeModal(String nombre, String edad, String fechaNac, String genero, String telefono) {
        // Verificar que haya un usuario autenticado
        if (auth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), "No se ha iniciado sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener el UID del usuario actual
        String userId = auth.getCurrentUser().getUid();

        // Crear un mapa con los datos actualizados
        Map<String, Object> datosActualizados = new HashMap<>();
        if (!nombre.isEmpty()) datosActualizados.put("nombre", nombre);
        if (!edad.isEmpty()) datosActualizados.put("edad", edad);
        if (!fechaNac.isEmpty()) datosActualizados.put("fecha_nacimiento", fechaNac);
        if (!genero.isEmpty()) datosActualizados.put("genero", genero);
        if (!telefono.isEmpty()) datosActualizados.put("telefono", telefono);

        // Referencia al documento del usuario
        DocumentReference userRef = db.collection("user").document(userId);

        // Actualizar los datos en Firestore
        userRef.update(datosActualizados)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(), "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
                    // Recargar el nombre o cualquier dato visible si es necesario
                    loadUserName();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error al actualizar el perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


}
