package com.example.consultapp.ui.pacientes;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PacientesViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public PacientesViewModel() {
        mText = new MutableLiveData<>();
    }

    public LiveData<String> getText() {
        return mText;
    }
}