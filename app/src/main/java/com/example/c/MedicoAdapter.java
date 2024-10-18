package com.example.c;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class MedicoAdapter extends RecyclerView.Adapter<MedicoAdapter.MedicoViewHolder> {

    private ArrayList<Medico> listaMedicos;

    public MedicoAdapter(ArrayList<Medico> listaMedicos) {
        this.listaMedicos = listaMedicos;
    }

    @NonNull
    @Override
    public MedicoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflar el layout del RecyclerView (CardView) con el diseño proporcionado
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lista_personalmedico, parent, false);
        return new MedicoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicoViewHolder holder, int position) {
        // Obtener los datos del médico en la posición actual
        Medico medico = listaMedicos.get(position);

        // Configurar el ViewHolder con los datos correspondientes
        holder.DoctorName.setText(medico.getNombre());
        holder.tvSpecialty.setText(medico.getEspecialidad());
        holder.Phone.setText(medico.getTelefono());
        holder.DoctorIcon.setImageResource(R.drawable.ic_medico); // Asegúrate de tener este drawable
    }

    @Override
    public int getItemCount() {
        return listaMedicos.size(); // Devolver el tamaño de la lista
    }

    public class MedicoViewHolder extends RecyclerView.ViewHolder {

        // Elementos de la vista (del layout del item de RecyclerView)
        ImageView DoctorIcon;
        TextView DoctorName;
        TextView tvSpecialty;
        TextView Phone;

        public MedicoViewHolder(@NonNull View itemView) {
            super(itemView);

            // Inicializar las vistas
            DoctorIcon = itemView.findViewById(R.id.DoctorIcon);
            DoctorName = itemView.findViewById(R.id.DoctorName);
            tvSpecialty = itemView.findViewById(R.id.tvSpecialty);
            Phone = itemView.findViewById(R.id.Phone);
        }
    }
}
