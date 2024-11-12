package com.example.consultapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class InicioAdminFragment extends Fragment {

    private Button btn_cerrarS;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el diseño para este fragmento
        View view = inflater.inflate(R.layout.fragment_inicio_admin, container, false);

        mAuth = FirebaseAuth.getInstance();

        // Usar la vista inflada para encontrar el botón
        btn_cerrarS = view.findViewById(R.id.btn_cerrarS);

        // Configurar el botón para cerrar sesión
        btn_cerrarS.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), login.class);
            startActivity(intent);
            getActivity().finish(); // Cierra la actividad actual
        });

        return view;
    }
}
