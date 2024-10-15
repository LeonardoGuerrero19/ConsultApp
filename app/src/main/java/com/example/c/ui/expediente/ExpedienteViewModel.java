package com.example.c.ui.expediente;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ExpedienteViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ExpedienteViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Este es el fragmento de expediente"); // Personaliza el texto según sea necesario
    }

    public LiveData<String> getText() {
        return mText;
    }
}
