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

public class ChangePasswordFragment extends Fragment {

    // Veze
    private final AuthRepository authRepository = new FirebaseAuthRepository();

    // Constructor
    public ChangePasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ===== Povezivanje =====

        TextInputEditText etCurrent = view.findViewById(R.id.et_change_password_current);
        TextInputEditText etNew = view.findViewById(R.id.et_change_password_new);
        TextInputEditText etConfirm = view.findViewById(R.id.et_change_password_confirm);

        TextView tvError = view.findViewById(R.id.tv_change_password_error);

        Button btnCancel = view.findViewById(R.id.btn_change_password_cancel);
        Button btnSubmit = view.findViewById(R.id.btn_change_password_submit);

        // ===== Navigacija =====

        // Povratak na Profile
        btnCancel.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        // ===== Funkcionalnosti =====

        // Promena lozinke
        btnSubmit.setOnClickListener(v -> {

            // Sakrij prethodnu gresku
            tvError.setVisibility(View.GONE);

            String currentPassword = etCurrent.getText().toString();
            String newPassword = etNew.getText().toString();
            String confirmPassword = etConfirm.getText().toString();

            // Provera trenutne lozinke
            if (currentPassword.isEmpty()) {
                tvError.setText(R.string.change_password_current_required);
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            // Provera nove lozinke
            if (!AuthValidator.isValidPassword(newPassword)) {
                tvError.setText(R.string.change_password_invalid);
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            // Provera potvrde lozinke
            if (!AuthValidator.passwordsMatch(newPassword, confirmPassword)) {
                tvError.setText(R.string.change_password_mismatch);
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            // Blokiraj dugmad dok Firebase obradjuje zahtev
            btnSubmit.setEnabled(false);
            btnCancel.setEnabled(false);

            authRepository.changePassword(currentPassword, newPassword,
                    new AuthRepository.PasswordCallback() {

                        @Override
                        public void onSuccess() {
                            handleChangePasswordSuccess(view);
                        }

                        @Override
                        public void onError(String message) {
                            handleChangePasswordError(
                                    view, btnSubmit, btnCancel, tvError, message
                            );
                        }
                    });
        });
    }

    // ===== Privatne metode =====

    // Uspesna promena lozinke
    private void handleChangePasswordSuccess(View view) {

        if (!isAdded() || getView() != view) {
            return;
        }

        Toast.makeText(requireContext(),
                R.string.change_password_success,
                Toast.LENGTH_SHORT).show();

        getParentFragmentManager().popBackStack();
    }

    // Neuspesna promena lozinke
    private void handleChangePasswordError(View view,
                                           Button btnSubmit,
                                           Button btnCancel,
                                           TextView tvError,
                                           String message) {

        if (!isAdded() || getView() != view) {
            return;
        }

        btnSubmit.setEnabled(true);
        btnCancel.setEnabled(true);

        if ("WRONG_PASSWORD".equals(message)) {
            tvError.setText(R.string.change_password_wrong_current);
        } else if ("NO_USER".equals(message)) {
            tvError.setText(R.string.change_password_no_user);
        } else {
            tvError.setText(R.string.change_password_failed);
        }

        tvError.setVisibility(View.VISIBLE);
    }
}
