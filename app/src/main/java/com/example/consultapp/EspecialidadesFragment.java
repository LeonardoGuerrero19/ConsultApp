package com.example.consultapp;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.consultapp.databinding.FragmentEspecialidadesBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EspecialidadesFragment extends Fragment {

    private EditText agregarServicioEditText;
    private Button agregarServicioButton;
    private RecyclerView recyclerViewServicios;
    private FragmentEspecialidadesBinding binding;  // Binding para acceder a las vistas
    private ServicioAdapter servicioAdapter;
    private List<String> listaServicios = new ArrayList<>();  // Lista para almacenar los nombres de los servicios

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inicializa el binding
        binding = FragmentEspecialidadesBinding.inflate(inflater, container, false);

        // Configura el RecyclerView y el adaptador
        recyclerViewServicios = binding.recyclerViewServicios;
        recyclerViewServicios.setLayoutManager(new GridLayoutManager(getContext(), 2));
        servicioAdapter = new ServicioAdapter(listaServicios);
        recyclerViewServicios.setAdapter(servicioAdapter);

        // Cargar los servicios existentes desde Firestore
        cargarServicios();

        // Configura el FloatingActionButton y su clic
        FloatingActionButton fab = binding.floatingActionButton;
        fab.setOnClickListener(view -> showBottomDialog());

        // Retorna la vista raíz de binding
        return binding.getRoot();
    }

    private void cargarServicios() {
        FirebaseFirestore.getInstance().collection("Servicios")
                .document("Todos los servicios")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Object serviciosObject = documentSnapshot.get("Servicios");

                    // Validar que el campo 'Servicios' sea una lista
                    if (serviciosObject instanceof List) {
                        List<String> nombresServicios = (List<String>) serviciosObject;

                        if (nombresServicios != null) {
                            listaServicios.clear();  // Limpiar la lista antes de cargar nuevos datos
                            listaServicios.addAll(nombresServicios);
                            servicioAdapter.notifyDataSetChanged();  // Notificar al adaptador para que actualice el RecyclerView
                        }
                    } else {
                        Toast.makeText(getContext(), "Error: El campo 'Servicios' no es una lista válida", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar servicios", Toast.LENGTH_SHORT).show();
                });
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
            dialog.dismiss();  // Cerrar el modal después de agregar
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

        // Agregar el servicio al documento "Todos los servicios" (para mantener la lista completa)
        FirebaseFirestore.getInstance().collection("Servicios")
                .document("Todos los servicios")
                .update("Servicios", FieldValue.arrayUnion(servicioNombre))
                .addOnSuccessListener(aVoid -> {
                    // Después de actualizar la lista, crear un documento separado para el servicio
                    FirebaseFirestore.getInstance().collection("Servicios")
                            .document(servicioNombre) // Nombre del servicio como ID del documento individual
                            .set(new HashMap<String, Object>() {{
                                put("nombre", servicioNombre); // Campo nombre
                                put("descripcion", descripcion); // Campo descripción
                            }})
                            .addOnSuccessListener(aVoid1 -> {
                                cargarServicios(); // Refrescar la lista completa
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


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Liberar el binding cuando la vista se destruya
    }
}