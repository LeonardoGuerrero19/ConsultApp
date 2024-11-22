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
        String[] fechaPartes = cita.getFecha().split("/");
        if (fechaPartes.length == 3) {
            String dia = fechaPartes[0];
            String mes = getNombreMes(Integer.parseInt(fechaPartes[1]));
            holder.textDia.setText(dia);
            holder.textMes.setText(mes);
        }
        holder.textServicio.setText(cita.getServicio());
        holder.textHorario.setText(cita.getHorario());
        holder.textDoctor.setText("Dr. " + cita.getDoctor());
    }

    private String getNombreMes(int numeroMes) {
        String[] meses = {
                "ENE", "FEB", "MAR", "ABR", "MAY", "JUN",
                "JUL", "AGO", "SEPT", "OCT", "NOV", "DIC"
        };
        return meses[numeroMes - 1];
    }

    @Override
    public int getItemCount() {
        return citasList.size();
    }

    public static class CitaViewHolder extends RecyclerView.ViewHolder {
        TextView textServicio, textDia, textMes, textHorario, textDoctor;

        public CitaViewHolder(@NonNull View itemView) {
            super(itemView);
            textServicio = itemView.findViewById(R.id.textServicio);
            textDia = itemView.findViewById(R.id.textDia);
            textMes = itemView.findViewById(R.id.textMes);
            textHorario = itemView.findViewById(R.id.textHorario);
            textDoctor = itemView.findViewById(R.id.textDoctor);
        }
    }
}
