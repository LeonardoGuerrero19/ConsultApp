package com.example.consultapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MoreEspecialidadesActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private LinearLayout linearServicios;  // Se corrigió el nombre de la variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_especialidades);

        // Inicializar FirebaseAuth y Realtime Database
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        linearServicios = findViewById(R.id.linearServicios); // Se cambió la referencia correcta

        // Obtener y mostrar los servicios
        cargarServicios();
    }

    private void cargarServicios() {
        DatabaseReference serviciosRef = databaseReference.child("Servicios").child("DetalleServicios");

        serviciosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                linearServicios.removeAllViews(); // Limpia el contenido previo
                LinearLayout fila = null;
                int contador = 0;

                for (DataSnapshot servicioSnapshot : snapshot.getChildren()) {

                    String nombre = servicioSnapshot.child("nombre").getValue(String.class);
                    String imagenUrl = servicioSnapshot.child("imagenUrl").getValue(String.class); // Suponiendo que la URL está almacenada en "imagenUrl"

                    // Inflar la vista para un servicio
                    View servicioView = LayoutInflater.from(MoreEspecialidadesActivity.this).inflate(R.layout.item_servicio, null);
                    TextView nombreServicio = servicioView.findViewById(R.id.nombre_servicio);
                    ImageView imagenServicio = servicioView.findViewById(R.id.imagen_servicio); // Obtener el ImageView

                    nombreServicio.setText(nombre);

                    // Cargar la imagen usando Glide
                    if (imagenUrl != null) {
                        Glide.with(MoreEspecialidadesActivity.this)
                                .load(imagenUrl)
                                .into(imagenServicio);
                    }

                    // Establecer el OnClickListener para redirigir a AgendaActivity
                    servicioView.setOnClickListener(v -> {
                        // Crear un Intent para redirigir a AgendaActivity
                        Intent intent = new Intent(MoreEspecialidadesActivity.this, AgendaActivity.class);
                        intent.putExtra("nombre_servicio", nombre); // Pasar el nombre del servicio (si lo necesitas)
                        startActivity(intent);
                    });

                    // Crear una fila nueva si es necesario
                    if (contador % 2 == 0) {
                        fila = new LinearLayout(MoreEspecialidadesActivity.this);
                        fila.setOrientation(LinearLayout.HORIZONTAL);
                        fila.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        fila.setPadding(0, 0, 0, 0); // Margen entre filas
                        linearServicios.addView(fila);
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
                    View viewVacia = new View(MoreEspecialidadesActivity.this);
                    viewVacia.setLayoutParams(paramsVacia);
                    fila.addView(viewVacia);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MoreEspecialidadesActivity.this, "Error al cargar los servicios", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
