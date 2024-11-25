package com.example.consultapp.ui.expedientes;

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

import com.example.consultapp.HistorialActivity;
import com.example.consultapp.InformePacienteActivity;
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

        // Referencias a elementos de la interfaz
        EditText etNumeroCuenta = binding.etNumeroCuenta;
        ImageButton btnBuscar = binding.btnBuscar;

        // Obtener referencia al LinearLayout donde mostrar los informes
        LinearLayout linearExpedientes = binding.linearExpedientes;

        String userId = mAuth.getCurrentUser().getUid();

        // Configurar el listener para el botón Buscar
        btnBuscar.setOnClickListener(v -> {
            String numeroCuenta = etNumeroCuenta.getText().toString().trim();

            // Validar que el campo no esté vacío
            if (TextUtils.isEmpty(numeroCuenta)) {
                Toast.makeText(getContext(), "Ingrese un número de cuenta", Toast.LENGTH_SHORT).show();
            } else {
                // Redirigir a InformePacienteActivity con el número de cuenta
                Intent intent = new Intent(getActivity(), HistorialActivity.class);
                intent.putExtra("numeroCuenta", numeroCuenta);
                startActivity(intent);
            }
        });

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
        dbRef.child("Informes").orderByChild("nombreDoctor").equalTo(nombreDoctor)
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
                            String peso = informeSnapshot.child("peso").getValue(String.class);
                            String altura = informeSnapshot.child("altura").getValue(String.class);
                            String alergias = informeSnapshot.child("alergias").getValue(String.class);

                            if (nombrePaciente != null && !pacientesMostrados.contains(nombrePaciente) && numeroCuenta != null) {
                                // Inflar el layout para el primer informe de este paciente
                                View expedienteView = getLayoutInflater().inflate(R.layout.item_expediente, linearExpedientes, false);
                                // Obtener referencias a los elementos del layout inflado
                                ImageButton btnHistorial = expedienteView.findViewById(R.id.btnHistorial); // Asegúrate de usar el ID correcto del ImageButton


                                // Obtener referencias a los elementos del layout inflado
                                TextView txtNombrePaciente = expedienteView.findViewById(R.id.txtNombrePaciente);
                                TextView txtNumeroCuenta = expedienteView.findViewById(R.id.txtNumeroCuenta);
                                LinearLayout modalDetalles = expedienteView.findViewById(R.id.modalDetalles);

                                TextView txtPeso = expedienteView.findViewById(R.id.peso);
                                TextView txtAltura = expedienteView.findViewById(R.id.altura);
                                TextView txtAlergias = expedienteView.findViewById(R.id.alergias);

                                TextView txtEdad = expedienteView.findViewById(R.id.edad);
                                TextView txtGenero = expedienteView.findViewById(R.id.genero);
                                TextView txtTelefono = expedienteView.findViewById(R.id.telefono);

                                // Establecer los valores básicos del informe
                                txtNombrePaciente.setText(nombrePaciente);
                                txtNumeroCuenta.setText(numeroCuenta);
                                txtPeso.setText(peso + " kg");
                                txtAltura.setText(altura + " cm");
                                txtAlergias.setText(alergias);

                                // Configura el evento de clic
                                btnHistorial.setOnClickListener(v -> {
                                    Intent intent = new Intent(getContext(), HistorialActivity.class);
                                    intent.putExtra("numeroCuenta", numeroCuenta); // Pasa el número de cuenta
                                    intent.putExtra("nombrePaciente", nombrePaciente); // Pasa el nombre del paciente
                                    intent.putExtra("peso", peso);
                                    intent.putExtra("altura", altura);
                                    intent.putExtra("alergias", alergias);
                                    // Puedes pasar más datos según sea necesario
                                    startActivity(intent);
                                });



                                // Consulta a "users" filtrando por numeroCuenta
                                dbRef.child("users").orderByChild("numeroCuenta").equalTo(numeroCuenta)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                                if (userSnapshot.exists()) {
                                                    // Recorrer los resultados de la consulta
                                                    for (DataSnapshot user : userSnapshot.getChildren()) {
                                                        String edad = user.child("edad").getValue(String.class);
                                                        String genero = user.child("genero").getValue(String.class);
                                                        String telefono = user.child("telefono").getValue(String.class);

                                                        // Establecer los valores en los TextView correspondientes
                                                        txtEdad.setText(edad != null ? edad : "No disponible");
                                                        txtGenero.setText(genero != null ? genero : "No disponible");
                                                        txtTelefono.setText(telefono != null ? telefono : "No disponible");
                                                    }
                                                } else {
                                                    txtEdad.setText("No disponible");
                                                    txtGenero.setText("No disponible");
                                                    txtTelefono.setText("No disponible");
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Toast.makeText(getContext(), "Error al cargar información del usuario", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                // Establecer un listener de clic en el ítem
                                expedienteView.setOnClickListener(v -> {
                                    // Cambiar la visibilidad del modal al hacer clic
                                    if (modalDetalles.getVisibility() == View.GONE) {
                                        modalDetalles.setVisibility(View.VISIBLE);
                                    } else {
                                        modalDetalles.setVisibility(View.GONE);
                                    }
                                });

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
