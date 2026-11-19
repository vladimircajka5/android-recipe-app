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
import androidx.fragment.app.FragmentManager;

import com.google.android.material.textfield.TextInputEditText;
import com.siit.recipeapp.R;
import com.siit.recipeapp.data.repository.AuthRepository;
import com.siit.recipeapp.data.repository.firebase.FirebaseAuthRepository;
import com.siit.recipeapp.ui.home.HomeFragment;

public class LoginFragment extends Fragment {

    // Veze
    private final AuthRepository authRepository = new FirebaseAuthRepository();

    // Constructor
    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ===== Povezivanje =====

        TextInputEditText etIdentifier = view.findViewById(R.id.et_login_identifier);
        TextInputEditText etPassword = view.findViewById(R.id.et_login_password);

        TextView tvError = view.findViewById(R.id.tv_login_error);
        TextView tvForgotPassword = view.findViewById(R.id.tv_login_forgot_password);

        Button btnRegister = view.findViewById(R.id.btn_login_register);
        Button btnLogin = view.findViewById(R.id.btn_login_lsumbit);
        Button btnGuest = view.findViewById(R.id.btn_login_guest);

        etIdentifier.setText("petarkukic12321@gmail.com");
        etPassword.setText("123456");

        // ===== Navigacija =====

        // Otvaranje Register ekrana
        btnRegister.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new RegisterFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Otvaranje Forgot Password ekrana
        tvForgotPassword.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ForgotPasswordFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Ulazak kao Guest
        btnGuest.setOnClickListener(v -> {
            navigateToHome();
        });

        // ===== Funkcionalnosti =====

        // Prijava korisnika
        btnLogin.setOnClickListener(v -> {

            // Sakrij prethodnu gresku
            tvError.setVisibility(View.GONE);

            String identifier = etIdentifier.getText().toString().trim();
            String password = etPassword.getText().toString();

            // Provera unetih podataka
            if (identifier.isEmpty() || password.isEmpty()) {
                tvError.setText(R.string.login_invalid_data);
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            btnLogin.setEnabled(false);

            authRepository.login(identifier, password,
                    new AuthRepository.LoginCallback() {

                        @Override
                        public void onSuccess() {
                            handleLoginSuccess(view);
                        }

                        @Override
                        public void onError(String message) {
                            handleLoginError(view, btnLogin, tvError, message);
                        }
                    });
        });
    }

    // ===== Privatne metode =====

    // Otvaranje Home ekrana
    private void navigateToHome() {

        FragmentManager manager = getParentFragmentManager();

        // Brisanje prethodnih fragmenata iz Back Stack-a
        manager.popBackStackImmediate(
                null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
        );

        manager.beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
    }

    // Uspesna prijava
    private void handleLoginSuccess(View view) {

        if (!isAdded() || getView() != view) {
            return;
        }

        Toast.makeText(requireContext(),
                R.string.login_success,
                Toast.LENGTH_SHORT).show();

        navigateToHome();
    }

    // Neuspesna prijava
    private void handleLoginError(View view,
                                  Button btnLogin,
                                  TextView tvError,
                                  String message) {

        if (!isAdded() || getView() != view) {
            return;
        }

        btnLogin.setEnabled(true);

        if ("EMAIL_NOT_VERIFIED".equals(message)) {
            tvError.setText(R.string.login_email_not_verified);
        } else {
            tvError.setText(R.string.login_failed);
        }

        tvError.setVisibility(View.VISIBLE);
    }
}
