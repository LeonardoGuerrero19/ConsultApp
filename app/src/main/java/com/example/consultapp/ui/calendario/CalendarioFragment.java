package com.example.consultapp.ui.calendario;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.consultapp.PerfilDoc;
import com.example.consultapp.databinding.FragmentCalendarioBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class CalendarioFragment extends Fragment {

    private FragmentCalendarioBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private LinearLayout linearCitas;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CalendarioViewModel calendarioViewModel =
                new ViewModelProvider(this).get(CalendarioViewModel.class);

        binding = FragmentCalendarioBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializar Firestore y Auth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        linearCitas = binding.linearCitas; // LinearLayout donde se mostrarán las citas

        // Obtener el TextView text_saludo desde el binding
        TextView textSaludo = binding.textSaludo;

        // Obtener el usuario actual de FirebaseAuth
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Referencia al documento del usuario en Firestore
            DocumentReference userRef = db.collection("medicos").document(userId);

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Obtener el nombre del campo "nombre"
                    String nombreUsuario = documentSnapshot.getString("nombre");
                    if (nombreUsuario != null) {
                        textSaludo.setText("Hola, " + nombreUsuario);
                    } else {
                        textSaludo.setText("Hola, Usuario");
                    }
                } else {
                    textSaludo.setText("Hola, Usuario");
                    Toast.makeText(getContext(), "No se encontró el usuario en la base de datos", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                textSaludo.setText("Hola, Usuario");
                Toast.makeText(getContext(), "Error al obtener el nombre del usuario", Toast.LENGTH_SHORT).show();
            });
        } else {
            textSaludo.setText("Hola, Usuario");
            Toast.makeText(getContext(), "Usuario no logueado", Toast.LENGTH_SHORT).show();
        }

        // Obtener las citas del día desde Firestore
        getCitasDelDia();

        // Obtener el ImageButton desde el binding y agregar un OnClickListener
        ImageButton imageButton = binding.image;
        imageButton.setOnClickListener(v -> {
            // Crear un intent para redireccionar a OtraActividad
            Intent intent = new Intent(getActivity(), PerfilDoc.class);
            startActivity(intent);
        });

        return root;
    }

    private void getCitasDelDia() {
        // Obtener la fecha actual para buscar las citas de hoy
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String fechaActual = sdf.format(calendar.getTime());

        // Consultar Firestore para obtener las citas de la fecha actual
        db.collection("citas")
                .whereEqualTo("fecha", fechaActual) // Filtrar por la fecha
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Limpiar el LinearLayout antes de agregar nuevas vistas
                        linearCitas.removeAllViews();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Obtener los campos de cada cita
                            String fecha = document.getString("fecha");
                            String horario = document.getString("horario");

                            // Crear un nuevo TextView para mostrar la cita
                            TextView citaView = new TextView(getContext());
                            citaView.setText("Cita para " + horario + " a las " + fecha);
                            citaView.setTextSize(18);
                            citaView.setPadding(10, 10, 10, 10);
                            linearCitas.addView(citaView);
                        }
                    } else {
                        // Si no hay citas, mostrar un mensaje
                        TextView noCitasView = new TextView(getContext());
                        noCitasView.setText("No hay citas para hoy.");
                        noCitasView.setTextSize(18);
                        noCitasView.setPadding(10, 10, 10, 10);
                        linearCitas.addView(noCitasView);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al obtener las citas", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

