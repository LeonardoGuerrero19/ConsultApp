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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InicioFragment extends Fragment {

    private FragmentInicioBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private GridLayout gridLayout;
    private LinearLayout linearProxCitas;
    private Button btn_cerrarS;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        InicioViewModel homeViewModel =
                new ViewModelProvider(this).get(InicioViewModel.class);

        binding = FragmentInicioBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializar FirebaseAuth y Realtime Database
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Inicializar vistas
        TextView textSaludo = binding.textSaludo;
        gridLayout = root.findViewById(R.id.gridLayout);
        linearProxCitas = root.findViewById(R.id.linearProxCitas);

        // Obtener el usuario actual de FirebaseAuth
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Referencia al nodo del usuario en Realtime Database
            databaseReference.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String nombreUsuario = snapshot.child("nombre").getValue(String.class);
                        if (nombreUsuario != null) {
                            textSaludo.setText("Hola, " + nombreUsuario);
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
        databaseReference.child("citas").orderByChild("usuario_id").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot citaSnapshot : snapshot.getChildren()) {
                                String estado = citaSnapshot.child("estado").getValue(String.class);
                                if ("proxima".equalsIgnoreCase(estado)) { // Verificar si el estado es "próxima"
                                    String servicio = citaSnapshot.child("servicio").getValue(String.class);
                                    String doctor = citaSnapshot.child("doctor").getValue(String.class);
                                    String fecha = citaSnapshot.child("fecha").getValue(String.class);
                                    String horario = citaSnapshot.child("horario").getValue(String.class);

                                    // Crear y agregar un TextView para cada cita válida
                                    agregarTextoCita(servicio, doctor, fecha, horario);
                                }
                            }
                        } else {
                            Toast.makeText(getContext(), "No tienes citas próximas.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Error al cargar las citas.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void agregarTextoCita(String servicio, String doctor, String fecha, String horario) {
        View citaView = LayoutInflater.from(getContext()).inflate(R.layout.item_prox_citas, linearProxCitas, false);

        TextView tvServicio = citaView.findViewById(R.id.tvServicio);
        TextView tvDoctor = citaView.findViewById(R.id.tvDoctor);
        TextView tvFecha = citaView.findViewById(R.id.tvFecha);
        TextView tvHorario = citaView.findViewById(R.id.tvHorario);

        String fechaFormateada = formatearFecha(fecha);

        tvServicio.setText(servicio);
        tvDoctor.setText("Dr. " + doctor);
        tvFecha.setText(fechaFormateada);
        tvHorario.setText(horario);

        linearProxCitas.addView(citaView);
    }

    private String formatearFecha(String fechaOriginal) {
        SimpleDateFormat formatoEntrada = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat formatoSalida = new SimpleDateFormat("d 'de' MMMM", new Locale("es", "ES"));

        try {
            Date fecha = formatoEntrada.parse(fechaOriginal);
            return formatoSalida.format(fecha);
        } catch (ParseException e) {
            e.printStackTrace();
            return fechaOriginal;
        }
    }

    private void obtenerServicios() {
        databaseReference.child("Servicios").child("TodosLosServicios").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot servicioSnapshot : snapshot.getChildren()) {
                        String servicio = servicioSnapshot.getValue(String.class);

                        if (servicio != null) {
                            View servicioView = LayoutInflater.from(getContext()).inflate(R.layout.item_button_service, gridLayout, false);

                            Button btnService = servicioView.findViewById(R.id.servicioButton);
                            btnService.setText(servicio);

                            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                            params.width = 0;
                            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                            params.setMargins(8, 8, 8, 8);
                            servicioView.setLayoutParams(params);

                            btnService.setOnClickListener(v -> {
                                Intent intent = new Intent(getContext(), AgendaActivity.class);
                                intent.putExtra("nombreServicio", servicio);
                                startActivity(intent);
                            });

                            gridLayout.addView(servicioView);
                        }
                    }
                } else {
                    Toast.makeText(getContext(), "Error al cargar los servicios", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error al cargar los servicios.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
