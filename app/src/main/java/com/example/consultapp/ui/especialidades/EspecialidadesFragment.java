package com.example.consultapp.ui.especialidades;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;


import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.consultapp.R;
import com.example.consultapp.databinding.FragmentEspecialidadesAdminBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EspecialidadesFragment extends Fragment {

    private FragmentEspecialidadesAdminBinding binding; // Binding para acceder a las vistas
    private DatabaseReference databaseReference;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inicializa el binding
        binding = FragmentEspecialidadesAdminBinding.inflate(inflater, container, false);


        // Configura el FloatingActionButton y su clic
        FloatingActionButton fab = binding.floatingActionButton;
        fab.setOnClickListener(view -> showBottomDialog());

        // Inicializar la referencia de la base de datos
        databaseReference = FirebaseDatabase.getInstance().getReference();

        cargarServicios();

        // Retorna la vista raíz de binding
        return binding.getRoot();
    }

    private void showBottomDialog() {
        // Usar requireContext() para obtener el contexto del fragmento
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheet_layout);

        // Obtener referencias a los elementos del modal
        EditText editarServicioModal = dialog.findViewById(R.id.nombre_especialidad);
        Button botonAgregarServicioModal = dialog.findViewById(R.id.btnAgregarEspecialidad);
        EditText descripcionServicioModal = dialog.findViewById(R.id.descripcion_especialidad);

        // Configurar el clic en el botón del modal
        botonAgregarServicioModal.setOnClickListener(v -> {
            String servicioNombre = editarServicioModal.getText().toString().trim();
            String descripcion = descripcionServicioModal.getText().toString().trim(); // Obtener la descripción
            agregarServicioDesdeModal(servicioNombre, descripcion);
            dialog.dismiss(); // Cerrar el modal después de agregar
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void agregarServicioDesdeModal(String servicioNombre, String descripcion) {
        if (TextUtils.isEmpty(servicioNombre)) {
            Toast.makeText(getContext(), "Ingrese un nombre de servicio", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(descripcion)) {
            Toast.makeText(getContext(), "Ingrese una descripción del servicio", Toast.LENGTH_SHORT).show();
            return;
        }

        // Agregar el servicio a la lista "TodosLosServicios"
        databaseReference.child("Servicios").child("TodosLosServicios").child(servicioNombre).setValue(servicioNombre)
                .addOnSuccessListener(aVoid -> {
                    // Crear un nodo separado para el servicio con su descripción
                    Map<String, Object> servicioMap = new HashMap<>();
                    servicioMap.put("nombre", servicioNombre);
                    servicioMap.put("descripcion", descripcion);

                    databaseReference.child("Servicios").child("DetalleServicios").child(servicioNombre).setValue(servicioMap)
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(getContext(), "Servicio agregado", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error al crear documento individual para el servicio", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al agregar servicio a la lista", Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarServicios() {
        DatabaseReference serviciosRef = databaseReference.child("Servicios").child("DetalleServicios");

        serviciosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.linearServicios.removeAllViews(); // Limpia el contenido previo
                LinearLayout fila = null; // Contenedor horizontal para pares de servicios
                int contador = 0; // Contador para agrupar en pares

                for (DataSnapshot servicioSnapshot : snapshot.getChildren()) {
                    String nombre = servicioSnapshot.child("nombre").getValue(String.class);

                    // Crear un CardView dinámicamente
                    View servicioView = LayoutInflater.from(getContext()).inflate(R.layout.item_servicio, null);
                    TextView nombreServicio = servicioView.findViewById(R.id.nombre_servicio);

                    // Configurar los textos
                    nombreServicio.setText(nombre);

                    // Configurar el contenedor horizontal
                    if (contador % 2 == 0) {
                        fila = new LinearLayout(getContext());
                        fila.setOrientation(LinearLayout.HORIZONTAL);
                        fila.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        fila.setPadding(0, 0, 0, 0); // Margen entre filas
                        binding.linearServicios.addView(fila);
                    }

                    // Configurar el tamaño de cada servicio dentro de la fila
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            0, // Ocupa la mitad del ancho
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1 // Peso igual entre servicios
                    );
                    // Añadir márgenes a cada item (servicio)
                    params.setMargins(0, 0, 20, 20); // 20dp de margen a la derecha
                    servicioView.setLayoutParams(params);

                    fila.addView(servicioView); // Añadir a la fila actual
                    contador++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error al cargar los servicios", Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Liberar el binding cuando la vista se destruya
    }
}