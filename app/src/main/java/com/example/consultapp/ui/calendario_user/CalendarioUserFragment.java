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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CalendarioUserFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private RecyclerView recyclerViewCitas;
    private CitasAdapter adapter;
    private List<Cita> citasList;

    // Referencia al botón seleccionado
    private Button selectedButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_calendario_user, container, false);

        // Inicializar FirebaseAuth y Realtime Database
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Inicializar RecyclerView
        recyclerViewCitas = root.findViewById(R.id.recyclerView_citas);
        recyclerViewCitas.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializar lista y adaptador
        citasList = new ArrayList<>();
        adapter = new CitasAdapter(citasList);
        recyclerViewCitas.setAdapter(adapter);

        // Referencias a los botones
        Button btnProximas = root.findViewById(R.id.btn_proximas);
        Button btnRealizadas = root.findViewById(R.id.btn_realizadas);
        Button btnCanceladas = root.findViewById(R.id.btn_canceladas);

        // Configurar botones con cambio de color
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

        // Establecer un botón predeterminado seleccionado y cargar citas
        updateSelectedButton(btnProximas);
        cargarCitas("proxima"); // Cargar citas automáticamente al inicio

        return root;
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

    private void cargarCitas(String estado) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            databaseReference.child("citas").orderByChild("usuario_id").equalTo(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            citasList.clear();
                            for (DataSnapshot citaSnapshot : snapshot.getChildren()) {
                                Cita cita = citaSnapshot.getValue(Cita.class);
                                if (cita != null && cita.getEstado().equals(estado)) {
                                    citasList.add(cita);
                                }
                            }
                            adapter.notifyDataSetChanged();

                            if (citasList.isEmpty()) {
                                Toast.makeText(getContext(), "No hay citas para el estado seleccionado", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "Error al cargar citas", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
