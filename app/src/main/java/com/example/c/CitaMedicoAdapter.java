package com.example.c;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CitaMedicoAdapter extends RecyclerView.Adapter<CitaMedicoAdapter.ViewHolder> {
    private List<CitaMedico> citas;

    public CitaMedicoAdapter(List<CitaMedico> citas) {
        this.citas = citas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_citamedico, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CitaMedico cita = citas.get(position);
        holder.tvNumeroCita.setText(cita.getNumeroCita());
        holder.tvPaciente.setText(cita.getNombrePaciente());
        holder.tvHora.setText(cita.getHoraCita());

        // Configura los botones para confirmar asistencia/cancelación
        holder.btnConfirmarAsistencia.setOnClickListener(v -> {
            // Lógica para confirmar asistencia
        });

        holder.btnConfirmarCancelacion.setOnClickListener(v -> {
            // Lógica para confirmar cancelación
        });
    }

    @Override
    public int getItemCount() {
        return citas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumeroCita;
        TextView tvPaciente;
        TextView tvHora;
        ImageButton btnConfirmarAsistencia;
        ImageButton btnConfirmarCancelacion;

        public ViewHolder(View itemView) {
            super(itemView);
            tvNumeroCita = itemView.findViewById(R.id.tvNumeroCita);
            tvPaciente = itemView.findViewById(R.id.tvPaciente);
            tvHora = itemView.findViewById(R.id.tvHora);
            btnConfirmarAsistencia = itemView.findViewById(R.id.btnConfirmarAsistencia);
            btnConfirmarCancelacion = itemView.findViewById(R.id.btnConfirmarCancelacion);
        }
    }
}

