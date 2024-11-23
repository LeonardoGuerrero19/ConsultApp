package com.example.consultapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class PersonalMedicoAdapter extends RecyclerView.Adapter<PersonalMedicoAdapter.PersonalMedicoViewHolder> {

    private List<Medico> medicoList;
    private Context context;

    public PersonalMedicoAdapter(List<Medico> medicoList, Context context) {
        this.medicoList = medicoList;
        this.context = context;
    }

    @Override
    public PersonalMedicoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_personal_medico, parent, false);
        return new PersonalMedicoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PersonalMedicoViewHolder holder, int position) {
        Medico medico = medicoList.get(position);
        holder.doctorNameTextView.setText(medico.getNombre());
        holder.doctorServiceTextView.setText(medico.getEspecializacion());

        // Funcionalidad para el botón de ver
        holder.btnVer.setOnClickListener(v -> {
            Intent intent = new Intent(context, PerfilPersonalMedico.class);
            intent.putExtra("medicoId", medico.getId());
            context.startActivity(intent);
        });

        // Funcionalidad para el botón de editar
        holder.btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditarPersonalMedico.class);
            intent.putExtra("medicoId", medico.getId());
            context.startActivity(intent);
        });

        // Funcionalidad para el botón de eliminar
        holder.btnEliminar.setOnClickListener(v -> eliminarMedico(medico.getId(), position));
    }

    @Override
    public int getItemCount() {
        return medicoList.size();
    }

    private void eliminarMedico(String medicoId, int position) {
        // Referencias a las ramas "users" y "Medicos"
        DatabaseReference medicosRef = FirebaseDatabase.getInstance().getReference().child("Medicos").child(medicoId);

        // Eliminar el médico de ambas ramas
        medicosRef.removeValue()
                .addOnSuccessListener(aVoid -> medicosRef.removeValue()
                        .addOnSuccessListener(aVoid1 -> {
                            // Remover al médico de la lista local y notificar al adaptador
                            medicoList.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(context, "Médico eliminado de ambas ramas", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(context, "Error al eliminar médico de la rama Medicos", Toast.LENGTH_SHORT).show()))
                .addOnFailureListener(e -> Toast.makeText(context, "Error al eliminar médico de la rama users", Toast.LENGTH_SHORT).show());
    }

    public static class PersonalMedicoViewHolder extends RecyclerView.ViewHolder {
        public TextView doctorNameTextView;
        public TextView doctorServiceTextView;
        public ImageButton btnVer;
        public ImageButton btnEditar;
        public ImageButton btnEliminar;

        public PersonalMedicoViewHolder(View itemView) {
            super(itemView);
            doctorNameTextView = itemView.findViewById(R.id.doctorNameTextView);
            doctorServiceTextView = itemView.findViewById(R.id.doctorServiceTextView);
            btnVer = itemView.findViewById(R.id.btn_ver);
            btnEditar = itemView.findViewById(R.id.btn_editar);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar);
        }
    }
}