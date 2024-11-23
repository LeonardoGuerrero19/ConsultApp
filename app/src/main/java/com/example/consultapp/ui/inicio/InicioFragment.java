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
import androidx.core.content.ContextCompat;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
                                String doctor = document.getString("doctor");
                                String fecha = document.getString("fecha");
                                String horario = document.getString("horario");

                                // Crear y agregar un TextView para cada cita válida
                                agregarTextoCita(servicio, doctor, fecha, horario);
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
        tvFecha.setText(fechaFormateada); // Usar la fecha formateada
        tvHorario.setText(horario);

        // Agregar el diseño inflado al LinearLayout
        linearProxCitas.addView(citaView);
    }

    private String formatearFecha(String fechaOriginal) {
        // Formato de la fecha que recibes (por ejemplo: "21/11/2024")
        SimpleDateFormat formatoEntrada = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        // Formato al que quieres convertir (por ejemplo: "21 de Noviembre de 2024")
        SimpleDateFormat formatoSalida = new SimpleDateFormat("d 'de' MMMM", new Locale("es", "ES"));

        try {
            Date fecha = formatoEntrada.parse(fechaOriginal); // Parsear la fecha original
            return formatoSalida.format(fecha); // Convertir al formato deseado
        } catch (ParseException e) {
            e.printStackTrace();
            return fechaOriginal; // En caso de error, devolver la fecha original
        }
    }

    private void obtenerServicios() {
        DocumentReference serviciosRef = db.collection("Servicios").document("Todos los servicios");

        serviciosRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    List<String> servicios = (List<String>) document.get("Servicios");

                    if (servicios != null) {
                        for (String servicio : servicios) {
                            // Inflar el diseño del botón
                            View servicioView = LayoutInflater.from(getContext()).inflate(R.layout.item_button_service, gridLayout, false);

                            // Referenciar el botón dentro del diseño inflado
                            Button btnService = servicioView.findViewById(R.id.servicioButton);
                            btnService.setText(servicio);

                            // Configurar el layoutParams para respetar el ancho del GridLayout
                            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                            params.width = 0; // Ancho dinámico
                            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); // Peso igual para las columnas
                            params.setMargins(8, 8, 8, 8); // Márgenes opcionales
                            servicioView.setLayoutParams(params);

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
                    Toast.makeText(getContext(), "Error al cargar los servicios", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
