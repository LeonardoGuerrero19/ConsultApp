package com.example.consultapp.ui.calendario;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.consultapp.InformeMedicoActivity;
import com.example.consultapp.PerfilDoc;
import com.example.consultapp.R;
import com.example.consultapp.databinding.FragmentCalendarioBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CalendarioFragment extends Fragment {

    private FragmentCalendarioBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CalendarioViewModel calendarioViewModel =
                new ViewModelProvider(this).get(CalendarioViewModel.class);

        binding = FragmentCalendarioBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializar Firestore y Auth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Obtener referencias de vistas
        TextView textSaludo = binding.textSaludo;
        ImageButton imageButton = binding.image;
        LinearLayout linearCitas = binding.linearCitas;

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Referencia al documento del médico
            db.collection("medicos").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String nombreDoctor = documentSnapshot.getString("nombre");
                            if (nombreDoctor != null) {
                                textSaludo.setText("Hola, " + nombreDoctor);
                                // Cargar las citas próximas del doctor logueado
                                cargarCitasProximas(linearCitas, nombreDoctor);
                            } else {
                                textSaludo.setText("Hola, Usuario");
                            }
                        } else {
                            textSaludo.setText("Hola, Usuario");
                            Toast.makeText(getContext(), "No se encontró el usuario en la base de datos", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        textSaludo.setText("Hola, Usuario");
                        Toast.makeText(getContext(), "Error al obtener el nombre del usuario", Toast.LENGTH_SHORT).show();
                    });
        } else {
            textSaludo.setText("Hola, Usuario");
            Toast.makeText(getContext(), "Usuario no logueado", Toast.LENGTH_SHORT).show();
        }

        // Listener del botón para ir al perfil del doctor
        imageButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PerfilDoc.class);
            startActivity(intent);
        });

        return root;
    }

    private void cargarCitasProximas(LinearLayout linearCitas, String nombreDoctor) {
        db.collection("citas")
                .whereEqualTo("estado", "proxima")
                .whereEqualTo("doctor", nombreDoctor) // Filtrar por doctor
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Limpiar cualquier vista previa en linearCitas
                    linearCitas.removeAllViews();

                    List<Cita> citas = new ArrayList<>();

                    // Obtener las citas
                    // Dentro de la función cargarCitasProximas()

                    // Inflar las citas ordenadas en el LinearLayout
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String horario = document.getString("horario");
                        String usuarioId = document.getString("usuario_id");

                        // Inflar el layout de la cita
                        View citaView = getLayoutInflater().inflate(R.layout.item_cita_dia, linearCitas, false);

                        // Obtener referencias de los elementos del layout inflado
                        TextView txtCuenta = citaView.findViewById(R.id.txtCuenta);
                        TextView txtNombre = citaView.findViewById(R.id.txtNombre);
                        TextView txtHorario = citaView.findViewById(R.id.txtHorario);
                        ImageButton btnRealizada = citaView.findViewById(R.id.btn_realizada);
                        ImageButton btnCancelada = citaView.findViewById(R.id.btn_cancelada);

                        // Establecer los valores de la cita
                        txtHorario.setText(horario);

                        // Consultar la información del paciente usando el usuarioId
                        db.collection("user").document(usuarioId).get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        String nombrePaciente = userDoc.getString("nombre");
                                        String numeroCuenta = userDoc.getString("numeroCuenta");

                                        // Actualizar la vista con los datos del paciente
                                        txtCuenta.setText(numeroCuenta);
                                        txtNombre.setText(nombrePaciente);
                                    } else {
                                        // En caso de que no se encuentre el paciente
                                        txtNombre.setText("Paciente no encontrado");
                                        txtCuenta.setText("Cuenta no disponible");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Error al obtener los datos del paciente", Toast.LENGTH_SHORT).show();
                                });

                        // Agregar funcionalidad a los botones
                        btnRealizada.setOnClickListener(v -> {
                            // Crear un intent para navegar al nuevo Activity
                            Intent intent = new Intent(getContext(), InformeMedicoActivity.class);

                            // Pasar datos a través del intent
                            intent.putExtra("usuarioId", usuarioId);  // ID del usuario asociado a la cita
                            intent.putExtra("numeroCuenta", txtCuenta.getText().toString()); // Número de cuenta del paciente
                            intent.putExtra("citaId", document.getId()); // ID único de la cita
                            intent.putExtra("nombreDoctor", nombreDoctor); // Nombre del doctor
                            intent.putExtra("nombre", txtNombre.getText().toString()); // Nombre del paciente


                            // Iniciar el nuevo Activity
                            startActivity(intent);
                        });

                        // Agregar funcionalidad al botón de cancelación
                        btnCancelada.setOnClickListener(v -> {
                            // Obtener el ID de la cita (document)
                            String citaId = document.getId();  // Aquí obtenemos el ID correcto

                            // Actualizar el estado de la cita a "cancelada"
                            db.collection("citas").document(citaId)
                                    .update("estado", "cancelada")
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "Cita cancelada", Toast.LENGTH_SHORT).show();
                                        // Recargar las citas para reflejar el cambio
                                        cargarCitasProximas(linearCitas, nombreDoctor);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Error al cancelar la cita", Toast.LENGTH_SHORT).show();
                                    });
                        });

                        // Agregar la vista inflada al LinearLayout
                        linearCitas.addView(citaView);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar citas", Toast.LENGTH_SHORT).show();
                });
    }

    // Clase Cita para almacenar los datos de cada cita
    class Cita {
        private String horario;
        private String usuarioId;

        public Cita(String horario, String usuarioId) {
            this.horario = horario;
            this.usuarioId = usuarioId;
        }

        public String getHorario() {
            return horario;
        }

        public String getUsuarioId() {
            return usuarioId;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
