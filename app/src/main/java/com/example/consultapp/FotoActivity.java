package com.example.consultapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.storage.FirebaseStorage;


import java.util.UUID;

public class FotoActivity extends AppCompatActivity {
    StorageReference storageReference;
    LinearProgressIndicator progress;
    Uri image;
    MaterialButton selectImage, uploadImage;
    ImageView imageView;

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                if (result.getData() != null) {
                    image = result.getData().getData();
                    uploadImage.setEnabled(true);
                    Glide.with(getApplicationContext()).load(image).into(imageView);
                }
            } else {
                Toast.makeText(FotoActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foto);

        FirebaseApp.initializeApp(FotoActivity.this);
        storageReference = FirebaseStorage.getInstance().getReference();

        // Cambiar a androidx.appcompat.widget.Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progress = findViewById(R.id.progress);
        imageView = findViewById(R.id.imageView);
        selectImage = findViewById(R.id.selectedImage);
        uploadImage = findViewById(R.id.uploadImage);

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                activityResultLauncher.launch(intent);
            }
        });

        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage(image);
            }
        });
    }

    private void uploadImage(Uri image) {
        if (image == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generar un nombre único para la imagen
        String fileName = "imagenes/" + UUID.randomUUID().toString();

        // Referencia al almacenamiento
        StorageReference reference = storageReference.child(fileName);

        // Subir el archivo
        reference.putFile(image)
                .addOnSuccessListener(taskSnapshot -> {
                    // Obtener la URL de descarga
                    reference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();

                        // Guardar la URL en Firestore
                        saveImageUrlToFirestore(imageUrl);
                    }).addOnFailureListener(e -> {
                        Toast.makeText(FotoActivity.this, "Error al obtener la URL de la imagen", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(FotoActivity.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                })
                .addOnProgressListener(snapshot -> {
                    progress.setMax((int) snapshot.getTotalByteCount());
                    progress.setProgress((int) snapshot.getBytesTransferred());
                });
    }

    private void saveImageUrlToFirestore(String imageUrl) {
        // Instancia de Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Crear un documento con la URL de la imagen
        db.collection("imagenes")
                .add(new com.example.consultapp.ImageModel(imageUrl)) // Puedes usar una clase modelo
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(FotoActivity.this, "URL guardada en Firestore", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(FotoActivity.this, "Error al guardar en Firestore", Toast.LENGTH_SHORT).show();
                });
    }
}