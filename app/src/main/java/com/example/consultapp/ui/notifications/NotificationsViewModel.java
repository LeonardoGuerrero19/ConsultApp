package com.example.consultapp.ui.notifications;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NotificationsViewModel extends ViewModel {

    private final MutableLiveData<List<Map<String, String>>> mNotificaciones;

    public NotificationsViewModel() {
        mNotificaciones = new MutableLiveData<>();
        mNotificaciones.setValue(new ArrayList<>());
        Log.d("NotificationsViewModel", "ViewModel inicializado con una lista vacía.");
    }

    public LiveData<List<Map<String, String>>> getNotificaciones() {
        Log.d("NotificationsViewModel", "getNotificaciones() llamado.");
        return mNotificaciones;
    }

    public void setNotificaciones(List<Map<String, String>> nuevasNotificaciones) {
        Log.d("NotificationsViewModel", "setNotificaciones() llamado con " + nuevasNotificaciones.size() + " notificaciones.");
        mNotificaciones.setValue(nuevasNotificaciones);
        Log.d("NotificationsViewModel", "Lista de notificaciones actualizada. Total de notificaciones: " + nuevasNotificaciones.size());
    }
}
