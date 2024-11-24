package com.example.consultapp.ui.citas;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.consultapp.Cita;
import com.example.consultapp.CitasAdapter;
import com.example.consultapp.PerfilDoc;
import com.example.consultapp.R;
import com.example.consultapp.databinding.FragmentCitasBinding;
import com.example.consultapp.login;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class CitasFragment extends Fragment {

    private FragmentCitasBinding binding;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private Dialog dialog; // Declarar como variable de instancia
    private static final int PICK_IMAGE_REQUEST = 1; // Código de solicitud para seleccionar la imagen
    private Uri imageUri = null; // URI de la imagen seleccionada

    private Spinner spinnerEspecializacion;
    private RecyclerView recyclerViewCitas;
    private CitasAdapter adapter;
    private List<Cita> citasList;

    // Referencia al botón seleccionado
    private Button selectedButton;
    private Button btnProximas, btnRealizadas, btnCanceladas;
    private String servicioSeleccionado = "";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CitasViewModel citasViewModel =
                new ViewModelProvider(this).get(CitasViewModel.class);

        binding = FragmentCitasBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializar Realtime Database y Auth
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        // Obtener referencias de vistas
        TextView textSaludo = binding.textSaludo;
        ImageButton imageButton = binding.image;

        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Inicializar RecyclerView
        recyclerViewCitas = binding.recyclerViewCitas;
        recyclerViewCitas.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializar lista y adaptador
        citasList = new ArrayList<>();
        adapter = new CitasAdapter(citasList);
        recyclerViewCitas.setAdapter(adapter);

        // Referencias a los botones
        btnProximas = binding.btnProximas;
        btnRealizadas = binding.btnRealizadas;
        btnCanceladas = binding.btnCanceladas;

        // Listener de botones
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

        // Spinner de especializaciones
        spinnerEspecializacion = binding.spinnerEspecializacion;
        cargarEspecializaciones();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Referencia al nodo del usuario
            databaseReference.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String nombreAdmin = snapshot.child("nombre").getValue(String.class);
                        if (nombreAdmin != null) {
                            textSaludo.setText("Hola, " + nombreAdmin);
                        } else {
                            textSaludo.setText("Hola, Usuario");
                        }

                        // Cargar la imagen de perfil
                        String userProfileImageUrl = snapshot.child("profileImage").getValue(String.class);
                        if (userProfileImageUrl != null) {
                            Glide.with(getContext())
                                    .load(userProfileImageUrl)
                                    .transform(new RoundedCorners(150)) // Redondear las esquinas con un radio de 16dp
                                    .placeholder(R.drawable.round_person_outline_24)
                                    .into(imageButton);
                        }
                    } else {
                        textSaludo.setText("Hola, Usuario");
                        Toast.makeText(getContext(), "No se encontró el usuario en la base de datos", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    textSaludo.setText("Hola, Usuario");
                    Toast.makeText(getContext(), "Error al obtener el nombre del usuario", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            textSaludo.setText("Hola, Usuario");
            Toast.makeText(getContext(), "Usuario no logueado", Toast.LENGTH_SHORT).show();
        }

        // Listener del botón para ir al perfil del doctor
        imageButton.setOnClickListener(view -> showBottomDialog());

        return root;
    }

    private void cargarEspecializaciones() {
        databaseReference.child("Servicios").child("TodosLosServicios").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> especializaciones = new ArrayList<>();
                for (DataSnapshot especialidadSnapshot : snapshot.getChildren()) {
                    String especializacion = especialidadSnapshot.getValue(String.class);
                    if (especializacion != null) {
                        especializaciones.add(especializacion);
                    }
                }

                if (!especializaciones.isEmpty()) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            getContext(),
                            R.layout.item_spinner,
                            especializaciones
                    );
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    spinnerEspecializacion.setAdapter(adapter);

                    spinnerEspecializacion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            servicioSeleccionado = especializaciones.get(position);

                            // Cargar citas de tipo "Próximas" al cambiar servicio
                            cargarCitas("proxima");
                            updateSelectedButton(btnProximas); // Asegurar que "Próximas" esté seleccionado
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "No hay especializaciones disponibles", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error al cargar especializaciones", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarCitas(String estado) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            databaseReference.child("citas").orderByChild("estado").equalTo(estado)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            citasList.clear();
                            for (DataSnapshot citaSnapshot : snapshot.getChildren()) {
                                Cita cita = citaSnapshot.getValue(Cita.class);
                                if (cita != null && servicioSeleccionado.equals(cita.getServicio())) {
                                    citasList.add(cita);
                                }
                            }
                            adapter.notifyDataSetChanged();

                            // Mostrar mensaje si no hay citas
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

    private void showBottomDialog() {
        // Crear y mostrar el modal para agregar un nuevo servicio
        dialog = new Dialog(requireContext());  // Asignar a la variable de instancia
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.topsheet_admin);

        ImageButton selectImageButton = dialog.findViewById(R.id.selectImageButton); // Botón para seleccionar imagen
        ImageView imageViewPreview = dialog.findViewById(R.id.imageView); // O el ID adecuado del ImageView en tu layout
        Button btn_cerrarS = dialog.findViewById(R.id.btn_cerrarS);

        // Cargar la imagen predeterminada al abrir el modal
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseReference.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String userProfileImageUrl = snapshot.child("profileImage").getValue(String.class);
                    if (userProfileImageUrl != null) {
                        // Mostrar la imagen del usuario en el modal
                        Glide.with(getContext())
                                .load(userProfileImageUrl)
                                .transform(new RoundedCorners(150)) // Redondear las esquinas con un radio de 16dp
                                .placeholder(R.drawable.round_person_outline_24)  // Imágen de placeholder
                                .into(imageViewPreview);  // Aquí mostramos la imagen en el ImageView
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Manejar error si es necesario
                }
            });
        }

        // Configura el clic en el botón de seleccionar imagen
        selectImageButton.setOnClickListener(v -> openImageSelector());

        // Configurar el botón para cerrar sesión
        btn_cerrarS.setOnClickListener(view -> {
            mAuth.signOut();
            Intent intent = new Intent(getContext(), login.class);
            startActivity(intent);
            getActivity().finish(); // Cierra la actividad actual
        });

        // Mostrar el dialog
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.TopDialogAnimation;
        dialog.getWindow().setGravity(Gravity.TOP);
    }


    private void openImageSelector() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData(); // Guardar la URI de la imagen seleccionada

            // Mostrar la imagen seleccionada en el ImageView del modal
            ImageView imageViewPreview = dialog.findViewById(R.id.imageView); // Asegúrate de usar el ID correcto
            imageViewPreview.setImageURI(imageUri);

            // Llamar a la función para subir la imagen directamente
            uploadImageToFirebase();
        }
    }


    private void uploadImageToFirebase() {
        if (imageUri != null) {
            // Mostrar un ProgressDialog mientras se sube la imagen
            ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("Subiendo imagen...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            // Crear una referencia en Firebase Storage
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            StorageReference fileReference = storageReference.child("Servicios/Imagenes/" + System.currentTimeMillis() + ".jpg");

            // Subir la imagen
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Obtener la URL de descarga
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString(); // URL de la imagen subida

                            // Guardar la URL en la base de datos de usuarios
                            saveImageUrlToUserDatabase(imageUrl);

                            if (progressDialog.isShowing()) progressDialog.dismiss();
                            Toast.makeText(getContext(), "Imagen subida con éxito", Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        if (progressDialog.isShowing()) progressDialog.dismiss();
                        Toast.makeText(getContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(getContext(), "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageUrlToUserDatabase(String imageUrl) {
        // Obtener el UID del usuario autenticado
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        if (userId != null) {
            // Referencia a la base de datos en la colección "users"
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

            // Actualizar el campo "profileImage" o el nombre que prefieras
            userRef.child("profileImage").setValue(imageUrl)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "URL guardada en la base de datos", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error al guardar la URL en la base de datos", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(getContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
