package com.example.consultapp.ui.especialidades;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EspecialidadesViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public EspecialidadesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is dashboard fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}