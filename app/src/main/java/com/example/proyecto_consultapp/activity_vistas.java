package com.example.proyecto_consultapp;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;  // Importar la clase Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.proyecto_consultapp.databinding.ActivityVistasBinding;

public class activity_vistas extends AppCompatActivity {

    private ActivityVistasBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityVistasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar la Toolbar como ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);  // Encontrar la Toolbar por su id
        setSupportActionBar(toolbar);  // Establecer la Toolbar como ActionBar

        BottomNavigationView navView = findViewById(R.id.nav_view);

        // Configuración del AppBar para las vistas principales de navegación
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_inicio, R.id.navigation_calendario, R.id.navigation_expediente)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_vistas);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    // Sobrescribir para manejar la navegación hacia arriba
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_vistas);
        return NavigationUI.navigateUp(navController, new AppBarConfiguration.Builder(navController.getGraph()).build())
                || super.onSupportNavigateUp();
    }
}
