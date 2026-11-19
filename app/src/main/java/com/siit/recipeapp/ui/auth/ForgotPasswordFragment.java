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
import android.widget.Toast;

import com.siit.recipeapp.data.repository.AuthRepository;
import com.siit.recipeapp.data.repository.firebase.FirebaseAuthRepository;
import com.siit.recipeapp.domain.validation.AuthValidator;

public class ForgotPasswordFragment extends Fragment {

    private final AuthRepository authRepository = new FirebaseAuthRepository();

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
            String address = email.getText() == null
                    ? ""
                    : email.getText().toString().trim();

            emailLayout.setError(null);

            if (!AuthValidator.isValidEmail(address)) {
                emailLayout.setError("Enter a valid email address.");
                return;
            }

            btnSubmit.setEnabled(false);

            authRepository.sendPasswordReset(
                    address,
                    new AuthRepository.OperationCallback() {
                        @Override
                        public void onSuccess() {
                            if (!isAdded() || getView() != view) {
                                return;
                            }

                            btnSubmit.setEnabled(true);

                            Toast.makeText(
                                    requireContext(),
                                    R.string.forgot_password_reset_requested,
                                    Toast.LENGTH_LONG
                            ).show();
                        }

                        @Override
                        public void onError(String message) {
                            if (!isAdded() || getView() != view) {
                                return;
                            }

                            btnSubmit.setEnabled(true);
                            emailLayout.setError(message);
                        }
                    });
        });
    }
}