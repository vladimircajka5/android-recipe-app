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

public class AdminRoleFragment extends Fragment {

    // Veze
    private final AuthRepository authRepository = new FirebaseAuthRepository();

    // Constructor
    public AdminRoleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_role, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ===== Povezivanje =====

        TextInputEditText etUsername = view.findViewById(R.id.et_admin_username);
        TextView tvError = view.findViewById(R.id.tv_admin_error);

        Button btnGrant = view.findViewById(R.id.btn_admin_grant);
        Button btnBack = view.findViewById(R.id.btn_admin_back);

        // ===== Navigacija =====

        // Povratak na Profile
        btnBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        // ===== Funkcionalnosti =====

        // Dodela administratorske uloge
        btnGrant.setOnClickListener(v -> {

            tvError.setVisibility(View.GONE);

            String username = etUsername.getText().toString().trim();

            if (!AuthValidator.isValidUsername(username)) {
                tvError.setText(R.string.admin_grant_invalid);
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            btnGrant.setEnabled(false);
            btnBack.setEnabled(false);

            authRepository.grantAdmin(username, new AuthRepository.AdminCallback() {

                @Override
                public void onSuccess() {
                    handleGrantSuccess(view);
                }

                @Override
                public void onError(String message) {
                    handleGrantError(view, btnGrant, btnBack, tvError, message);
                }
            });
        });
    }

    // ===== Privatne metode =====

    // Uspesna dodela uloge
    private void handleGrantSuccess(View view) {

        if (!isAdded() || getView() != view) {
            return;
        }

        Toast.makeText(requireContext(),
                R.string.admin_grant_success,
                Toast.LENGTH_SHORT).show();

        getParentFragmentManager().popBackStack();
    }

    // Neuspesna dodela uloge
    private void handleGrantError(View view, Button btnGrant,
                                  Button btnBack, TextView tvError,
                                  String message) {

        if (!isAdded() || getView() != view) {
            return;
        }

        btnGrant.setEnabled(true);
        btnBack.setEnabled(true);

        if ("USER_NOT_FOUND".equals(message)) {
            tvError.setText(R.string.admin_grant_not_found);
        } else if ("ALREADY_ADMIN".equals(message)) {
            tvError.setText(R.string.admin_grant_already_admin);
        } else if ("SELF_ASSIGNMENT".equals(message)) {
            tvError.setText(R.string.admin_grant_self);
        } else if ("INVALID_USERNAME".equals(message)) {
            tvError.setText(R.string.admin_grant_invalid);
        } else {
            tvError.setText(R.string.admin_grant_failed);
        }

        tvError.setVisibility(View.VISIBLE);
    }
}
