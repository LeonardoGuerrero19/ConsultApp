package com.example.consultapp.ui.expedientes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.consultapp.R;
import com.example.consultapp.databinding.FragmentExpedientesBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;
import java.util.Set;

public class ExpedientesFragment extends Fragment {

    private FragmentExpedientesBinding binding;
    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ExpedientesViewModel expedientesViewModel =
                new ViewModelProvider(this).get(ExpedientesViewModel.class);

        binding = FragmentExpedientesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializar Firebase Auth y Realtime Database
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        // Obtener referencia al LinearLayout donde mostrar los informes
        LinearLayout linearExpedientes = binding.linearExpedientes;

        String userId = mAuth.getCurrentUser().getUid();

        // Referencia al nodo del médico
        dbRef.child("Medicos").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String nombreDoctor = snapshot.child("nombre").getValue(String.class);
                    if (nombreDoctor != null) {
                        // Cargar los informes del doctor logueado
                        cargarInformes(linearExpedientes, nombreDoctor);
                    } else {
                        Toast.makeText(getContext(), "No se encontró el nombre del doctor", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "No se encontró el usuario en la base de datos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error al obtener los datos del doctor", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    private void cargarInformes(LinearLayout linearExpedientes, String nombreDoctor) {
        dbRef.child("informe").orderByChild("nombreDoctor").equalTo(nombreDoctor)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Limpiar cualquier vista previa en linearExpedientes
                        linearExpedientes.removeAllViews();

                        // Crear un conjunto para almacenar los nombres de los pacientes ya mostrados
                        Set<String> pacientesMostrados = new HashSet<>();

                        for (DataSnapshot informeSnapshot : snapshot.getChildren()) {
                            String nombrePaciente = informeSnapshot.child("nombre").getValue(String.class);
                            String numeroCuenta = informeSnapshot.child("numeroCuenta").getValue(String.class);

                            if (nombrePaciente != null && !pacientesMostrados.contains(nombrePaciente)) {
                                // Inflar el layout para el primer informe de este paciente
                                View expedienteView = getLayoutInflater().inflate(R.layout.item_expediente, linearExpedientes, false);

                                // Obtener referencias a los elementos del layout inflado
                                TextView txtNombrePaciente = expedienteView.findViewById(R.id.txtNombrePaciente);
                                TextView txtNumeroCuenta = expedienteView.findViewById(R.id.txtNumeroCuenta);

                                // Establecer los valores del informe
                                txtNombrePaciente.setText(nombrePaciente);
                                txtNumeroCuenta.setText(numeroCuenta);

                                // Agregar el informe al LinearLayout
                                linearExpedientes.addView(expedienteView);

                                // Agregar el nombre del paciente al conjunto para evitar duplicados
                                pacientesMostrados.add(nombrePaciente);
                            }
                        }

                        if (pacientesMostrados.isEmpty()) {
                            Toast.makeText(getContext(), "No hay informes para este doctor", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Error al cargar los informes", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
