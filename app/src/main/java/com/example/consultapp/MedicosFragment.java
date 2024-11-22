package com.example.consultapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MedicosFragment extends Fragment {

    private RecyclerView recyclerView;
    private PersonalMedicoAdapter adapter;
    private List<Medico> medicos;
    private DatabaseReference databaseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_medicos, container, false);

        // Configura el RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewDoctores);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        medicos = new ArrayList<>();
        adapter = new PersonalMedicoAdapter(medicos, requireContext());
        recyclerView.setAdapter(adapter);

        // Configurar el FloatingActionButton
        FloatingActionButton floatingActionButton = view.findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(v -> {
            // Iniciar la nueva actividad
            Intent intent = new Intent(getActivity(), RegistroDoctor.class);
            startActivity(intent);
        });

        // Inicializar la referencia a Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarMedicos(); // Recargar datos al volver al fragmento
    }

    private void cargarMedicos() {
        databaseReference.child("Medicos").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                medicos.clear(); // Limpia la lista antes de agregar nuevos datos
                for (DataSnapshot medicoSnapshot : snapshot.getChildren()) {
                    Medico medico = medicoSnapshot.getValue(Medico.class);
                    if (medico != null) {
                        medico.setId(medicoSnapshot.getKey());
                        medicos.add(medico);
                    }
                }
                adapter.notifyDataSetChanged(); // Notifica al adaptador que los datos han cambiado
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejo de errores
            }
        });
    }
}
