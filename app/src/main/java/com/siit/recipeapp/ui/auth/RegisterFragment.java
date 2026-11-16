package com.siit.recipeapp.ui.auth;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.siit.recipeapp.R;
import com.siit.recipeapp.RecipeApplication;
import com.siit.recipeapp.domain.validation.AuthValidator;
import com.siit.recipeapp.ui.home.HomeFragment;

public class RegisterFragment extends Fragment {

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnBack = view.findViewById(R.id.btn_register_back);
        Button btnRegister = view.findViewById(R.id.btn_register_sumbit);
        TextInputLayout usernameLayout = view.findViewById(R.id.til_register_username);
        TextInputLayout emailLayout = view.findViewById(R.id.til_register_email);
        TextInputLayout passwordLayout = view.findViewById(R.id.til_register_password);
        TextInputEditText username = view.findViewById(R.id.et_register_username);
        TextInputEditText email = view.findViewById(R.id.et_register_email);
        TextInputEditText password = view.findViewById(R.id.et_register_password);

        btnBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });



        btnRegister.setOnClickListener(v -> {
            String name = text(username);
            String address = text(email);
            String secret = text(password);
            usernameLayout.setError(null);
            emailLayout.setError(null);
            passwordLayout.setError(null);
            if (!AuthValidator.isValidUsername(name)) {
                usernameLayout.setError("Use 3-30 letters, numbers, '.', '_' or '-'");
                return;
            }
            if (!AuthValidator.isValidEmail(address)) {
                emailLayout.setError("Enter a valid email address");
                return;
            }
            if (!AuthValidator.isValidPassword(secret)) {
                passwordLayout.setError("Password must contain at least 8 characters");
                return;
            }
            btnRegister.setEnabled(false);
            ((RecipeApplication) requireContext().getApplicationContext()).auth()
                    .register(name, address, secret, new com.siit.recipeapp.data.remote.AuthManager.Callback<>() {
                        @Override public void onSuccess(com.google.firebase.auth.FirebaseUser user) {
                            if (!isAdded()) return;
                            btnRegister.setEnabled(true);
                            passwordLayout.setError("Verification email sent. Verify it before signing in.");
                        }
                        @Override public void onError(@NonNull Exception error) {
                            if (isAdded()) {
                                btnRegister.setEnabled(true);
                                usernameLayout.setError(error.getMessage());
                            }
                        }
                    });
        });
    }

    private static String text(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }
}