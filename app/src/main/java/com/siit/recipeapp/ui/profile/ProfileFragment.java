package com.siit.recipeapp.ui.profile;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.siit.recipeapp.R;
import com.siit.recipeapp.ui.auth.ChangePasswordFragment;
import com.siit.recipeapp.ui.auth.LoginFragment;
import com.siit.recipeapp.ui.recipe.RecipeListFragment;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnLogout = view.findViewById(R.id.btn_profile_logout);
        Button btnEdit = view.findViewById(R.id.btn_profile_edit);
        Button btnChangePassword = view.findViewById(R.id.btn_profile_change_password);
        Button btnRecipes = view.findViewById(R.id.btn_profile_recipes);
        Button btnFavorites = view.findViewById(R.id.btn_profile_favorites);

        // Logout
        btnLogout.setOnClickListener(v -> {
            FragmentManager fm = getParentFragmentManager();

            fm.popBackStack(
                    null,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
            );

            fm.beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragment_container, new LoginFragment())
                    .commit();
        });

        // Edit Profile
        btnEdit.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new EditProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Change Password
        btnChangePassword.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ChangePasswordFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // My Recipes
        btnRecipes.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new RecipeListFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Favorites
        btnFavorites.setOnClickListener(v -> {
            RecipeListFragment fragment = new RecipeListFragment();

            Bundle args = new Bundle();
            args.putBoolean(RecipeListFragment.ARG_FAVORITES, true);
            fragment.setArguments(args);

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });



        // Promena teme
        MaterialSwitch switchTheme = view.findViewById(R.id.switch_profile_theme);

        int themeMode = requireActivity()
                .getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                .getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_YES);

        // Prikazi trenutno izabranu temu
        switchTheme.setChecked(themeMode == AppCompatDelegate.MODE_NIGHT_NO);

        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {

            int mode;

            if (isChecked) {
                mode = AppCompatDelegate.MODE_NIGHT_NO;
            } else {
                mode = AppCompatDelegate.MODE_NIGHT_YES;
            }

            // Sacuvaj izbor teme
            requireActivity()
                    .getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                    .edit()
                    .putInt("theme_mode", mode)
                    .apply();

            // Primeni novu temu
            AppCompatDelegate.setDefaultNightMode(mode);
        });



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
}