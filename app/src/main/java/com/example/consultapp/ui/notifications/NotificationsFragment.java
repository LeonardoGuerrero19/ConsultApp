package com.example.consultapp.ui.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.consultapp.R;
import com.example.consultapp.databinding.FragmentNotificationsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private LinearLayout linearLayoutNotifications;
    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;
    private String userId; // Variable para almacenar el ID del usuario

    private static final String TAG = "NotificationsFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        linearLayoutNotifications = binding.linearLayoutNotifications;
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        // Obtener el ID del usuario
        userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (userId == null) {
            Log.e(TAG, "Usuario no autenticado.");
            Toast.makeText(getContext(), "Usuario no autenticado.", Toast.LENGTH_SHORT).show();
            return root;
        }

        Log.d(TAG, "Usuario autenticado: " + userId);
        cargarNotificaciones();
        return root;
    }

    private void cargarNotificaciones() {
        if (userId == null) {
            Toast.makeText(getContext(), "Usuario no autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener el nombre del médico autenticado
        dbRef.child("Medicos").child(userId).child("nombre").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String nombreMedico = snapshot.getValue(String.class);

                if (nombreMedico == null) {
                    Log.e("NotificationsFragment", "No se encontró el nombre del médico autenticado.");
                    Toast.makeText(getContext(), "Error al obtener datos del médico.", Toast.LENGTH_SHORT).show();
                    return;
                }

                cargarNotificacionesParaMedico(nombreMedico);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("NotificationsFragment", "Error al cargar el nombre del médico: " + error.getMessage());
                Toast.makeText(getContext(), "Error al cargar datos.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarNotificacionesParaMedico(String nombreMedico) {
        dbRef.child("Notificaciones").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                linearLayoutNotifications.removeAllViews();

                for (DataSnapshot medicoSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot notificacionSnapshot : medicoSnapshot.getChildren()) {
                        String notificacionNombreMedico = notificacionSnapshot.child("nombreMedico").getValue(String.class);

                        if (nombreMedico.equals(notificacionNombreMedico)) {
                            agregarNotificacionVista(notificacionSnapshot);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("NotificationsFragment", "Error al cargar notificaciones: " + error.getMessage());
                Toast.makeText(getContext(), "Error al cargar notificaciones.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void agregarNotificacionVista(DataSnapshot notificacionSnapshot) {
        String titulo = notificacionSnapshot.child("titulo").getValue(String.class);
        String mensaje = notificacionSnapshot.child("mensaje").getValue(String.class);
        String estado = notificacionSnapshot.child("estado").getValue(String.class);
        String notificacionId = notificacionSnapshot.getKey();

        Log.d(TAG, "Creando vista para la notificación. Título: " + titulo + ", Estado: " + estado);

        View notificacionView = LayoutInflater.from(getContext())
                .inflate(R.layout.item_notification, linearLayoutNotifications, false);

        TextView tvTitulo = notificacionView.findViewById(R.id.notification_title);
        TextView tvMensaje = notificacionView.findViewById(R.id.notification_message);
        TextView tvEstado = notificacionView.findViewById(R.id.notification_status);
        Button btnMarcarLeida = notificacionView.findViewById(R.id.btn_mark_as_read);

        tvTitulo.setText(titulo != null ? titulo : "Sin título");
        tvMensaje.setText(mensaje != null ? mensaje : "Sin mensaje");
        tvEstado.setText(estado != null ? estado : "Sin estado");

        btnMarcarLeida.setOnClickListener(v -> {
            Log.d(TAG, "Botón 'Marcar como leída' presionado para la notificación: " + notificacionId);
            new android.app.AlertDialog.Builder(getContext())
                    .setTitle("Confirmación")
                    .setMessage("¿Deseas marcar esta notificación como leída?")
                    .setPositiveButton("Sí", (dialog, which) -> marcarNotificacionComoLeida(notificacionId, notificacionView))
                    .setNegativeButton("No", null)
                    .show();
        });

        linearLayoutNotifications.addView(notificacionView);
        Log.d(TAG, "Vista de notificación añadida al LinearLayout.");
    }

    private void marcarNotificacionComoLeida(String notificacionId, View notificacionView) {
        if (userId == null || notificacionId == null) {
            Log.e(TAG, "Datos de notificación no válidos. userId o notificacionId son nulos.");
            Toast.makeText(getContext(), "Datos de notificación no válidos.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Marcando notificación como leída. ID: " + notificacionId);
        dbRef.child("Notificaciones").child(userId).child(notificacionId).child("estado").setValue("leída")
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Notificación marcada como leída. Eliminando de la vista.");
                    linearLayoutNotifications.removeView(notificacionView);
                    Toast.makeText(getContext(), "Notificación marcada como leída.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al marcar como leída: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al marcar como leída.", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
