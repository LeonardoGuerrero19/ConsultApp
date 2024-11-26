package com.example.consultapp.ui.notificaciones_user;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationsUserViewModel extends ViewModel {

    private static final String TAG = "NotificationsUserVM";
    private final DatabaseReference dbRef;
    private final FirebaseAuth mAuth;

    private final MutableLiveData<List<Map<String, Object>>> notificationsLiveData;
    private final MutableLiveData<String> errorLiveData;

    public NotificationsUserViewModel() {
        dbRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        notificationsLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
    }

    public LiveData<List<Map<String, Object>>> getNotifications() {
        return notificationsLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public void loadNotifications() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Log.e(TAG, "Usuario no autenticado.");
            errorLiveData.setValue("Usuario no autenticado.");
            return;
        }

        dbRef.child("Notificaciones_user").child(userId)
                .orderByChild("estado")
                .equalTo("no_leido") // Filtrar solo las notificaciones no leídas
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Map<String, Object>> notifications = new ArrayList<>();

                        for (DataSnapshot notificacionSnapshot : snapshot.getChildren()) {
                            Map<String, Object> notification = new HashMap<>();
                            notification.put("mensaje", notificacionSnapshot.child("mensaje").getValue(String.class));
                            notification.put("informe_id", notificacionSnapshot.child("informe_id").getValue(String.class));
                            notification.put("notificacion_id", notificacionSnapshot.getKey());
                            notifications.add(notification);
                        }

                        notificationsLiveData.setValue(notifications);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error al cargar notificaciones: " + error.getMessage());
                        errorLiveData.setValue("Error al cargar notificaciones.");
                    }
                });
    }

    public void markNotificationAsRead(String notificationId) {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null || notificationId == null) {
            Log.e(TAG, "Datos insuficientes para marcar la notificación como leída.");
            errorLiveData.setValue("Error al marcar la notificación.");
            return;
        }

        dbRef.child("Notificaciones_user").child(userId).child(notificationId).child("estado").setValue("leída")
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Notificación marcada como leída correctamente."))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al marcar notificación como leída: " + e.getMessage());
                    errorLiveData.setValue("Error al marcar la notificación.");
                });
    }
}
