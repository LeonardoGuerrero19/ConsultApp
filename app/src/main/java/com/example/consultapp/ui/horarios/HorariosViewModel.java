package com.example.consultapp.ui.horarios;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HorariosViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public HorariosViewModel() {
        mText = new MutableLiveData<>();
    }

    public LiveData<String> getText() {
        return mText;
    }
}