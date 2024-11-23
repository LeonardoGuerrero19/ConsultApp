package com.example.consultapp.ui.citas;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.consultapp.Cita;
import com.example.consultapp.CitasAdapter;
import com.example.consultapp.PerfilDoc;
import com.example.consultapp.R;
import com.example.consultapp.databinding.FragmentCitasBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CitasFragment extends Fragment {

    private FragmentCitasBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private Spinner spinnerEspecializacion;
    private RecyclerView recyclerViewCitas;
    private CitasAdapter adapter;
    private List<Cita> citasList;

    // Referencia al botón seleccionado
    private Button selectedButton;
    private Button btnProximas, btnRealizadas, btnCanceladas;
    private String servicioSeleccionado = "";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CitasViewModel citasViewModel =
                new ViewModelProvider(this).get(CitasViewModel.class);

        binding = FragmentCitasBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializar Firestore y Auth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Obtener referencias de vistas
        TextView textSaludo = binding.textSaludo;
        ImageButton imageButton = binding.image;

        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Inicializar RecyclerView
        recyclerViewCitas = binding.recyclerViewCitas;
        recyclerViewCitas.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializar lista y adaptador
        citasList = new ArrayList<>();
        adapter = new CitasAdapter(citasList);
        recyclerViewCitas.setAdapter(adapter);

        // Referencias a los botones
        btnProximas = binding.btnProximas;
        btnRealizadas = binding.btnRealizadas;
        btnCanceladas = binding.btnCanceladas;

        // Listener de botones
        btnProximas.setOnClickListener(v -> {
            cargarCitas("proxima");
            updateSelectedButton(btnProximas);
        });

        btnRealizadas.setOnClickListener(v -> {
            cargarCitas("realizada");
            updateSelectedButton(btnRealizadas);
        });

        btnCanceladas.setOnClickListener(v -> {
            cargarCitas("cancelada");
            updateSelectedButton(btnCanceladas);
        });

        // Spinner de especializaciones
        spinnerEspecializacion = binding.spinnerEspecializacion;
        cargarEspecializaciones();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Referencia al documento del médico
            db.collection("user").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String nombreAdmin = documentSnapshot.getString("nombre");
                            if (nombreAdmin != null) {
                                textSaludo.setText("Hola, " + nombreAdmin);
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

    private void cargarEspecializaciones() {
        db.collection("Servicios").document("Todos los servicios").get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> especializaciones = (List<String>) documentSnapshot.get("Servicios");
                        if (especializaciones != null && !especializaciones.isEmpty()) {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    getContext(),
                                    R.layout.item_spinner,
                                    especializaciones
                            );
                            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                            spinnerEspecializacion.setAdapter(adapter);

                            spinnerEspecializacion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    servicioSeleccionado = especializaciones.get(position);

                                    // Cargar citas de tipo "Próximas" al cambiar servicio
                                    cargarCitas("proxima");
                                    updateSelectedButton(btnProximas); // Asegurar que "Próximas" esté seleccionado
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            });
                        } else {
                            Toast.makeText(getContext(), "No hay especializaciones disponibles", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "No se encontró el documento de servicios", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar especializaciones", Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarCitas(String estado) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("citas")
                    .whereEqualTo("estado", estado)
                    .whereEqualTo("servicio", servicioSeleccionado)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            citasList.clear();
                            for (DocumentSnapshot document : task.getResult()) {
                                Cita cita = document.toObject(Cita.class);
                                citasList.add(cita);
                            }
                            adapter.notifyDataSetChanged();

                            // Mostrar mensaje si no hay citas
                            if (citasList.isEmpty()) {
                                Toast.makeText(getContext(), "No hay citas para el estado seleccionado", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "Error al cargar citas", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateSelectedButton(Button button) {
        // Restablecer el color del botón previamente seleccionado
        if (selectedButton != null) {
            selectedButton.setBackgroundTintList(getContext().getColorStateList(R.color.gray));
            selectedButton.setTextColor(getContext().getColor(R.color.black));
        }

        // Cambiar el color del nuevo botón seleccionado
        selectedButton = button;
        selectedButton.setBackgroundTintList(getContext().getColorStateList(R.color.aqua)); // Color seleccionado
        selectedButton.setTextColor(getContext().getColor(R.color.white));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
