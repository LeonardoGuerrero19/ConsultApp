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
    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;
    private GridLayout gridLayout;
    private LinearLayout linearProxCitas;
    private Button btn_cerrarS;

    private ValueEventListener citasListener;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        InicioViewModel homeViewModel =
                new ViewModelProvider(this).get(InicioViewModel.class);

        binding = FragmentInicioBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializar Firebase Auth y Realtime Database
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        // Inicializar vistas
        TextView textSaludo = binding.textSaludo;
        gridLayout = root.findViewById(R.id.gridLayout);
        linearProxCitas = root.findViewById(R.id.linearProxCitas);

        // Configurar el botón para cerrar sesión
        btn_cerrarS = root.findViewById(R.id.btn_cerrarS);
        btn_cerrarS.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), login.class);
            startActivity(intent);
            getActivity().finish(); // Cierra la actividad actual
        });

        // Cargar los datos del usuario y citas
        cargarUsuarioYDatos();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            linearProxCitas.removeAllViews(); // Limpia las vistas existentes antes de recargar
            agregarListenerCitas(userId); // Actualiza las citas al regresar al fragmento
        }
    }


    private void cargarUsuarioYDatos() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Referencia al nodo del usuario en Realtime Database
            dbRef.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String nombreUsuario = snapshot.child("nombre").getValue(String.class);
                        if (nombreUsuario != null) {
                            binding.textSaludo.setText("Hola, " + nombreUsuario);
                        } else {
                            binding.textSaludo.setText("Hola, Usuario");
                        }
                    } else {
                        binding.textSaludo.setText("Hola, Usuario");
                        Toast.makeText(getContext(), "No se encontró el usuario en la base de datos", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    binding.textSaludo.setText("Hola, Usuario");
                    Toast.makeText(getContext(), "Error al obtener el nombre del usuario", Toast.LENGTH_SHORT).show();
                }
            });

            // Configurar un listener persistente para las citas
            agregarListenerCitas(userId);

            // Obtener y mostrar los servicios
            obtenerServicios();
        } else {
            binding.textSaludo.setText("Hola, Usuario");
            Toast.makeText(getContext(), "Usuario no logueado", Toast.LENGTH_SHORT).show();
        }
    }

    private void agregarListenerCitas(String userId) {
        DatabaseReference citasRef = dbRef.child("citas").orderByChild("usuario_id").equalTo(userId).getRef();

        citasListener = citasRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                linearProxCitas.removeAllViews(); // Limpiar las vistas anteriores
                if (snapshot.exists()) {
                    for (DataSnapshot citaSnapshot : snapshot.getChildren()) {
                        String estado = citaSnapshot.child("estado").getValue(String.class);
                        if ("proxima".equalsIgnoreCase(estado)) {
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
        // Inflar el diseño XML de la cita
        View citaView = LayoutInflater.from(getContext()).inflate(R.layout.item_prox_citas, linearProxCitas, false);

        // Referenciar los TextView dentro del diseño inflado
        TextView tvServicio = citaView.findViewById(R.id.tvServicio);
        TextView tvDoctor = citaView.findViewById(R.id.tvDoctor);
        TextView tvFecha = citaView.findViewById(R.id.tvFecha);
        TextView tvHorario = citaView.findViewById(R.id.tvHorario);

        // Convertir la fecha al formato deseado
        String fechaFormateada = formatearFecha(fecha);

        // Establecer los valores
        tvServicio.setText(servicio);
        tvDoctor.setText("Dr. " + doctor);
        tvFecha.setText(fechaFormateada);
        tvHorario.setText(horario);

        // Agregar el diseño inflado al LinearLayout
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
        dbRef.child("Servicios").child("TodosLosServicios")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        gridLayout.removeAllViews(); // Limpia las vistas previas
                        if (snapshot.exists()) {
                            for (DataSnapshot servicioSnapshot : snapshot.getChildren()) {
                                String servicio = servicioSnapshot.getValue(String.class);

                                if (servicio != null) {
                                    // Inflar el diseño del botón
                                    View servicioView = LayoutInflater.from(getContext()).inflate(R.layout.item_button_service, gridLayout, false);

                                    // Referenciar el botón dentro del diseño inflado
                                    Button btnService = servicioView.findViewById(R.id.btnService);
                                    btnService.setText(servicio);

                                    // Configurar acción del botón
                                    btnService.setOnClickListener(v -> {
                                        Intent intent = new Intent(getContext(), AgendaActivity.class);
                                        intent.putExtra("nombreServicio", servicio);
                                        startActivity(intent);
                                    });

                                    // Agregar la vista inflada al GridLayout
                                    gridLayout.addView(servicioView);
                                }
                            }
                        } else {
                            Toast.makeText(getContext(), "No hay servicios disponibles.", Toast.LENGTH_SHORT).show();
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
        if (citasListener != null) {
            dbRef.removeEventListener(citasListener);
        }
        binding = null;
    }
}
