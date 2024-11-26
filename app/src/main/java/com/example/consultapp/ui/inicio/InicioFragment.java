package com.example.consultapp.ui.inicio;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.consultapp.AgendaActivity;
import com.example.consultapp.PerfilEspecialidadActivity;
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
    private LinearLayout linearProxCitas, linearSerivicios;
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
        ImageButton imageButton = binding.image;
        linearSerivicios = root.findViewById(R.id.linearServicios);
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
                        String fotoUserUrl = snapshot.child("fotoPerfil").getValue(String.class); // Asume que el nodo "foto" tiene la URL
                        if (nombreUsuario != null) {
                            textSaludo.setText("Hola, " + nombreUsuario);
                            // Cargar la foto del doctor en el ImageButton
                            if (fotoUserUrl != null && !fotoUserUrl.isEmpty()) {
                                Glide.with(requireContext())
                                        .load(fotoUserUrl)
                                        .transform(new RoundedCorners(150)) // Redondear las esquinas con un radio de 16dp
                                        .placeholder(R.drawable.round_person_outline_24)
                                        .into(imageButton);
                            }
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
        cargarServicios();

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
                .addValueEventListener(new ValueEventListener() { // Cambiar a ValueEventListener
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        linearProxCitas.removeAllViews(); // Limpiar citas previas para evitar duplicados
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

    private void cargarServicios() {
        DatabaseReference serviciosRef = databaseReference.child("Servicios").child("DetalleServicios");

        serviciosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.linearServicios.removeAllViews(); // Limpia el contenido previo
                LinearLayout fila = null;
                int contador = 0;

                for (DataSnapshot servicioSnapshot : snapshot.getChildren()) {
                    if (contador >= 4) break;  // Limita a 4 servicios

                    String nombre = servicioSnapshot.child("nombre").getValue(String.class);
                    String imagenUrl = servicioSnapshot.child("imagenUrl").getValue(String.class); // Suponiendo que la URL está almacenada en "imagenUrl"

                    View servicioView = LayoutInflater.from(getContext()).inflate(R.layout.item_servicio, null);
                    TextView nombreServicio = servicioView.findViewById(R.id.nombre_servicio);
                    ImageView imagenServicio = servicioView.findViewById(R.id.imagen_servicio); // Obtener el ImageView

                    nombreServicio.setText(nombre);

                    // Cargar la imagen usando Glide
                    if (imagenUrl != null) {
                        Glide.with(getContext())
                                .load(imagenUrl)
                                .into(imagenServicio);
                    }

                    // Establecer el OnClickListener para redirigir a AgendaActivity
                    servicioView.setOnClickListener(v -> {
                        // Crear un Intent para redirigir a AgendaActivity
                        Intent intent = new Intent(getContext(), AgendaActivity.class);
                        intent.putExtra("nombre_servicio", nombre); // Pasar el nombre del servicio (si lo necesitas)
                        startActivity(intent);
                    });

                    // Crear una fila nueva si es necesario
                    if (contador % 2 == 0) {
                        fila = new LinearLayout(getContext());
                        fila.setOrientation(LinearLayout.HORIZONTAL);
                        fila.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        fila.setPadding(0, 0, 0, 0); // Margen entre filas
                        binding.linearServicios.addView(fila);
                    }

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1
                    );
                    params.setMargins(0, 0, 40, 50); // Margen entre los elementos
                    servicioView.setLayoutParams(params);

                    fila.addView(servicioView);
                    contador++;
                }

                // Si solo hay un servicio, agregar una vista vacía al final
                if (snapshot.getChildrenCount() == 1) {
                    LinearLayout.LayoutParams paramsVacia = new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1
                    );
                    View viewVacia = new View(getContext());
                    viewVacia.setLayoutParams(paramsVacia);
                    fila.addView(viewVacia);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
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
