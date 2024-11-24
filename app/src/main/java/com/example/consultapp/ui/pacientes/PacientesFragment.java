package com.example.consultapp.ui.pacientes;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.consultapp.InformePacienteActivity;
import com.example.consultapp.R;
import com.example.consultapp.databinding.FragmentPacientesBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PacientesFragment extends Fragment {

    private FragmentPacientesBinding binding;
    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inicializar el ViewModel y el binding
        PacientesViewModel pacientesViewModel =
                new ViewModelProvider(this).get(PacientesViewModel.class);
        binding = FragmentPacientesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializar Firebase Auth y Realtime Database
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("users");

        // Referencias a elementos de la interfaz
        EditText etNumeroCuenta = binding.etNumeroCuenta;
        ImageButton btnBuscar = binding.btnBuscar;
        LinearLayout linearPacientes = binding.linearPacientes;

        // Configurar el listener para el botón Buscar
        btnBuscar.setOnClickListener(v -> {
            String numeroCuenta = etNumeroCuenta.getText().toString().trim();

            // Validar que el campo no esté vacío
            if (TextUtils.isEmpty(numeroCuenta)) {
                Toast.makeText(getContext(), "Ingrese un número de cuenta", Toast.LENGTH_SHORT).show();
            } else {
                // Redirigir a InformePacienteActivity con el número de cuenta
                Intent intent = new Intent(getActivity(), InformePacienteActivity.class);
                intent.putExtra("numeroCuenta", numeroCuenta);
                startActivity(intent);
            }
        });

        // Configurar Firebase Database para mostrar datos en el LinearLayout
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                linearPacientes.removeAllViews(); // Limpiar la vista antes de agregar datos nuevos
                LayoutInflater inflater = LayoutInflater.from(getContext());

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String rol = userSnapshot.child("rol").getValue(String.class);
                    String nombre = userSnapshot.child("nombre").getValue(String.class);
                    String noCuenta = userSnapshot.child("numeroCuenta").getValue(String.class);

                    // Filtrar por rol "usuario"
                    if ("usuario".equalsIgnoreCase(rol)) {
                        // Inflar el diseño item_paciente
                        View itemView = inflater.inflate(R.layout.item_paciente, linearPacientes, false);

                        // Referenciar los elementos del diseño inflado
                        TextView tvNombre = itemView.findViewById(R.id.tvNombre);
                        TextView tvNumeroCuenta = itemView.findViewById(R.id.tvNumeroCuenta);
                        ImageButton btnMas = itemView.findViewById(R.id.btn_mas);

                        // Configurar los valores de los TextView
                        tvNombre.setText(nombre != null ? nombre : "Usuario sin nombre");
                        tvNumeroCuenta.setText(noCuenta != null ? noCuenta : "Usuario sin cuenta");

                        // Configurar el ImageButton para redirigir al Activity
                        btnMas.setOnClickListener(v -> {
                            Intent intent = new Intent(getActivity(), InformePacienteActivity.class);
                            intent.putExtra("nombre", nombre);
                            intent.putExtra("numeroCuenta", noCuenta);
                            startActivity(intent);
                        });

                        // Agregar el diseño inflado al LinearLayout
                        linearPacientes.addView(itemView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejar errores de Firebase
                Toast.makeText(getContext(), "Error al cargar datos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
