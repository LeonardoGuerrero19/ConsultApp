package com.example.consultapp.ui.inicio;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.consultapp.AgendaActivity;
import com.example.consultapp.R;
import com.example.consultapp.databinding.FragmentInicioBinding;
import com.example.consultapp.login;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class InicioFragment extends Fragment {

    private FragmentInicioBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private GridLayout gridLayout;
    private LinearLayout linearProxCitas;
    private Button btn_cerrarS;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        InicioViewModel homeViewModel =
                new ViewModelProvider(this).get(InicioViewModel.class);

        binding = FragmentInicioBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializar Firestore y Auth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Inicializar vistas
        TextView textSaludo = binding.textSaludo;
        gridLayout = root.findViewById(R.id.gridLayout);
        linearProxCitas = root.findViewById(R.id.linearProxCitas);

        // Obtener el usuario actual de FirebaseAuth
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Referencia al documento del usuario en Firestore
            DocumentReference userRef = db.collection("user").document(userId);

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Obtener el nombre del campo "nombre"
                    String nombreUsuario = documentSnapshot.getString("nombre");
                    if (nombreUsuario != null) {
                        textSaludo.setText("Hola, " + nombreUsuario);
                    } else {
                        textSaludo.setText("Hola, Usuario");
                    }
                } else {
                    textSaludo.setText("Hola, Usuario");
                    Toast.makeText(getContext(), "No se encontró el usuario en la base de datos", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                textSaludo.setText("Hola, Usuario");
                Toast.makeText(getContext(), "Error al obtener el nombre del usuario", Toast.LENGTH_SHORT).show();
            });

            // Cargar próximas citas del usuario
            cargarProximasCitas(userId);

        } else {
            textSaludo.setText("Hola, Usuario");
            Toast.makeText(getContext(), "Usuario no logueado", Toast.LENGTH_SHORT).show();
        }

        // Obtener y mostrar los servicios
        obtenerServicios();

        // Usar la vista inflada para encontrar el botón
        btn_cerrarS = root.findViewById(R.id.btn_cerrarS);

        // Configurar el botón para cerrar sesión
        btn_cerrarS.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), login.class);
            startActivity(intent);
            getActivity().finish(); // Cierra la actividad actual
        });

        return root;
    }

    private void cargarProximasCitas(String userId) {
        db.collection("citas")
                .whereEqualTo("usuario_id", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String estado = document.getString("estado");
                            if ("proxima".equalsIgnoreCase(estado)) { // Verificar si el estado es "próxima"
                                String servicio = document.getString("servicio");
                                String fecha = document.getString("fecha");
                                String horario = document.getString("horario");

                                // Crear y agregar un TextView para cada cita válida
                                agregarTextoCita(servicio, fecha, horario);
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), "No tienes citas próximas.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar las citas.", Toast.LENGTH_SHORT).show();
                });
    }

    private void agregarTextoCita(String servicio, String fecha, String horario) {
        TextView textView = new TextView(getContext());
        textView.setText(String.format("Servicio: %s\nFecha: %s\nHorario: %s", servicio, fecha, horario));
        textView.setTextSize(16);
        textView.setPadding(16, 16, 16, 16);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Agregar el TextView al LinearLayout
        linearProxCitas.addView(textView);
    }

    private void obtenerServicios() {
        // Referencia al documento que contiene los servicios
        DocumentReference serviciosRef = db.collection("Servicios").document("Todos los servicios");

        // Obtener el documento
        serviciosRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    // Obtiene el array de servicios
                    List<String> servicios = (List<String>) document.get("Servicios");

                    if (servicios != null) {
                        // Recorrer la lista de servicios y agregar los botones al GridLayout
                        for (String servicio : servicios) {
                            Button button = new Button(getContext());
                            button.setText(servicio);
                            button.setLayoutParams(new GridLayout.LayoutParams(
                                    GridLayout.spec(GridLayout.UNDEFINED),
                                    GridLayout.spec(GridLayout.UNDEFINED)
                            ));
                            button.setPadding(20, 20, 20, 20);

                            // Acción cuando se hace clic en el botón
                            button.setOnClickListener(v -> {
                                // Crear un Intent para redirigir a AgendaActivity
                                Intent intent = new Intent(getContext(), AgendaActivity.class);
                                // Pasar el nombre del servicio como extra
                                intent.putExtra("nombreServicio", servicio);
                                startActivity(intent);
                            });

                            // Agrega el botón al GridLayout
                            gridLayout.addView(button);
                        }
                    }
                }
            } else {
                Toast.makeText(getContext(), "Error al cargar los servicios", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
