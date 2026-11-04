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

import com.siit.recipeapp.R;
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

        btnBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });



        btnRegister.setOnClickListener(v -> {
            FragmentManager fm = getParentFragmentManager();

            fm.popBackStack(
                    null,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
            );

            fm.beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        });

    }
}