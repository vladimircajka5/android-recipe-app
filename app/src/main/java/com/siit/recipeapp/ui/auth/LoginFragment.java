package com.siit.recipeapp.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.siit.recipeapp.R;
import com.siit.recipeapp.ui.home.HomeFragment;

public class LoginFragment extends Fragment {

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

        Button btnRegister = view.findViewById(R.id.btn_login_register);
        Button btnLogin = view.findViewById(R.id.btn_login_lsumbit);
        Button btnGuest = view.findViewById(R.id.btn_login_guest);
        TextView tvForgotPassword = view.findViewById(R.id.tv_login_forgot_password);

        // Register
        btnRegister.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new RegisterFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Login i Guest za sada rade isto
        View.OnClickListener openHome = v -> {
            FragmentManager manager = getParentFragmentManager();

            manager.popBackStackImmediate(
                    null,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
            );

            manager.beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        };

        btnLogin.setOnClickListener(openHome);
        btnGuest.setOnClickListener(openHome);

        // Forgot Password
        tvForgotPassword.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ForgotPasswordFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }
}
