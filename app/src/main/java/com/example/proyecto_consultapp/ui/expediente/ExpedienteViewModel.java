package com.example.proyecto_consultapp.ui.expediente;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ExpedienteViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ExpedienteViewModel() {
        mText = new MutableLiveData<>();
    }

    public LiveData<String> getText() {
        return mText;
    }
}