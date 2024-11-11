package com.example.consultapp;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ServicioAdapter extends RecyclerView.Adapter<ServicioAdapter.ServicioViewHolder> {

    private List<String> listaServicios;

    public ServicioAdapter(List<String> listaServicios) {
        this.listaServicios = listaServicios;
    }

    @NonNull
    @Override
    public ServicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_servicio, parent, false);
        return new ServicioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServicioViewHolder holder, int position) {
        String servicio = listaServicios.get(position);
        holder.servicioButton.setText(servicio);

        // Establece un listener de clic para cada botón de servicio
        holder.servicioButton.setOnClickListener(v -> {
            // Crea un Intent para abrir la ServicioDetalle
            Intent intent = new Intent(v.getContext(), ServicioDetalle.class);
            intent.putExtra("NOMBRE_SERVICIO", servicio); // Pasa el nombre del servicio
            v.getContext().startActivity(intent); // Inicia la actividad
        });
    }


    @Override
    public int getItemCount() {
        return listaServicios.size();
    }

    public static class ServicioViewHolder extends RecyclerView.ViewHolder {
        Button servicioButton;

        public ServicioViewHolder(@NonNull View itemView) {
            super(itemView);
            servicioButton = itemView.findViewById(R.id.servicioButton);
        }
    }
}
