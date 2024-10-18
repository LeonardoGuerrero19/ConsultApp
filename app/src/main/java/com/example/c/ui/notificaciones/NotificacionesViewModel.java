package com.example.c.ui.notificaciones;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NotificacionesViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public NotificacionesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Avisos Importantes");
    }

    public LiveData<String> getText() {
        return mText;
    }
}