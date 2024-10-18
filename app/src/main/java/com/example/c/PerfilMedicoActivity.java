package com.example.c;

import static android.content.Intent.getIntent;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PerfilMedicoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_perfil_medico);

        // Recibir los datos enviados desde el fragmento
        Intent intent = getIntent();
        String nombre = intent.getStringExtra("nombreMedico");
        String especialidad = intent.getStringExtra("especialidadMedico");
        String descripcion = intent.getStringExtra("descripcionMedico");
        String horario = intent.getStringExtra("horarioMedico");
        String telefono = intent.getStringExtra("telefonoMedico");

        // Mostrar los datos en la interfaz
        TextView txtNombre = findViewById(R.id.medico);
        TextView txtEspecialidad = findViewById(R.id.especialidad);
        TextView txtDescripcion = findViewById(R.id.descrip); //
        TextView txtHorario = findViewById(R.id.horarios); //
        TextView txtTelefono = findViewById(R.id.contacto); //

        txtNombre.setText(nombre);
        txtEspecialidad.setText(especialidad);
        txtDescripcion.setText(descripcion);
        txtHorario.setText(horario);
        txtTelefono.setText(telefono);
    }
}

