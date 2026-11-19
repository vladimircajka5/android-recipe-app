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

public class ForgotPasswordFragment extends Fragment {

    // Veze
    private final AuthRepository authRepository = new FirebaseAuthRepository();

    // Constructor
    public ForgotPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ===== Povezivanje =====

        TextInputEditText etEmail = view.findViewById(R.id.et_forgot_password_email);
        TextView tvError = view.findViewById(R.id.tv_forgot_password_error);

        Button btnSubmit = view.findViewById(R.id.btn_forgot_password_submit);
        Button btnCancel = view.findViewById(R.id.btn_forgot_password_cancel);

        // ===== Navigacija =====

        // Povratak na Login
        btnCancel.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        // ===== Funkcionalnosti =====

        // Slanje emaila za resetovanje lozinke
        btnSubmit.setOnClickListener(v -> {

            // Sakrij prethodnu gresku
            tvError.setVisibility(View.GONE);

            String email = etEmail.getText().toString().trim();

            // Provera email adrese
            if (!AuthValidator.isValidEmail(email)) {
                tvError.setText(R.string.forgot_password_invalid_email);
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            // Blokiraj dugmad dok Firebase obradjuje zahtev
            btnSubmit.setEnabled(false);
            btnCancel.setEnabled(false);

            authRepository.sendPasswordResetEmail(email,
                    new AuthRepository.PasswordCallback() {

                        @Override
                        public void onSuccess() {
                            handleResetPasswordSuccess(view);
                        }

                        @Override
                        public void onError(String message) {
                            handleResetPasswordError(
                                    view, btnSubmit, btnCancel, tvError
                            );
                        }
                    });
        });
    }

    // ===== Privatne metode =====

    // Uspesno slanje emaila
    private void handleResetPasswordSuccess(View view) {

        if (!isAdded() || getView() != view) {
            return;
        }

        Toast.makeText(requireContext(),
                R.string.forgot_password_sent,
                Toast.LENGTH_LONG).show();

        getParentFragmentManager().popBackStack();
    }

    // Neuspesno slanje emaila
    private void handleResetPasswordError(View view,
                                          Button btnSubmit,
                                          Button btnCancel,
                                          TextView tvError) {

        if (!isAdded() || getView() != view) {
            return;
        }

        btnSubmit.setEnabled(true);
        btnCancel.setEnabled(true);

        tvError.setText(R.string.forgot_password_failed);
        tvError.setVisibility(View.VISIBLE);
    }
}
