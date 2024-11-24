package com.example.consultapp.ui.especialidades;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.consultapp.R;
import com.example.consultapp.databinding.FragmentEspecialidadesAdminBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EspecialidadesFragment extends Fragment {

    // Binding para acceder a las vistas del fragmento
    private FragmentEspecialidadesAdminBinding binding;
    private DatabaseReference databaseReference; // Referencia a la base de datos de Firebase
    private static final int PICK_IMAGE_REQUEST = 1; // Código de solicitud para seleccionar la imagen
    private Uri imageUri = null; // URI de la imagen seleccionada
    private String servicioNombre;  // Declarar como variable de clase
    private String descripcion;     // Declarar como variable de clase
    private Dialog dialog; // Declarar como variable de instancia


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inicializa el binding
        binding = FragmentEspecialidadesAdminBinding.inflate(inflater, container, false);

        // Configura el FloatingActionButton para mostrar el modal al hacer clic
        FloatingActionButton fab = binding.floatingActionButton;
        fab.setOnClickListener(view -> showBottomDialog());

        // Inicializa la referencia de la base de datos
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Carga los servicios desde la base de datos
        cargarServicios();

        // Retorna la vista raíz de binding
        return binding.getRoot();
    }

    private void showBottomDialog() {
        // Crear y mostrar el modal para agregar un nuevo servicio
        dialog = new Dialog(requireContext());  // Asignar a la variable de instancia
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheet_layout);

        // Referencias a los elementos del modal
        EditText editarServicioModal = dialog.findViewById(R.id.nombre_especialidad);
        Button botonAgregarServicioModal = dialog.findViewById(R.id.btnAgregarEspecialidad);
        ImageButton selectImageButton = dialog.findViewById(R.id.selectImageButton); // Botón para seleccionar imagen
        EditText descripcionServicioModal = dialog.findViewById(R.id.descripcion_especialidad);

        ImageView imageViewPreview = dialog.findViewById(R.id.imageView); // O el ID adecuado del ImageView en tu layout

        // Configura el clic en el botón de seleccionar imagen
        selectImageButton.setOnClickListener(v -> openImageSelector());

        // Configurar el clic en el botón de agregar servicio
        botonAgregarServicioModal.setOnClickListener(v -> {
            servicioNombre = editarServicioModal.getText().toString().trim();
            descripcion = descripcionServicioModal.getText().toString().trim();

            // Validar si los campos no están vacíos
            if (TextUtils.isEmpty(servicioNombre)) {
                Toast.makeText(getContext(), "Ingrese un nombre de servicio", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(descripcion)) {
                Toast.makeText(getContext(), "Ingrese una descripción del servicio", Toast.LENGTH_SHORT).show();
                return;
            }

            agregarServicioDesdeModal(servicioNombre, descripcion);
            dialog.dismiss(); // Cerrar el modal después de agregar
        });

        // Mostrar el dialog
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }


    private void openImageSelector() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    private void agregarServicioDesdeModal(String servicioNombre, String descripcion) {
        // Validar si los campos no están vacíos
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
                    // Crear un nodo separado para el servicio con su descripción y la URL de la imagen
                    if (imageUri != null) {
                        uploadImageToFirebaseStorage(servicioNombre, descripcion); // Llamar al método para subir la imagen
                    } else {
                        // Si no hay imagen, solo guarda el servicio con el nombre y la descripción
                        Map<String, Object> servicioMap = new HashMap<>();
                        servicioMap.put("nombre", servicioNombre);
                        servicioMap.put("descripcion", descripcion);
                        databaseReference.child("Servicios").child("DetalleServicios").child(servicioNombre).setValue(servicioMap)
                                .addOnSuccessListener(aVoid1 -> {
                                    Toast.makeText(getContext(), "Servicio agregado sin imagen", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Error al crear documento individual para el servicio", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al agregar servicio a la lista", Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarServicios() {
        DatabaseReference serviciosRef = databaseReference.child("Servicios").child("DetalleServicios");

        serviciosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.linearServicios.removeAllViews(); // Limpia el contenido previo
                LinearLayout fila = null;
                int contador = 0;

                for (DataSnapshot servicioSnapshot : snapshot.getChildren()) {
                    String nombre = servicioSnapshot.child("nombre").getValue(String.class);
                    String imagenUrl = servicioSnapshot.child("imagenUrl").getValue(String.class); // Suponiendo que la URL está almacenada en "imagenUrl"

                    View servicioView = LayoutInflater.from(getContext()).inflate(R.layout.item_servicio, null);
                    TextView nombreServicio = servicioView.findViewById(R.id.nombre_servicio);
                    ImageView imagenServicio = servicioView.findViewById(R.id.imagen_servicio); // Obtener el ImageView

                    nombreServicio.setText(nombre);

                    // Cargar la imagen usando Glide
                    if (imagenUrl != null) {
                        Glide.with(getContext())
                                .load(imagenUrl)
                                .into(imagenServicio);
                    }

                    // Crear una fila nueva si es necesario
                    if (contador % 2 == 0) {
                        fila = new LinearLayout(getContext());
                        fila.setOrientation(LinearLayout.HORIZONTAL);
                        fila.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        fila.setPadding(0, 0, 0, 0); // Margen entre filas
                        binding.linearServicios.addView(fila);
                    }

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1
                    );
                    params.setMargins(0, 0, 40, 50); // Margen entre los elementos
                    servicioView.setLayoutParams(params);

                    fila.addView(servicioView);
                    contador++;
                }

                // Si solo hay un servicio, agregar una vista vacía al final
                if (snapshot.getChildrenCount() == 1) {
                    LinearLayout.LayoutParams paramsVacia = new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1
                    );
                    View viewVacia = new View(getContext());
                    viewVacia.setLayoutParams(paramsVacia);
                    fila.addView(viewVacia);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error al cargar los servicios", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData(); // Obtén la URI de la imagen seleccionada

            // Aquí actualizas el ImageView con la imagen seleccionada
            ImageView imageViewPreview = dialog.findViewById(R.id.imageView); // Usar el dialog de la instancia
            Glide.with(requireContext())
                    .load(imageUri) // Carga la URI de la imagen
                    .into(imageViewPreview); // Muestra la imagen en el ImageView

            // También puedes llamar al método uploadImageToFirebaseStorage si deseas subir la imagen
            if (!TextUtils.isEmpty(servicioNombre) && !TextUtils.isEmpty(descripcion)) {
                uploadImageToFirebaseStorage(servicioNombre, descripcion); // Pasa estos valores para completar la subida
            } else {
                Toast.makeText(getContext(), "Ingrese nombre y descripción del servicio", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImageToFirebaseStorage(String servicioNombre, String descripcion) {
        if (imageUri != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            StorageReference fileReference = storageReference.child("Servicios/Imagenes/" + System.currentTimeMillis() + ".jpg");

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString(); // Obtiene la URL de la imagen

                            // Agregar la URL de la imagen al servicio en Firebase
                            Map<String, Object> servicioMap = new HashMap<>();
                            servicioMap.put("nombre", servicioNombre); // Usar el parámetro
                            servicioMap.put("descripcion", descripcion); // Usar el parámetro
                            servicioMap.put("imagenUrl", imageUrl); // Guardar la URL de la imagen

                            databaseReference.child("Servicios").child("DetalleServicios").child(servicioNombre).setValue(servicioMap)
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Error al agregar servicio", Toast.LENGTH_SHORT).show();
                                    });
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(getContext(), "No se ha seleccionado ninguna imagen", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Liberar el binding cuando la vista se destruya
    }
}
