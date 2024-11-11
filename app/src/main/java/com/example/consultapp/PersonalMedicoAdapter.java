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

import com.google.firebase.firestore.FirebaseFirestore;

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
                .inflate(R.layout.item_personal_medico, parent, false); // Cambia el layout según tu diseño
        return new PersonalMedicoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PersonalMedicoViewHolder holder, int position) {
        Medico medico = medicoList.get(position);
        holder.doctorNameTextView.setText(medico.getNombre());
        holder.doctorServiceTextView.setText(medico.getEspecializacion()); // Enlazar especialización

        // Funcionalidad para el botón de ver
        holder.btnVer.setOnClickListener(v -> {
            Intent intent = new Intent(context, PerfilPersonalMedico.class);
            intent.putExtra("medicoId", medico.getId()); // Ahora debería funcionar sin error
            context.startActivity(intent);
        });

        // Funcionalidad para el botón de editar
        holder.btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditarPersonalMedico.class);
            intent.putExtra("medicoId", medico.getId()); // Ahora debería funcionar sin error
            context.startActivity(intent);
        });


        // Funcionalidad para el botón de eliminar
        holder.btnEliminar.setOnClickListener(v -> {
            // Llamamos al método para eliminar al médico
            eliminarMedico(medico.getId());
        });
    }

    @Override
    public int getItemCount() {
        return medicoList.size();
    }

    private void eliminarMedico(String medicoId) {
        // Referencia a la colección 'user' donde están los médicos
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Eliminar el médico de la base de datos por su ID
        db.collection("user").document(medicoId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Notificamos que se eliminó el médico y actualizamos el RecyclerView
                    // Si necesitas actualizar la lista, puedes eliminar el objeto de la lista y notificar al adaptador
                    for (Medico medico : medicoList) {
                        if (medico.getId().equals(medicoId)) {
                            medicoList.remove(medico);
                            notifyDataSetChanged();
                            break;
                        }
                    }
                    Toast.makeText(context, "Médico eliminado", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Manejo de errores
                    Toast.makeText(context, "Error al eliminar médico", Toast.LENGTH_SHORT).show();
                });
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
