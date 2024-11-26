package com.example.consultapp.ui.calendario;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.consultapp.InformeMedicoActivity;
import com.example.consultapp.PerfilDoc;
import com.example.consultapp.R;
import com.example.consultapp.databinding.FragmentCalendarioBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

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

        // Inicializar Firebase Realtime Database y Auth
        dbRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        // Obtener referencias de vistas
        TextView textSaludo = binding.textSaludo;
        ImageButton imageButton = binding.image;
        LinearLayout linearCitas = binding.linearCitas;

        // Obtener la referencia del CalendarView y el TextView donde mostrar la fecha
        CalendarView calendarView = binding.calendarView;
        TextView textPrueba = binding.textPrueba;

        // Establecer la fecha actual en el CalendarView
        Calendar calendar = Calendar.getInstance();
        long currentDate = calendar.getTimeInMillis();
        calendarView.setDate(currentDate, true, true);

        // Mostrar la fecha actual en textPrueba
        textPrueba.setText("Fecha seleccionada: " + calendar.get(Calendar.DAY_OF_MONTH) + "/" +
                (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR));

        // Configurar el listener para cambios de fecha en CalendarView
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Mostrar la fecha seleccionada
            textPrueba.setText("Fecha seleccionada: " + dayOfMonth + "/" + (month + 1) + "/" + year);
        });

        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();

            // Referencia al nodo del médico en Realtime Database
            dbRef.child("Medicos").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String nombreDoctor = snapshot.child("nombre").getValue(String.class);
                        String fotoDoctorUrl = snapshot.child("fotoPerfil").getValue(String.class); // Asume que el nodo "foto" tiene la URL

                        if (nombreDoctor != null) {
                            textSaludo.setText("Hola, " + nombreDoctor);
                            // Cargar la foto del doctor en el ImageButton
                            if (fotoDoctorUrl != null && !fotoDoctorUrl.isEmpty()) {
                                Glide.with(requireContext())
                                        .load(fotoDoctorUrl)
                                        .transform(new RoundedCorners(150)) // Redondear las esquinas con un radio de 16dp
                                        .placeholder(R.drawable.round_person_outline_24)
                                        .into(imageButton);
                            }
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
                        linearCitas.removeAllViews(); // Limpiar cualquier vista previa en linearCitas

                        for (DataSnapshot citaSnapshot : snapshot.getChildren()) {
                            String doctor = citaSnapshot.child("doctor").getValue(String.class);
                            String fechaCita = citaSnapshot.child("fecha").getValue(String.class); // Asume que la fecha está guardada en el campo "fecha"

                            if (nombreDoctor.equals(doctor)) {
                                String horario = citaSnapshot.child("horario").getValue(String.class);
                                String usuarioId = citaSnapshot.child("usuario_id").getValue(String.class);
                                String citaId = citaSnapshot.getKey();

                                // Inflar el layout de la cita
                                View citaView = getLayoutInflater().inflate(R.layout.item_cita_dia, linearCitas, false);

                                // Referencias de los elementos
                                TextView txtCuenta = citaView.findViewById(R.id.txtCuenta);
                                TextView txtNombre = citaView.findViewById(R.id.txtNombre);
                                TextView txtHorario = citaView.findViewById(R.id.txtHorario);
                                ImageButton btnRealizada = citaView.findViewById(R.id.btn_realizada);
                                ImageButton btnCancelada = citaView.findViewById(R.id.btn_cancelada);

                                txtHorario.setText(horario);

                                // Consultar información del paciente
                                dbRef.child("users").child(usuarioId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                        if (userSnapshot.exists()) {
                                            String nombrePaciente = userSnapshot.child("nombre").getValue(String.class);
                                            String numeroCuenta = userSnapshot.child("numeroCuenta").getValue(String.class);

                                            txtCuenta.setText(numeroCuenta != null ? numeroCuenta : "Cuenta no disponible");
                                            txtNombre.setText(nombrePaciente != null ? nombrePaciente : "Paciente no encontrado");
                                        } else {
                                            txtCuenta.setText("Cuenta no disponible");
                                            txtNombre.setText("Paciente no encontrado");
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
                                    intent.putExtra("fechaCita", fechaCita); // Aquí enviamos la fecha de la cita
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

                                // Agregar la vista inflada al LinearLayout
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
