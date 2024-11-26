package com.example.consultapp;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditarPersonalMedico extends AppCompatActivity {

    private TextView nombre;
    private EditText nombreMedico, telefono;
    private Button btn_guardar, horario;
    private Spinner especializacion;
    private String medicoId;
    private DatabaseReference databaseReference;
    private List<String> horarioSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_personal_medico);

        // Obtener el ID del médico del Intent
        medicoId = getIntent().getStringExtra("medicoId");

        // Referencias a los elementos del layout
        nombre = findViewById(R.id.medicoTextView);
        nombreMedico = findViewById(R.id.editNombre);
        especializacion = findViewById(R.id.spinner_especializacion);
        telefono = findViewById(R.id.editTelefono);
        horario = findViewById(R.id.editHorario);
        btn_guardar = findViewById(R.id.btnGuardarCambios);

        // Listener para mostrar el diálogo para seleccionar el horario
        horario.setOnClickListener(v -> showBottomDialog());

        // Inicializar la referencia de la base de datos
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Cargar especializaciones desde Realtime Database
        cargarEspecializacionesDesdeRealtime();

        // Cargar los datos del médico desde Realtime Database
        cargarDatosMedico();

        // Acción para guardar los cambios
        btn_guardar.setOnClickListener(view -> {
            String nuevoNombre = nombreMedico.getText().toString();
            String nuevaEspecializacion = especializacion.getSelectedItem().toString();
            String nuevoTelefono = telefono.getText().toString();

            // Validar que los campos no estén vacíos
            if (nuevoNombre.isEmpty() || nuevaEspecializacion.isEmpty() || nuevoTelefono.isEmpty() || horarioSeleccionado == null || horarioSeleccionado.isEmpty()) {
                Toast.makeText(EditarPersonalMedico.this, "Todos los campos deben ser llenados", Toast.LENGTH_SHORT).show();
                return;
            }

            // Actualizar los datos en Realtime Database
            actualizarDatosMedico(nuevoNombre, nuevaEspecializacion, nuevoTelefono, horarioSeleccionado);
        });
    }

    private void cargarDatosMedico() {
        databaseReference.child("Medicos").child(medicoId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Medico medico = snapshot.getValue(Medico.class);
                    if (medico != null) {
                        nombre.setText("Dr. " + medico.getNombre());
                        nombreMedico.setText(medico.getNombre());
                        telefono.setText(medico.getTelefono());

                        // Establecer la especialización seleccionada
                        String especializacionMedico = medico.getEspecializacion();
                        if (especializacionMedico != null && especializacion.getAdapter() != null) {
                            ArrayAdapter adapter = (ArrayAdapter) especializacion.getAdapter();
                            int position = adapter.getPosition(especializacionMedico);
                            if (position >= 0) {
                                especializacion.setSelection(position);
                            }
                        }

                        // Obtener horarios y mostrar el rango
                        List<String> horarios = medico.getHorarios();
                        if (horarios != null && !horarios.isEmpty()) {
                            horarioSeleccionado = horarios; // Guardar los horarios en la lista
                            String primerHorario = horarios.get(0);
                            String ultimoHorario = horarios.get(horarios.size() - 1);
                            horario.setText(String.format("%s - %s", primerHorario, ultimoHorario));
                        }
                    }
                } else {
                    Toast.makeText(EditarPersonalMedico.this, "Médico no encontrado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(EditarPersonalMedico.this, "Error al cargar los datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarEspecializacionesDesdeRealtime() {
        databaseReference.child("Servicios").child("TodosLosServicios").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                List<String> especializaciones = new ArrayList<>();
                for (com.google.firebase.database.DataSnapshot especialidadSnapshot : snapshot.getChildren()) {
                    String especializacion = especialidadSnapshot.getValue(String.class);
                    if (especializacion != null) {
                        especializaciones.add(especializacion);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(EditarPersonalMedico.this, R.layout.item_spinner2, especializaciones);
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                especializacion.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                Toast.makeText(EditarPersonalMedico.this, "Error al cargar especializaciones", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarDatosMedico(String nuevoNombre, String nuevaEspecializacion, String nuevoTelefono, List<String> nuevoHorario) {
        // Crear un mapa con los datos actualizados
        Map<String, Object> datosActualizados = new HashMap<>();
        datosActualizados.put("nombre", nuevoNombre);
        datosActualizados.put("especializacion", nuevaEspecializacion);
        datosActualizados.put("telefono", nuevoTelefono);
        datosActualizados.put("horarios", nuevoHorario);

        // Actualizar los datos en la rama "Medicos"
        DatabaseReference medicosRef = databaseReference.child("Medicos").child(medicoId);

        medicosRef.updateChildren(datosActualizados)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditarPersonalMedico.this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
                    finish(); // Cerrar la actividad después de guardar los cambios
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditarPersonalMedico.this, "Error al actualizar los datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showBottomDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheet_layout_doctor);

        // Obtener referencias a los TimePicker del modal
        TimePicker timePickerInicio = dialog.findViewById(R.id.timePickerInicio);
        TimePicker timePickerFin = dialog.findViewById(R.id.timePickerFin);

        // Cambiar según la versión de Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePickerInicio.setHour(timePickerInicio.getHour());
            timePickerInicio.setMinute(timePickerInicio.getMinute());
            timePickerFin.setHour(timePickerFin.getHour());
            timePickerFin.setMinute(timePickerFin.getMinute());
        } else {
            timePickerInicio.setCurrentHour(timePickerInicio.getCurrentHour());
            timePickerInicio.setCurrentMinute(timePickerInicio.getCurrentMinute());
            timePickerFin.setCurrentHour(timePickerFin.getCurrentHour());
            timePickerFin.setCurrentMinute(timePickerFin.getCurrentMinute());
        }

        // Acción para actualizar el horario cuando el usuario cierre el diálogo
        Button btnAceptar = dialog.findViewById(R.id.btnAceptarHorario); // Asegúrate de que el ID sea correcto
        btnAceptar.setOnClickListener(v -> {
            int horaInicio = timePickerInicio.getHour();
            int minutoInicio = timePickerInicio.getMinute();
            int horaFin = timePickerFin.getHour();
            int minutoFin = timePickerFin.getMinute();

            // Generar el array de horarios por intervalos de media hora
            List<String> horarios = generarHorarios(horaInicio, minutoInicio, horaFin, minutoFin);

            if (horarios.isEmpty()) {
                Toast.makeText(this, "El rango de horario no es válido.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Actualizar el horario seleccionado solo si se ha hecho una selección
            if (!horarios.equals(horarioSeleccionado)) {
                horarioSeleccionado = horarios;
            }

            // Actualizar el texto del botón para mostrar el rango
            horario.setText(String.format("%02d:%02d - %02d:%02d", horaInicio, minutoInicio, horaFin, minutoFin));

            dialog.dismiss();
        });

        // Configuración de la ventana emergente
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.show();
    }

    private List<String> generarHorarios(int horaInicio, int minutoInicio, int horaFin, int minutoFin) {
        List<String> horarios = new ArrayList<>();

        while (horaInicio < horaFin || (horaInicio == horaFin && minutoInicio < minutoFin)) {
            // Formatear la hora y minuto en el formato de 24 horas
            horarios.add(String.format("%02d:%02d", horaInicio, minutoInicio));

            minutoInicio += 30;
            if (minutoInicio == 60) {
                minutoInicio = 0;
                horaInicio++;
            }
        }

        return horarios;
    }
}
