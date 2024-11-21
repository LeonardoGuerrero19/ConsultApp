package com.example.funcion_loginregistro;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class BuscarUsuarioActivity extends AppCompatActivity {

    private EditText etNumeroCuenta;
    private Button btnBuscar;
    private TextView tvResultado;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_usuario);

        // Inicializar elementos de la interfaz
        etNumeroCuenta = findViewById(R.id.etNumeroCuenta);
        btnBuscar = findViewById(R.id.btnBuscar);
        tvResultado = findViewById(R.id.tvResultado);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Configurar botón de búsqueda
        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscarUsuario();
            }
        });
    }

    private void buscarUsuario() {
        // Obtener el número de cuenta ingresado por el usuario
        String numeroCuenta = etNumeroCuenta.getText().toString().trim();

        // Verificar si el número de cuenta no está vacío
        if (numeroCuenta.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese un número de cuenta", Toast.LENGTH_SHORT).show();
            return;
        }

        // Referencia a la colección de usuarios en Firestore
        CollectionReference usuariosRef = db.collection("user");

        // Buscar documentos donde el campo "numeroCuenta" coincida con el número ingresado
        usuariosRef.whereEqualTo("numeroCuenta", numeroCuenta)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                // Mostrar los datos del usuario encontrado
                                StringBuilder resultado = new StringBuilder();
                                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                    String nombre = document.getString("nombre");
                                    String correo = document.getString("correo");
                                    resultado.append("Nombre: ").append(nombre).append("\n");
                                    resultado.append("Correo: ").append(correo).append("\n\n");
                                }
                                tvResultado.setText(resultado.toString());
                            } else {
                                // No se encontró el usuario
                                tvResultado.setText("No se encontró un usuario con ese número de cuenta.");
                            }
                        } else {
                            // Error en la consulta
                            Toast.makeText(BuscarUsuarioActivity.this, "Error al buscar usuario", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
