package com.siit.recipeapp.ui.auth;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.siit.recipeapp.R;
import com.siit.recipeapp.RecipeApplication;
import com.siit.recipeapp.domain.validation.AuthValidator;

public class ForgotPasswordFragment extends Fragment {

    public ForgotPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnCancel = view.findViewById(R.id.btn_forgot_password_cancel);
        Button btnSubmit = view.findViewById(R.id.btn_forgot_password_submit);
        TextInputLayout emailLayout = view.findViewById(R.id.til_forgot_password_email);
        TextInputEditText email = view.findViewById(R.id.et_forgot_password_email);

        btnCancel.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });
        btnSubmit.setOnClickListener(v -> {
            String address = email.getText() == null ? "" : email.getText().toString().trim();
            emailLayout.setError(null);
            if (!AuthValidator.isValidEmail(address)) {
                emailLayout.setError("Enter a valid email address");
                return;
            }
            btnSubmit.setEnabled(false);
            ((RecipeApplication) requireContext().getApplicationContext()).auth()
                    .sendPasswordReset(address, new com.siit.recipeapp.data.remote.AuthManager.Callback<>() {
                        @Override public void onSuccess(Void value) {
                            if (isAdded()) {
                                btnSubmit.setEnabled(true);
                                emailLayout.setError("Password reset email sent");
                            }
                        }
                        @Override public void onError(@NonNull Exception error) {
                            if (isAdded()) {
                                btnSubmit.setEnabled(true);
                                emailLayout.setError(error.getMessage());
                            }
                        }
                    });
        });
    }
}