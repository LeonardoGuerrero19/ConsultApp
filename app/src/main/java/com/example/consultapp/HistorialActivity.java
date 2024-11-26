package com.example.consultapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HistorialActivity extends AppCompatActivity {

    private DatabaseReference dbRef;
    private LinearLayout llInformes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        // Inicializar la referencia a la base de datos
        dbRef = FirebaseDatabase.getInstance().getReference("Informes");

        // Obtener referencias a los elementos de la UI
        llInformes = findViewById(R.id.llInformes);
        TextView txtNumeroCuenta = findViewById(R.id.txtNumeroCuenta);

        // Obtener los datos del intent
        Intent intent = getIntent();
        String numeroCuenta = intent.getStringExtra("numeroCuenta");
        String nombreDoctor = intent.getStringExtra("nombreDoctor");  // Nombre del doctor logueado

        // Establecer el número de cuenta en la UI
        txtNumeroCuenta.setText(numeroCuenta);

        // Cargar los informes desde Firebase
        cargarInformes(numeroCuenta, nombreDoctor);
    }

    private void cargarInformes(String numeroCuenta, String nombreDoctor) {
        // Leer los informes del usuario de la base de datos, filtrando por nombreDoctor
        dbRef.orderByChild("numeroCuenta").equalTo(numeroCuenta).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Limpiar la lista de informes antes de agregar nuevos
                    llInformes.removeAllViews();

                    // Iterar a través de los informes
                    for (DataSnapshot informeSnapshot : dataSnapshot.getChildren()) {
                        String nombre = informeSnapshot.child("nombre").getValue(String.class);
                        String nombreDoctorInforme = informeSnapshot.child("nombreDoctor").getValue(String.class);
                        String fechaCita = informeSnapshot.child("fechaCita").getValue(String.class);
                        String motivo = informeSnapshot.child("motivo").getValue(String.class);
                        String padecimiento = informeSnapshot.child("padecimiento").getValue(String.class);
                        String medicamentos = informeSnapshot.child("medicamento").getValue(String.class);

                        // Verificar si el informe pertenece al doctor logueado
                        if (nombreDoctorInforme != null && nombreDoctorInforme.equals(nombreDoctor)) {
                            // Inflar el diseño del informe
                            View informeView = getLayoutInflater().inflate(R.layout.item_historial, llInformes, false);

                            // Rellenar los TextViews con los datos del informe
                            TextView txtFechaCita = informeView.findViewById(R.id.txtFechaCita);
                            TextView txtMotivo = informeView.findViewById(R.id.txtMotivo);
                            TextView txtPadecimiento = informeView.findViewById(R.id.txtPadecimiento);
                            TextView txtMedicamentos = informeView.findViewById(R.id.txtMedicamentos);
                            LinearLayout modalDetalles = informeView.findViewById(R.id.modalDetalles);


                            txtFechaCita.setText(fechaCita);
                            txtMotivo.setText(motivo);
                            txtPadecimiento.setText(padecimiento);
                            txtMedicamentos.setText(medicamentos);

                            // Establecer un listener de clic en el ítem
                            informeView.setOnClickListener(v -> {
                                // Cambiar la visibilidad del modal al hacer clic
                                if (modalDetalles.getVisibility() == View.GONE) {
                                    modalDetalles.setVisibility(View.VISIBLE);
                                } else {
                                    modalDetalles.setVisibility(View.GONE);
                                }
                            });

                            // Agregar la vista inflada al LinearLayout
                            llInformes.addView(informeView);
                        }
                    }
                } else {
                    Toast.makeText(HistorialActivity.this, "No se encontraron informes para este usuario.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(HistorialActivity.this, "Error al cargar los informes.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
