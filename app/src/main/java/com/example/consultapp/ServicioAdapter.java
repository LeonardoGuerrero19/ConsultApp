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

    private List<Servicio> listaServicios;

    public ServicioAdapter(List<Servicio> listaServicios) {
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
        Servicio servicio = listaServicios.get(position);
        holder.servicioButton.setText(servicio.getNombre());

        holder.servicioButton.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ServicioDetalle.class);
            intent.putExtra("NOMBRE_SERVICIO", servicio.getNombre());
            intent.putExtra("DESCRIPCION_SERVICIO", servicio.getDescripcion());
            intent.putExtra("IMAGEN_URL", servicio.getImagenUrl());
            v.getContext().startActivity(intent);
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