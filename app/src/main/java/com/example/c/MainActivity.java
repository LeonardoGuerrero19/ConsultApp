package com.example.c;

import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.c.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Verifica qué tipo de usuario está logueado (paciente o médico)
        boolean esMedico = verificarSiEsMedico(); // Implementa esta lógica según tu aplicación

        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration;
        NavController navController;

        // Dependiendo del usuario, se carga el archivo de navegación correcto
        if (esMedico) {
            // Configuración para médico
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_calendariomedico, R.id.navigation_historial, R.id.navigation_perfil)
                    .build();
            navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(binding.navView, navController);
        } else {
            // Configuración para paciente
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_inicio, R.id.navigation_calendario, R.id.navigation_expediente, R.id.navigation_notificaciones)
                    .build();
            navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(binding.navView, navController);
        }
    }

    // Método para determinar si es un médico
    private boolean verificarSiEsMedico() {
        // Implementa la lógica para saber si el usuario logueado es médico o paciente
        // Por ejemplo, puedes verificar el rol del usuario en tu base de datos o SharedPreferences
        return false; // Modifica esto según tu lógica
    }
}
