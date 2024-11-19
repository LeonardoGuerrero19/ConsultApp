package com.example.consultapp.ui.calendario_user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.consultapp.Cita;
import com.example.consultapp.CitasAdapter;
import com.example.consultapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CalendarioUserFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerViewCitas;
    private CitasAdapter adapter;
    private List<Cita> citasList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_calendario_user, container, false);

        // Inicializar Firestore y Auth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Inicializar RecyclerView
        recyclerViewCitas = root.findViewById(R.id.recyclerView_citas);
        recyclerViewCitas.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializar lista y adaptador
        citasList = new ArrayList<>();
        adapter = new CitasAdapter(citasList);
        recyclerViewCitas.setAdapter(adapter);

        // Configurar botón Proximas
        Button btnProximas = root.findViewById(R.id.btn_proximas);
        btnProximas.setOnClickListener(v -> cargarCitasProximas());

        // Configurar botón Realizadas
        Button btnRealizadas = root.findViewById(R.id.btn_realizadas);
        btnRealizadas.setOnClickListener(v -> cargarCitasRealizadas());

        // Configurar botón Realizadas
        Button btnCanceladas = root.findViewById(R.id.btn_canceladas);
        btnCanceladas.setOnClickListener(v -> cargarCitasCanceladas());

        return root;
    }

    private void cargarCitasProximas() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Filtrar citas con estado "proxima"
            db.collection("citas")
                    .whereEqualTo("usuario_id", userId)
                    .whereEqualTo("estado", "proxima")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            citasList.clear(); // Limpiar la lista antes de agregar nuevos elementos
                            for (DocumentSnapshot document : task.getResult()) {
                                Cita cita = document.toObject(Cita.class);
                                citasList.add(cita);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getContext(), "Error al cargar citas", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void cargarCitasRealizadas() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Filtrar citas con estado "realizada"
            db.collection("citas")
                    .whereEqualTo("usuario_id", userId)
                    .whereEqualTo("estado", "realizada")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            citasList.clear(); // Limpiar la lista antes de agregar nuevos elementos
                            for (DocumentSnapshot document : task.getResult()) {
                                Cita cita = document.toObject(Cita.class);
                                citasList.add(cita);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getContext(), "Error al cargar citas", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void cargarCitasCanceladas() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Filtrar citas con estado "realizada"
            db.collection("citas")
                    .whereEqualTo("usuario_id", userId)
                    .whereEqualTo("estado", "cancelada")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            citasList.clear(); // Limpiar la lista antes de agregar nuevos elementos
                            for (DocumentSnapshot document : task.getResult()) {
                                Cita cita = document.toObject(Cita.class);
                                citasList.add(cita);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getContext(), "Error al cargar citas", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
