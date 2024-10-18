package com.example.c;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CalendarioMedicoViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public CalendarioMedicoViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Calendario de Citas");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
    // TODO: Implement the ViewModel
