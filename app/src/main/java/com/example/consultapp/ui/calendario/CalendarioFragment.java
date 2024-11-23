package com.example.consultapp.ui.calendario;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CalendarioFragment extends Fragment {

    private FragmentCalendarioBinding binding;
    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CalendarioViewModel calendarioViewModel =
                new ViewModelProvider(this).get(CalendarioViewModel.class);

        binding = FragmentCalendarioBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializar Realtime Database y Auth
        dbRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        // Obtener referencias de vistas
        TextView textSaludo = binding.textSaludo;
        ImageButton imageButton = binding.image;
        LinearLayout linearCitas = binding.linearCitas;

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Referencia al nodo del médico
            dbRef.child("Medicos").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String nombreDoctor = snapshot.child("nombre").getValue(String.class);
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
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    textSaludo.setText("Hola, Usuario");
                    Toast.makeText(getContext(), "Error al obtener el nombre del usuario", Toast.LENGTH_SHORT).show();
                }
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
        dbRef.child("citas").orderByChild("estado").equalTo("proxima")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Limpiar cualquier vista previa en linearCitas
                        linearCitas.removeAllViews();

                        for (DataSnapshot citaSnapshot : snapshot.getChildren()) {
                            String doctor = citaSnapshot.child("doctor").getValue(String.class);
                            if (nombreDoctor.equals(doctor)) {
                                // Obtener datos de la cita
                                String horario = citaSnapshot.child("horario").getValue(String.class);
                                String usuarioId = citaSnapshot.child("usuario_id").getValue(String.class);
                                String citaId = citaSnapshot.getKey();

                                // Inflar el layout de la cita
                                View citaView = getLayoutInflater().inflate(R.layout.item_cita_dia, linearCitas, false);

                                // Obtener referencias de los elementos del layout inflado
                                TextView txtCuenta = citaView.findViewById(R.id.txtCuenta);
                                TextView txtNombre = citaView.findViewById(R.id.txtNombre);
                                TextView txtHorario = citaView.findViewById(R.id.txtHorario);
                                ImageButton btnRealizada = citaView.findViewById(R.id.btn_realizada);
                                ImageButton btnCancelada = citaView.findViewById(R.id.btn_cancelada);

                                // Establecer el horario
                                txtHorario.setText(horario);

                                // Consultar información del paciente
                                dbRef.child("users").child(usuarioId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                        if (userSnapshot.exists()) {
                                            String nombrePaciente = userSnapshot.child("nombre").getValue(String.class);
                                            String numeroCuenta = userSnapshot.child("numeroCuenta").getValue(String.class);

                                            // Actualizar vista
                                            txtCuenta.setText(numeroCuenta != null ? numeroCuenta : "Cuenta no disponible");
                                            txtNombre.setText(nombrePaciente != null ? nombrePaciente : "Paciente no encontrado");
                                        } else {
                                            txtNombre.setText("Paciente no encontrado");
                                            txtCuenta.setText("Cuenta no disponible");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(getContext(), "Error al obtener datos del paciente", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                // Configurar botones
                                btnRealizada.setOnClickListener(v -> {
                                    Intent intent = new Intent(getContext(), InformeMedicoActivity.class);
                                    intent.putExtra("usuarioId", usuarioId);
                                    intent.putExtra("numeroCuenta", txtCuenta.getText().toString());
                                    intent.putExtra("citaId", citaId);
                                    intent.putExtra("nombreDoctor", nombreDoctor);
                                    intent.putExtra("nombre", txtNombre.getText().toString());
                                    startActivity(intent);
                                });

                                btnCancelada.setOnClickListener(v -> {
                                    dbRef.child("citas").child(citaId).child("estado").setValue("cancelada")
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(getContext(), "Cita cancelada", Toast.LENGTH_SHORT).show();
                                                cargarCitasProximas(linearCitas, nombreDoctor);
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(getContext(), "Error al cancelar la cita", Toast.LENGTH_SHORT).show();
                                            });
                                });

                                // Agregar vista inflada al LinearLayout
                                linearCitas.addView(citaView);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Error al cargar citas", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
