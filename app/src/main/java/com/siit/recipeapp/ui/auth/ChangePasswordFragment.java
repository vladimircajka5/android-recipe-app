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

public class ChangePasswordFragment extends Fragment {

    public ChangePasswordFragment() {
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
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnCancel = view.findViewById(R.id.btn_change_password_cancel);
        Button btnSubmit = view.findViewById(R.id.btn_change_password_submit);
        TextInputLayout currentLayout = view.findViewById(R.id.til_change_password_current);
        TextInputLayout newLayout = view.findViewById(R.id.til_change_password_new);
        TextInputLayout confirmLayout = view.findViewById(R.id.til_change_password_confirm);
        TextInputEditText current = view.findViewById(R.id.et_change_password_current);
        TextInputEditText next = view.findViewById(R.id.et_change_password_new);
        TextInputEditText confirm = view.findViewById(R.id.et_change_password_confirm);

        btnCancel.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });
        btnSubmit.setOnClickListener(v -> {
            String oldSecret = text(current);
            String newSecret = text(next);
            String confirmation = text(confirm);
            currentLayout.setError(null);
            newLayout.setError(null);
            confirmLayout.setError(null);
            if (!AuthValidator.isValidPassword(oldSecret)
                    || !AuthValidator.isValidPassword(newSecret)) {
                newLayout.setError("Passwords must contain at least 6 characters");
                return;
            }
            if (!AuthValidator.passwordsMatch(newSecret, confirmation)) {
                confirmLayout.setError("Passwords do not match");
                return;
            }
            btnSubmit.setEnabled(false);
            ((RecipeApplication) requireContext().getApplicationContext()).auth()
                    .changePassword(oldSecret, newSecret,
                            new com.siit.recipeapp.data.remote.AuthManager.Callback<>() {
                                @Override public void onSuccess(Void value) {
                                    if (isAdded()) {
                                        btnSubmit.setEnabled(true);
                                        getParentFragmentManager().popBackStack();
                                    }
                                }
                                @Override public void onError(@NonNull Exception error) {
                                    if (isAdded()) {
                                        btnSubmit.setEnabled(true);
                                        currentLayout.setError(error.getMessage());
                                    }
                                }
                            });
        });
    }

    private static String text(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }
}