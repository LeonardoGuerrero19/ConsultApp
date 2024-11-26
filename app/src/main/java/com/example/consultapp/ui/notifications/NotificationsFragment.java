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
        dbRef.child("notificaciones")
                .orderByChild("nombre_medico")
                .equalTo(nombreMedico) // Filtrar por el nombre del médico
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        linearLayoutNotifications.removeAllViews();

                        boolean hasUnreadNotifications = false;

                        for (DataSnapshot notificacionSnapshot : snapshot.getChildren()) {
                            String estado = notificacionSnapshot.child("estado").getValue(String.class);

                            // Mostrar solo notificaciones en estado "no_leido"
                            if ("no_leido".equals(estado)) {
                                agregarNotificacionVista(notificacionSnapshot);
                                hasUnreadNotifications = true;
                            }
                        }

                        if (!hasUnreadNotifications) {
                            Toast.makeText(getContext(), "No tienes notificaciones no leídas.", Toast.LENGTH_SHORT).show();
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
        String mensaje = notificacionSnapshot.child("mensaje").getValue(String.class);
        String estado = notificacionSnapshot.child("estado").getValue(String.class);
        String fecha = notificacionSnapshot.child("fecha").getValue(String.class);
        String hora = notificacionSnapshot.child("hora").getValue(String.class);
        String notificacionId = notificacionSnapshot.getKey();

        View notificacionView = LayoutInflater.from(getContext())
                .inflate(R.layout.item_notification, linearLayoutNotifications, false);

        TextView tvMensaje = notificacionView.findViewById(R.id.notification_message);
        TextView tvEstado = notificacionView.findViewById(R.id.notification_status);
        TextView tvFechaHora = notificacionView.findViewById(R.id.notification_date_time);
        Button btnMarcarLeida = notificacionView.findViewById(R.id.btn_mark_as_read);

        tvMensaje.setText(mensaje != null ? mensaje : "Sin mensaje");
        tvEstado.setText(estado != null ? estado : "no_leido");
        tvFechaHora.setText(fecha != null && hora != null ? fecha + " " + hora : "Sin fecha/hora");

        btnMarcarLeida.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(getContext())
                    .setTitle("Confirmación")
                    .setMessage("¿Deseas marcar esta notificación como leída?")
                    .setPositiveButton("Sí", (dialog, which) -> marcarNotificacionComoLeida(notificacionId, notificacionView))
                    .setNegativeButton("No", null)
                    .show();
        });

        linearLayoutNotifications.addView(notificacionView);
    }

    private void marcarNotificacionComoLeida(String notificacionId, View notificacionView) {
        if (notificacionId == null) {
            Log.e(TAG, "ID de notificación nulo. No se puede marcar como leída.");
            Toast.makeText(getContext(), "Error al marcar como leída.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Acceder directamente al nodo de la notificación y actualizar el estado
        dbRef.child("notificaciones").child(notificacionId).child("estado").setValue("leída")
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Notificación marcada como leída correctamente.");
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
