package com.example.consultapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FotoActivity extends Activity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private StorageReference storageReference;
    private Uri imageUri;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foto);

        Button selectImageButton = findViewById(R.id.selectImageButton);
        Button uploadButton = findViewById(R.id.uploadButton);
        imageView = findViewById(R.id.imageView);

        // Inicializa Firebase Storage
        storageReference = FirebaseStorage.getInstance().getReference().child("a");

        // Seleccionar imagen
        selectImageButton.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // Subir imagen
        uploadButton.setOnClickListener(view -> {
            if (imageUri != null) {
                StorageReference fileRef = storageReference.child(System.currentTimeMillis() + ".jpg");
                fileRef.putFile(imageUri)
                        .addOnSuccessListener(taskSnapshot ->
                                Toast.makeText(FotoActivity.this, "Imagen subida correctamente", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(FotoActivity.this, "Error al subir la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(FotoActivity.this, "Selecciona una imagen primero", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }
}
