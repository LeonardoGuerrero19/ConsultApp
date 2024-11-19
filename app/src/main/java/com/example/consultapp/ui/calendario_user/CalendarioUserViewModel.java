package com.example.consultapp.ui.calendario_user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CalendarioUserViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public CalendarioUserViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is dashboard fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}