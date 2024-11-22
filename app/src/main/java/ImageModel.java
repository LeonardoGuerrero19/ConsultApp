package com.example.consultapp;

public class ImageModel {
    private String imageUrl;

    // Constructor vacío para Firestore
    public ImageModel() {
    }

    public ImageModel(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
