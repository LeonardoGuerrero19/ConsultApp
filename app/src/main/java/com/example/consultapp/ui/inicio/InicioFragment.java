package com.example.consultapp.ui.inicio;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.consultapp.AgendaActivity;
import com.example.consultapp.MedicoPerfilActivity;
import com.example.consultapp.MoreDoctoresActivity;
import com.example.consultapp.MoreEspecialidadesActivity;
import com.example.consultapp.PerfilDoc;
import com.example.consultapp.PerfilEspecialidadActivity;
import com.example.consultapp.R;
import com.example.consultapp.databinding.FragmentInicioBinding;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InicioFragment extends Fragment {

    private FragmentInicioBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private GridLayout gridLayout;
    private LinearLayout linearProxCitas, linearSerivicios, linearPersonalMedico;
    private Button btn_cerrarS;
    private Dialog dialog; // Declarar como variable de instancia
    private static final int PICK_IMAGE_REQUEST = 1; // Código de solicitud para seleccionar la imagen
    private Uri imageUri = null; // URI de la imagen seleccionada

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        InicioViewModel inicioViewModel =
                new ViewModelProvider(this).get(InicioViewModel.class);

        binding = FragmentInicioBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializar FirebaseAuth y Realtime Database
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Inicializar vistas
        TextView textSaludo = binding.textSaludo;
        ImageButton imageButton = binding.image;
        linearSerivicios = root.findViewById(R.id.linearServicios);
        linearProxCitas = root.findViewById(R.id.linearProxCitas);
        linearPersonalMedico = root.findViewById(R.id.linearPersonalMedico);
        btn_cerrarS = root.findViewById(R.id.btn_cerrarS);

        // Obtener el usuario actual de FirebaseAuth
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Referencia al nodo del usuario en Realtime Database
            databaseReference.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String nombreUsuario = snapshot.child("nombre").getValue(String.class);
                        String fotoUserUrl = snapshot.child("fotoPerfil").getValue(String.class); // Asume que el nodo "foto" tiene la URL
                        if (nombreUsuario != null) {
                            textSaludo.setText("Hola, " + nombreUsuario);
                            // Cargar la foto del doctor en el ImageButton
                            if (fotoUserUrl != null && !fotoUserUrl.isEmpty()) {
                                Glide.with(requireContext())
                                        .load(fotoUserUrl)
                                        .transform(new RoundedCorners(150)) // Redondear las esquinas con un radio de 16dp
                                        .placeholder(R.drawable.round_person_outline_24)
                                        .into(imageButton);
                            }
                        } else {
                            textSaludo.setText("Hola, Usuario");
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

            // Cargar próximas citas del usuario
            cargarProximasCitas(userId);
            cargarPersonalMedico();

        } else {
            textSaludo.setText("Hola, Usuario");
            Toast.makeText(getContext(), "Usuario no logueado", Toast.LENGTH_SHORT).show();
        }

        // Inicializar el TextView
        TextView moreLink = root.findViewById(R.id.more_link);
        TextView moreLink2 = root.findViewById(R.id.more_link2);


        // Configurar el OnClickListener
        moreLink.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MoreEspecialidadesActivity.class); // Cambia 'NuevaActivity' por tu clase destino
            startActivity(intent);
        });

        // Configurar el OnClickListener
        moreLink2.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MoreDoctoresActivity.class); // Cambia 'NuevaActivity' por tu clase destino
            startActivity(intent);
        });


        // Obtener y mostrar los servicios
        cargarServicios();

        // Listener del botón para ir al perfil del doctor
        imageButton.setOnClickListener(view -> showBottomDialog());

        return root;
    }

    private void cargarProximasCitas(String userId) {
        databaseReference.child("citas").orderByChild("usuario_id").equalTo(userId)
                .addValueEventListener(new ValueEventListener() { // Cambiar a ValueEventListener
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        linearProxCitas.removeAllViews(); // Limpiar citas previas para evitar duplicados
                        if (snapshot.exists()) {
                            for (DataSnapshot citaSnapshot : snapshot.getChildren()) {
                                String estado = citaSnapshot.child("estado").getValue(String.class);
                                if ("proxima".equalsIgnoreCase(estado)) { // Verificar si el estado es "próxima"
                                    String servicio = citaSnapshot.child("servicio").getValue(String.class);
                                    String doctor = citaSnapshot.child("doctor").getValue(String.class);
                                    String fecha = citaSnapshot.child("fecha").getValue(String.class);
                                    String horario = citaSnapshot.child("horario").getValue(String.class);

                                    // Crear y agregar un TextView para cada cita válida
                                    agregarTextoCita(servicio, doctor, fecha, horario);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Error al cargar las citas.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void agregarTextoCita(String servicio, String doctor, String fecha, String horario) {
        View citaView = LayoutInflater.from(getContext()).inflate(R.layout.item_prox_citas, linearProxCitas, false);

        TextView tvServicio = citaView.findViewById(R.id.tvServicio);
        TextView tvDoctor = citaView.findViewById(R.id.tvDoctor);
        TextView tvFecha = citaView.findViewById(R.id.tvFecha);
        TextView tvHorario = citaView.findViewById(R.id.tvHorario);

        String fechaFormateada = formatearFecha(fecha);

        tvServicio.setText(servicio);
        tvDoctor.setText("Dr. " + doctor);
        tvFecha.setText(fechaFormateada);
        tvHorario.setText(horario);

        linearProxCitas.addView(citaView);
    }

    private String formatearFecha(String fechaOriginal) {
        SimpleDateFormat formatoEntrada = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat formatoSalida = new SimpleDateFormat("d 'de' MMMM", new Locale("es", "ES"));

        try {
            Date fecha = formatoEntrada.parse(fechaOriginal);
            return formatoSalida.format(fecha);
        } catch (ParseException e) {
            e.printStackTrace();
            return fechaOriginal;
        }
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
                    if (contador >= 4) break;  // Limita a 4 servicios

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

                    // Establecer el OnClickListener para redirigir a AgendaActivity
                    servicioView.setOnClickListener(v -> {
                        // Crear un Intent para redirigir a AgendaActivity
                        Intent intent = new Intent(getContext(), AgendaActivity.class);
                        intent.putExtra("nombre_servicio", nombre); // Pasar el nombre del servicio (si lo necesitas)
                        startActivity(intent);
                    });

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

    private void cargarPersonalMedico() {
        DatabaseReference medicosRef = databaseReference.child("Medicos"); // Suponiendo que los médicos están en la rama "medicos"

        medicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Limpiar las vistas previas para evitar duplicados
                LinearLayout linearPersonalMedico = binding.linearPersonalMedico;
                linearPersonalMedico.removeAllViews();

                // Iterar sobre los médicos en la base de datos
                if (snapshot.exists()) {
                    int contador = 0; // Variable para contar los médicos

                    for (DataSnapshot medicoSnapshot : snapshot.getChildren()) {
                        if (contador >= 4) break; // Detener la iteración después de los primeros 3 médicos

                        String nombre = medicoSnapshot.child("nombre").getValue(String.class);
                        String especialidad = medicoSnapshot.child("especializacion").getValue(String.class);
                        String cedula = medicoSnapshot.child("cedula").getValue(String.class);
                        String fotoPerfil = medicoSnapshot.child("fotoPerfil").getValue(String.class); // Obtener la URL de la foto de perfil
                        String medicoId = medicoSnapshot.getKey(); // Obtener la clave única del médico, que es el ID en Firebase

                        // Crear un layout dinámico para cada médico
                        View medicoView = LayoutInflater.from(getContext()).inflate(R.layout.item_medico, linearPersonalMedico, false);

                        // Configurar los TextViews con los datos del médico
                        TextView tvNombre = medicoView.findViewById(R.id.tvNombreMedico);
                        TextView tvEspecialidad = medicoView.findViewById(R.id.tvEspecialidad);
                        TextView tvCedula = medicoView.findViewById(R.id.tvCedula);
                        ImageView ivFotoPerfil = medicoView.findViewById(R.id.imageView); // ImageView donde se mostrará la foto de perfil
                        Button btnVerPerfil = medicoView.findViewById(R.id.btnVerPerfilMedico); // Botón de "Ver perfil"

                        tvNombre.setText("Dr. " + nombre);
                        tvEspecialidad.setText(especialidad);
                        tvCedula.setText(cedula);

                        // Cargar la foto del médico usando Glide
                        if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
                            Glide.with(getContext())
                                    .load(fotoPerfil)
                                    .transform(new RoundedCorners(150)) // Redondear las esquinas
                                    .into(ivFotoPerfil);
                        }

                        // Agregar el layout del médico al contenedor
                        linearPersonalMedico.addView(medicoView);

                        // Configurar el botón de "Ver perfil"
                        btnVerPerfil.setOnClickListener(v -> {
                            // Redirigir a la actividad de perfil del médico
                            Intent intent = new Intent(getContext(), MedicoPerfilActivity.class);
                            intent.putExtra("nombre", nombre);  // Aquí asegúrate de que "nombre" no sea null
                            intent.putExtra("especialidad", especialidad);
                            intent.putExtra("cedula", cedula);
                            intent.putExtra("fotoPerfil", fotoPerfil);
                            intent.putExtra("medicoId", medicoId); // Pasar el ID del médico
                            startActivity(intent);
                        });

                        contador++; // Incrementar el contador
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error al cargar los médicos", Toast.LENGTH_SHORT).show();
            }
        });
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

        btn_cerrarS.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), login.class);
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
