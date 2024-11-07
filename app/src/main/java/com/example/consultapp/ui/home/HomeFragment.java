package com.example.consultapp.ui.home;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.consultapp.R;
import com.example.consultapp.ServicioAdapter;
import com.example.consultapp.databinding.FragmentHomeBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private EditText agregarServicioEditText;
    private Button agregarServicioButton;
    private FragmentHomeBinding binding;  // Binding para acceder a las vistas
    private RecyclerView recyclerViewServicios;
    private ServicioAdapter servicioAdapter;
    private List<String> listaServicios = new ArrayList<>();  // Lista para almacenar los nombres de los servicios

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflar el layout y obtener la instancia del binding
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot(); // Obtén la vista raíz

        // Inicializar las vistas
        recyclerViewServicios = binding.recyclerViewServicios;

        // Configurar el RecyclerView como una cuadrícula de 2 columnas
        recyclerViewServicios.setLayoutManager(new GridLayoutManager(getContext(), 2));
        servicioAdapter = new ServicioAdapter(listaServicios);
        recyclerViewServicios.setAdapter(servicioAdapter);

        // Cargar los servicios existentes desde Firestore
        cargarServicios();

        // Obtén el FloatingActionButton desde el binding y configura el clic
        FloatingActionButton fab = binding.floatingActionButton;
        fab.setOnClickListener(view ->
                showBottomDialog()
        );


        return root;
    }

    private void agregarServicio() {
        String agregarServicio = agregarServicioEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(agregarServicio)) {
            // Agregar el nuevo servicio a Firestore
            FirebaseFirestore.getInstance().collection("Servicios")
                    .document("Todos los servicios")
                    .update("Servicios", FieldValue.arrayUnion(agregarServicio))
                    .addOnSuccessListener(aVoid -> {
                        agregarServicioEditText.setText(""); // Limpiar el campo de texto
                        cargarServicios(); // Llamada para refrescar la lista completa
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error al agregar servicio", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(getContext(), "Ingrese un nombre de servicio", Toast.LENGTH_SHORT).show();
        }
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

        // Configurar el clic en el botón del modal
        botonAgregarServicioModal.setOnClickListener(v -> {
            String servicioNombre = editarServicioModal.getText().toString().trim();
            agregarServicioDesdeModal(servicioNombre);
            dialog.dismiss();  // Cerrar el modal después de agregar
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void agregarServicioDesdeModal(String servicioNombre) {
        if (!TextUtils.isEmpty(servicioNombre)) {
            // Agregar el nuevo servicio a Firestore
            FirebaseFirestore.getInstance().collection("Servicios")
                    .document("Todos los servicios")
                    .update("Servicios", FieldValue.arrayUnion(servicioNombre))
                    .addOnSuccessListener(aVoid -> {
                        cargarServicios(); // Llamada para refrescar la lista completa
                        Toast.makeText(getContext(), "Servicio agregado", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error al agregar servicio", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(getContext(), "Ingrese un nombre de servicio", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Liberar el binding cuando la vista se destruya
    }
}
