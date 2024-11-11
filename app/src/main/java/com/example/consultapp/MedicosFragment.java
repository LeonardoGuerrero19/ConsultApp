package com.example.consultapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MedicosFragment extends Fragment {

    private RecyclerView recyclerView;
    private PersonalMedicoAdapter adapter; // Cambiado a PersonalMedicoAdapter
    private List<Medico> medicos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_medicos, container, false);

        // Configura el RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewDoctores);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        medicos = new ArrayList<>();
        adapter = new PersonalMedicoAdapter(medicos, requireContext()); // Pasa la lista de médicos y el contexto
        recyclerView.setAdapter(adapter);


        // Configurar el FloatingActionButton
        FloatingActionButton floatingActionButton = view.findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(v -> {
            // Iniciar la nueva actividad
            Intent intent = new Intent(getActivity(), RegistroDoctor.class);
            startActivity(intent);
        });

        // Obtener datos de Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user")
                .whereEqualTo("rol", "medico")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Medico> medicoList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Medico medico = document.toObject(Medico.class);
                        medico.setId(document.getId()); // Establece el ID del documento Firestore en el objeto Medico
                        medicoList.add(medico);
                    }
                    // Aquí configuras el adaptador con la lista de médicos
                    adapter = new PersonalMedicoAdapter(medicoList, requireContext());
                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    // Manejo de errores
                });


        return view;
    }
}
