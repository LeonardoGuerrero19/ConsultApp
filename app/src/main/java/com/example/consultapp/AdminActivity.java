package com.example.consultapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Verificar si el usuario está autenticado
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            // Si el usuario no está autenticado, redirigir a la pantalla de inicio de sesión
            startActivity(new Intent(AdminActivity.this, login.class));
            finish(); // Finalizar la actividad actual
            return;
        }

        // Verificar el rol del usuario
        String uid = currentUser.getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(uid).child("rol").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String rol = snapshot.getValue(String.class);
                    if (!"administrador".equals(rol)) {
                        // Si no es administrador, redirigir a la pantalla de inicio de sesión
                        startActivity(new Intent(AdminActivity.this, login.class));
                        finish();
                    }
                } else {
                    // Si no existe el campo "rol", redirigir a la pantalla de inicio de sesión
                    startActivity(new Intent(AdminActivity.this, login.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejar errores de lectura
                startActivity(new Intent(AdminActivity.this, login.class));
                finish();
            }
        });

        // Configurar la Toolbar y el DrawerLayout
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Eliminar el título en la Toolbar
        getSupportActionBar().setTitle("");

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav,
                R.string.close_nav);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.aqua));
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Cargar el fragmento inicial si no hay un estado guardado
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new InicioAdminFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new InicioAdminFragment()).commit();
        } else if (id == R.id.nav_especialidades) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new EspecialidadesFragment()).commit();
        } else if (id == R.id.nav_medicos) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MedicosFragment()).commit();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
