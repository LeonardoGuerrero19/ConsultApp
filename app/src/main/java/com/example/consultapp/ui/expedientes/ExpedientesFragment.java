package com.example.consultapp.ui.expedientes;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.consultapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ExpedientesFragment extends Fragment {

    private EditText etNumeroCuenta;
    private ImageButton btnBuscar;
    private TextView tvNumeroCuenta, tvNombre;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el diseño del fragmento
        View view = inflater.inflate(R.layout.fragment_expedientes, container, false);

        // Inicializar elementos de la interfaz
        etNumeroCuenta = view.findViewById(R.id.etNumeroCuenta);
        btnBuscar = view.findViewById(R.id.btnBuscar);
        tvNumeroCuenta = view.findViewById(R.id.tvNumeroCuenta);
        tvNombre = view.findViewById(R.id.tvNombre);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Configurar botón de búsqueda
        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscarUsuario();
            }
        });

        return view;
    }

    private void buscarUsuario() {
        // Obtener el número de cuenta ingresado por el usuario
        String numeroCuenta = etNumeroCuenta.getText().toString().trim();

        // Verificar si el número de cuenta no está vacío
        if (numeroCuenta.isEmpty()) {
            Toast.makeText(getContext(), "Por favor ingrese un número de cuenta", Toast.LENGTH_SHORT).show();
            return;
        }

        // Referencia a la colección de usuarios en Firestore
        CollectionReference usuariosRef = db.collection("user");

        // Buscar documentos donde el campo "numeroCuenta" coincida con el número ingresado
        usuariosRef.whereEqualTo("numeroCuenta", numeroCuenta)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                // Mostrar los datos del usuario encontrado
                                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                    String cuenta = document.getString("numeroCuenta");
                                    String nombre = document.getString("nombre");

                                    // Mostrar datos en los TextView
                                    tvNumeroCuenta.setText(cuenta);
                                    tvNombre.setText(nombre);
                                }
                            } else {
                                // No se encontró el usuario
                                tvNumeroCuenta.setText("Número de cuenta: No disponible");
                                tvNombre.setText("Nombre: No disponible");
                            }
                        } else {
                            // Error en la consulta
                            Toast.makeText(getContext(), "Error al buscar usuario", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
