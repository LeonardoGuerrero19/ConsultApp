package com.example.proyecto_consultapp.ui.inicio;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.proyecto_consultapp.R;
import com.example.proyecto_consultapp.activity_login;
import com.example.proyecto_consultapp.databinding.FragmentInicioBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class InicioFragment extends Fragment {

    private FragmentInicioBinding binding;
    private InicioViewModel inicioViewModel;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private Button btn_cerrarS;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        inicioViewModel =
                new ViewModelProvider(this).get(InicioViewModel.class);

        binding = FragmentInicioBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        final TextView textView = binding.txtSaludo;
        btn_cerrarS = binding.btnCerrarS;  // Asignación correcta de btn_cerrarS utilizando ViewBinding

        // Obtener el usuario actual
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DocumentReference docRef = mFirestore.collection("user").document(userId);
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String nombreUsuario = document.getString("nombre");
                        textView.setText("Hola, " + nombreUsuario);
                    }
                }
            });
        }

        // Asignar funcionalidad al botón de cerrar sesión
        btn_cerrarS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();  // Cerrar sesión de Firebase

                // Iniciar la actividad de login
                Intent intent = new Intent(getActivity(), activity_login.class);
                startActivity(intent);

                // Cerrar la actividad actual
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
