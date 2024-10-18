package com.example.c;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class PersonalMedicoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MedicoAdapter medicoAdapter;
    private ArrayList<Medico> listaMedicos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_personal_medico);

        recyclerView = findViewById(R.id.listaDoctors);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Simulación de datos de médicos
        listaMedicos = new ArrayList<>();
        listaMedicos.add(new Medico("Dr. Juan Pérez", "Patología", "0000-587-4741"));
        listaMedicos.add(new Medico("Dra. Ana Martínez", "Ginecología", "0000-587-4742"));

        medicoAdapter = new MedicoAdapter(listaMedicos);
        recyclerView.setAdapter(medicoAdapter);
    }
}
