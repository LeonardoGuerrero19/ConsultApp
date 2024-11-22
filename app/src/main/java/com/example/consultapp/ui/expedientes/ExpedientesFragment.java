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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashSet;
import java.util.Set;

public class ExpedientesFragment extends Fragment {

    private FragmentExpedientesBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ExpedientesViewModel expedientesViewModel =
                new ViewModelProvider(this).get(ExpedientesViewModel.class);

        binding = FragmentExpedientesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializar Firestore y Auth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Obtener referencia al LinearLayout donde mostrar los informes
        LinearLayout linearExpedientes = binding.linearExpedientes;

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Referencia al documento del médico
            db.collection("medicos").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String nombreDoctor = documentSnapshot.getString("nombre");
                            if (nombreDoctor != null) {
                                // Cargar los informes del doctor logueado
                                cargarInformes(linearExpedientes, nombreDoctor);
                            } else {
                                Toast.makeText(getContext(), "No se encontró el nombre del doctor", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "No se encontró el usuario en la base de datos", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error al obtener los datos del doctor", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(getContext(), "Usuario no logueado", Toast.LENGTH_SHORT).show();
        }

        return root;
    }

    private void cargarInformes(LinearLayout linearExpedientes, String nombreDoctor) {
        db.collection("informe")
                .whereEqualTo("nombreDoctor", nombreDoctor) // Filtrar por nombreDoctor
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Limpiar cualquier vista previa en linearExpedientes
                    linearExpedientes.removeAllViews();

                    // Crear un conjunto para almacenar los nombres de los pacientes ya mostrados
                    Set<String> pacientesMostrados = new HashSet<>();

                    // Obtener los informes
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String nombrePaciente = document.getString("nombre");
                        String numeroCuenta = document.getString("numeroCuenta");

                        // Verificar si el paciente ya ha sido mostrado
                        if (!pacientesMostrados.contains(nombrePaciente)) {
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
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar los informes", Toast.LENGTH_SHORT).show();
                });
    }

}
