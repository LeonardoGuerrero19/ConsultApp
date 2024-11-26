package com.example.consultapp.ui.notificaciones_user;

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
import androidx.lifecycle.ViewModelProvider;

import com.example.consultapp.R;

import java.util.List;
import java.util.Map;

public class NotificationsUserFragment extends Fragment {

    private static final String TAG = "NotificationsUserFragment";
    private LinearLayout linearLayoutNotifications;
    private NotificationsUserViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications_user, container, false);

        // Inicializar el ViewModel
        viewModel = new ViewModelProvider(this).get(NotificationsUserViewModel.class);

        // Inicializar el LinearLayout donde se mostrarán las notificaciones
        linearLayoutNotifications = root.findViewById(R.id.linearLayoutNotificationsUser);

        // Observar las notificaciones desde el ViewModel
        viewModel.getNotifications().observe(getViewLifecycleOwner(), this::mostrarNotificaciones);

        // Observar los errores
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Cargar las notificaciones
        viewModel.loadNotifications();

        return root;
    }

    /**
     * Método para mostrar las notificaciones en la interfaz.
     */
    private void mostrarNotificaciones(List<Map<String, Object>> notifications) {
        // Limpiar el LinearLayout antes de mostrar nuevas notificaciones
        linearLayoutNotifications.removeAllViews();

        if (notifications.isEmpty()) {
            Toast.makeText(getContext(), "No tienes notificaciones.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Agregar cada notificación a la interfaz
        for (Map<String, Object> notification : notifications) {
            agregarNotificacionVista(notification);
        }
    }

    /**
     * Método para agregar una vista de notificación al LinearLayout.
     */
    private void agregarNotificacionVista(Map<String, Object> notification) {
        String mensaje = (String) notification.get("mensaje");
        String notificacionId = (String) notification.get("notificacion_id");

        // Inflar la vista personalizada para la notificación
        View notificacionView = LayoutInflater.from(getContext())
                .inflate(R.layout.item_notification_user, linearLayoutNotifications, false);

        // Inicializar los elementos de la vista
        TextView tvMensaje = notificacionView.findViewById(R.id.notification_user_message);
        Button btnMarcarLeida = notificacionView.findViewById(R.id.btn_mark_as_read_user);

        // Configurar el mensaje de la notificación
        tvMensaje.setText(mensaje);

        // Configurar el botón para marcar la notificación como leída
        btnMarcarLeida.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(getContext())
                    .setTitle("Confirmación")
                    .setMessage("¿Deseas marcar esta notificación como leída?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        // Llamar al ViewModel para marcar como leída
                        viewModel.markNotificationAsRead(notificacionId);
                        // Eliminar la notificación de la vista
                        linearLayoutNotifications.removeView(notificacionView);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // Agregar la vista de la notificación al LinearLayout
        linearLayoutNotifications.addView(notificacionView);
    }
}