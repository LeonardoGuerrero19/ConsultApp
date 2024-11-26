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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CalendarioFragment extends Fragment {

    private FragmentCalendarioBinding binding;
    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;
    // Variable para almacenar la fecha seleccionada
    private String selectedDate;

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

        // Establecer la fecha actual en el CalendarView
        Calendar calendar = Calendar.getInstance();
        long currentDate = calendar.getTimeInMillis();
        calendarView.setDate(currentDate, true, true);

        // Mostrar la fecha actual en textPrueba
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        // Configurar el listener para cambios de fecha en CalendarView
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Crear la fecha seleccionada en formato dd/MM/yyyy
            selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;

            // Ahora puedes usar la variable selectedDate en cualquier lugar de tu fragmento
            cargarCitasPorFecha(linearCitas, selectedDate);
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
                            // Cargar las citas para la fecha seleccionada
                            cargarCitasPorFecha(linearCitas, sdf.format(new Date(currentDate)));
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

    private void cargarCitasPorFecha(LinearLayout linearCitas, String selectedDate) {
        // Obtener la fecha actual
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String todayDate = sdf.format(new Date());

        dbRef.child("citas").orderByChild("estado").equalTo("proxima")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        linearCitas.removeAllViews(); // Limpiar cualquier vista previa en linearCitas

                        for (DataSnapshot citaSnapshot : snapshot.getChildren()) {
                            String doctor = citaSnapshot.child("doctor").getValue(String.class);
                            String fechaCita = citaSnapshot.child("fecha").getValue(String.class); // Asume que la fecha está guardada en el campo "fecha"

                            if (fechaCita != null && fechaCita.equals(selectedDate) && doctor != null) {
                                // Aquí comprobamos que la fecha de la cita coincida con la seleccionada
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

                                // Si la fecha seleccionada no es hoy, ocultamos los botones
                                if (!selectedDate.equals(todayDate)) {
                                    btnRealizada.setVisibility(View.GONE);
                                    btnCancelada.setVisibility(View.GONE);
                                }

                                // Configurar botones
                                btnRealizada.setOnClickListener(v -> {
                                    Intent intent = new Intent(getContext(), InformeMedicoActivity.class);
                                    intent.putExtra("usuarioId", usuarioId);
                                    intent.putExtra("numeroCuenta", txtCuenta.getText().toString());
                                    intent.putExtra("citaId", citaId);
                                    intent.putExtra("nombreDoctor", doctor);
                                    intent.putExtra("fechaCita", fechaCita); // Aquí enviamos la fecha de la cita
                                    intent.putExtra("nombre", txtNombre.getText().toString());
                                    startActivity(intent);
                                });

                                btnCancelada.setOnClickListener(v -> {
                                    dbRef.child("citas").child(citaId).child("estado").setValue("cancelada")
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(getContext(), "Cita cancelada", Toast.LENGTH_SHORT).show();
                                                cargarCitasPorFecha(linearCitas, selectedDate);
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(getContext(), "Error al cancelar la cita", Toast.LENGTH_SHORT).show();
                                            });
                                });

                                // Agregar la vista de la cita al LinearLayout
                                linearCitas.addView(citaView);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Error al cargar las citas", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
