package com.example.consultapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CitasAdapter extends RecyclerView.Adapter<CitasAdapter.CitaViewHolder> {

    private List<Cita> citasList;

    public CitasAdapter(List<Cita> citasList) {
        this.citasList = citasList;
    }

    @NonNull
    @Override
    public CitaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cita, parent, false);
        return new CitaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CitaViewHolder holder, int position) {
        Cita cita = citasList.get(position);
        holder.textServicio.setText("Servicio: " + cita.getServicio());
        holder.textFecha.setText("Fecha: " + cita.getFecha());
        holder.textHorario.setText("Horario: " + cita.getHorario());
    }

    @Override
    public int getItemCount() {
        return citasList.size();
    }

    public static class CitaViewHolder extends RecyclerView.ViewHolder {
        TextView textServicio, textFecha, textHorario;

        public CitaViewHolder(@NonNull View itemView) {
            super(itemView);
            textServicio = itemView.findViewById(R.id.textServicio);
            textFecha = itemView.findViewById(R.id.textFecha);
            textHorario = itemView.findViewById(R.id.textHorario);
        }
    }
}
