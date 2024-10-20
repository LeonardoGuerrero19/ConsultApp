package com.example.funcion_loginregistro;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.material.snackbar.Snackbar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class VerificacionActivity extends AppCompatActivity {
    EditText veryCode;
    Button btn_verificar;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verificacion);

        veryCode = findViewById(R.id.veryCode);
        btn_verificar = findViewById(R.id.btn_verificar);
        mAuth = FirebaseAuth.getInstance();

        btn_verificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String codigo = veryCode.getText().toString().trim();

                if (codigo.isEmpty()) {
                    Snackbar.make(view, "Por favor ingrese el código de verificación.", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                // Llamar al método de verificación de correo
                checkEmailVerification();

                // Supongamos que has guardado el código en una variable cuando enviaste el correo
                String codigoEsperado = "123456"; // Este es un ejemplo, deberías cambiarlo por el código real

                if (codigo.equals(codigoEsperado)) {
                    Snackbar.make(view, "Código verificado correctamente.", Snackbar.LENGTH_SHORT).show();
                    // Aquí puedes redirigir al usuario a otra actividad
                } else {
                    Snackbar.make(view, "Código incorrecto, inténtelo de nuevo.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Método para verificar si el correo ha sido verificado
    private void checkEmailVerification() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (user.isEmailVerified()) {
                        // El correo está verificado, permite el acceso
                        Snackbar.make(findViewById(R.id.btn_verificar),
                                "Correo verificado. Acceso concedido.",
                                Snackbar.LENGTH_SHORT).show();
                        // Aquí puedes redirigir al usuario a la actividad principal
                    } else {
                        // El correo no está verificado
                        Snackbar.make(findViewById(R.id.btn_verificar),
                                "Por favor verifica tu correo electrónico.",
                                Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
