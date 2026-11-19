package com.siit.recipeapp.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.siit.recipeapp.R;
import com.siit.recipeapp.data.repository.AuthRepository;
import com.siit.recipeapp.data.repository.firebase.FirebaseAuthRepository;
import com.siit.recipeapp.domain.validation.AuthValidator;

public class RegisterFragment extends Fragment {

    // Veze
    private final AuthRepository authRepository = new FirebaseAuthRepository();

    // Constructor
    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ===== Povezivanje =====

        TextInputEditText etUsername = view.findViewById(R.id.et_register_username);
        TextInputEditText etEmail = view.findViewById(R.id.et_register_email);
        TextInputEditText etPassword = view.findViewById(R.id.et_register_password);

        TextView tvError = view.findViewById(R.id.tv_register_error);

        Button btnBack = view.findViewById(R.id.btn_register_back);
        Button btnRegister = view.findViewById(R.id.btn_register_sumbit);

        // ===== Navigacija =====

        // Povratak na Login
        btnBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        // ===== Funkcionalnosti =====

        // Registracija korisnika
        btnRegister.setOnClickListener(v -> {

            // Sakrij prethodnu gresku
            tvError.setVisibility(View.GONE);

            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();

            // Minimalna validacija
            if (!AuthValidator.isValidUsername(username)
                    || !AuthValidator.isValidEmail(email)
                    || !AuthValidator.isValidPassword(password)) {

                tvError.setText(R.string.register_invalid_data);
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            // Blokiraj dugmad dok Firebase obradjuje zahtev
            btnRegister.setEnabled(false);
            btnBack.setEnabled(false);

            authRepository.register(username, email, password,
                    new AuthRepository.RegisterCallback() {

                        @Override
                        public void onSuccess(boolean verificationEmailSent) {
                            handleRegisterSuccess(view, verificationEmailSent);
                        }

                        @Override
                        public void onError(String message) {
                            handleRegisterError(
                                    view, btnRegister, btnBack, tvError, message
                            );
                        }
                    });
        });
    }

    // ===== Privatne metode =====

    // Uspesna registracija
    private void handleRegisterSuccess(View view, boolean verificationEmailSent) {

        if (!isAdded() || getView() != view) {
            return;
        }

        if (verificationEmailSent) {
            Toast.makeText(requireContext(),
                    R.string.register_verification_sent,
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(requireContext(),
                    R.string.register_verification_failed,
                    Toast.LENGTH_LONG).show();
        }

        getParentFragmentManager().popBackStack();
    }

    // Neuspesna registracija
    private void handleRegisterError(View view,
                                     Button btnRegister,
                                     Button btnBack,
                                     TextView tvError,
                                     String message) {

        if (!isAdded() || getView() != view) {
            return;
        }

        btnRegister.setEnabled(true);
        btnBack.setEnabled(true);

        if (message != null
                && message.contains("Username is already taken")) {
            tvError.setText(R.string.register_username_taken);
        } else {
            tvError.setText(R.string.register_failed_generic);
        }

        tvError.setVisibility(View.VISIBLE);
    }
}
