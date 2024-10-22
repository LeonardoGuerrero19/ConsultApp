package com.example.proyecto_consultapp.ui.inicio;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class InicioViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public InicioViewModel() {
        mText = new MutableLiveData<>();
        // Obtener el usuario actual de FirebaseAuth
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Obtener el nombre del usuario
            String displayName = user.getDisplayName();
            // Verificar si el nombre es null
            if (displayName == null || displayName.isEmpty()) {
                displayName = "Usuario";
            }
            // Actualizar el LiveData con el nombre del usuario
            mText.setValue("Hola, " + displayName);
        } else {
            mText.setValue("Hola, usuario invitado");
        }
    }

    public LiveData<String> getText() {
        return mText;
    }
}
