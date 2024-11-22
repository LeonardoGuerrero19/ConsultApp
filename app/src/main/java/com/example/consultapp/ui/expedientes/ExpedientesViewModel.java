package com.example.consultapp.ui.expedientes;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ExpedientesViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ExpedientesViewModel() {
        mText = new MutableLiveData<>();
    }

    public LiveData<String> getText() {
        return mText;
    }

}
