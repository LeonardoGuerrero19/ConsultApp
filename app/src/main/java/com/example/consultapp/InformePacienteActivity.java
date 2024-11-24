package com.example.consultapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class InformePacienteActivity extends AppCompatActivity {

    private DatabaseReference dbRef;
    private LinearLayout linearInformes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informe_paciente);

        // Obtener datos del Intent
        String numeroCuenta = getIntent().getStringExtra("numeroCuenta");

        // Referenciar TextViews en el diseño
        linearInformes = findViewById(R.id.linearInformes); // Obtén la referencia aquí


        // Inicializar referencia a Firebase
        dbRef = FirebaseDatabase.getInstance().getReference("Informes");

        // Cargar informes asociados al paciente
        cargarInformes(numeroCuenta);
    }

    private void cargarInformes(String numeroCuenta) {
        dbRef.orderByChild("numeroCuenta").equalTo(numeroCuenta)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        linearInformes.removeAllViews(); // Limpiar antes de cargar nuevos datos
                        LayoutInflater inflater = LayoutInflater.from(InformePacienteActivity.this);

                        for (DataSnapshot informeSnapshot : snapshot.getChildren()) {
                            // Obtener datos del informe
                            String motivo = informeSnapshot.child("motivo").getValue(String.class);
                            String padecimiento = informeSnapshot.child("padecimiento").getValue(String.class);
                            String medicamento = informeSnapshot.child("medicamento").getValue(String.class);

                            // Inflar un diseño para cada informe
                            View informeView = inflater.inflate(R.layout.item_informe, linearInformes, false);

                            // Configurar datos en el diseño inflado
                            TextView tvMotivo = informeView.findViewById(R.id.tvMotivoInforme);
                            TextView tvPadecimiento = informeView.findViewById(R.id.tvPadecimientoInforme);
                            TextView tvMedicamento = informeView.findViewById(R.id.tvMedicamentoInforme);

                            tvMotivo.setText(motivo != null ? motivo : "Motivo no disponible");
                            tvPadecimiento.setText(padecimiento != null ? padecimiento : "Padecimiento no disponible");
                            tvMedicamento.setText(medicamento != null ? medicamento : "Medicamento no disponible");

                            // Agregar la vista inflada al LinearLayout
                            linearInformes.addView(informeView);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Manejar errores (puedes agregar un log o mostrar un mensaje al usuario)
                    }
                });
    }

}