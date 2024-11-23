package com.example.consultapp.ui.medicos;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MedicosViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public MedicosViewModel() {
        mText = new MutableLiveData<>();
    }

    public LiveData<String> getText() {
        return mText;
    }
}
