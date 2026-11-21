package com.siit.recipeapp.ui.profile;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.siit.recipeapp.R;
import com.siit.recipeapp.data.local.RecipeDatabase;
import com.siit.recipeapp.data.model.account.UserRole;
import com.siit.recipeapp.data.remote.UserDataSession;
import com.siit.recipeapp.data.repository.AuthRepository;
import com.siit.recipeapp.data.repository.ProfileImageRepository;
import com.siit.recipeapp.data.repository.firebase.FirebaseAuthRepository;
import com.siit.recipeapp.RecipeApplication;
import com.siit.recipeapp.data.repository.firebase.FirebaseProfileImageRepository;
import com.siit.recipeapp.ui.auth.AdminRoleFragment;
import com.siit.recipeapp.ui.auth.ChangePasswordFragment;
import com.siit.recipeapp.ui.auth.LoginFragment;
import com.siit.recipeapp.ui.recipe.RecipeListFragment;
import com.siit.recipeapp.ui.report.ReportsFragment;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    // Veze
    private final AuthRepository authRepository = new FirebaseAuthRepository();
    private final ProfileImageRepository imageRepository = new FirebaseProfileImageRepository();

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ===== Povezivanje =====
        Button btnGrantAdmin = view.findViewById(R.id.btn_profile_grant_admin);
        Button btnLogout = view.findViewById(R.id.btn_profile_logout);
        Button btnEdit = view.findViewById(R.id.btn_profile_edit);
        Button btnChangePassword = view.findViewById(R.id.btn_profile_change_password);
        Button btnRecipes = view.findViewById(R.id.btn_profile_recipes);
        Button btnFavorites = view.findViewById(R.id.btn_profile_favorites);
        ImageView ivAvatar = view.findViewById(R.id.iv_profile_avatar);

        Button btnReports = view.findViewById(R.id.btn_profile_reports);
        Button btnManageReports = view.findViewById(R.id.btn_profile_manage_reports);
        btnReports.setOnClickListener(v -> openReports(false));
        btnManageReports.setOnClickListener(v -> openReports(true));

        // ===== Navigacija =====
        // Logout
        btnLogout.setOnClickListener(v -> {
            authRepository.logout();
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

        // Administratorske funkcionalnosti
        btnGrantAdmin.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new AdminRoleFragment())
                    .addToBackStack(null)
                    .commit();
        });



        // ===== Funkcionalnosti =====

        // Provera administratorske uloge
        authRepository.getCurrentRole(new AuthRepository.RoleCallback() {

            @Override
            public void onSuccess(UserRole role) {

                if (!isAdded() || getView() != view) {
                    return;
                }

                if (role == UserRole.ADMIN) {
                    btnGrantAdmin.setVisibility(View.VISIBLE);
                    btnManageReports.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String message) {
                // Dugme ostaje sakriveno ako provera ne uspe
            }
        });

        // Ucitavanje korisnickih podataka
        loadProfileData(view);

        // Ucitavanje profilne slike
        String userId = UserDataSession.currentOwnerId(requireContext());

        if (userId != null) {
            loadProfileImage(view, userId, ivAvatar);
        }


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




// ===== Privatne metode =====

    private void openReports(boolean admin) {
        getParentFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, ReportsFragment.newInstance(admin))
                .addToBackStack(null)
                .commit();
    }

    // Ucitavanje profilne slike
    private void loadProfileImage(View view, String userId, ImageView ivAvatar) {

        imageRepository.loadImageUrl(userId, new ProfileImageRepository.LoadCallback() {

            @Override
            public void onSuccess(String imageUrl) {

                if (!isAdded() || getView() != view) {
                    return;
                }

                if (imageUrl == null || imageUrl.isEmpty()) {
                    ivAvatar.setImageResource(R.drawable.ic_avatar_placeholder);
                    return;
                }

                Glide.with(ProfileFragment.this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_avatar_placeholder)
                        .error(R.drawable.ic_avatar_placeholder)
                        .centerCrop()
                        .into(ivAvatar);
            }

            @Override
            public void onError(String message) {
                // Ostaje podrazumevana slika
            }
        });
    }


    // Ucitavanje profila iz lokalne baze
    private void loadProfileData(View view) {

        String userId = UserDataSession.currentOwnerId(requireContext());

        if (userId == null) {
            return;
        }

        TextView tvUsername = view.findViewById(R.id.tv_profile_username);
        TextView tvRegion = view.findViewById(R.id.tv_profile_region);
        TextView tvBio = view.findViewById(R.id.tv_profile_bio);

        RecipeDatabase database = RecipeDatabase.getInstance(requireContext());

        database.userProfileDao()
                .observeProfile(userId)
                .observe(getViewLifecycleOwner(), profile -> {

                    if (profile == null) {
                        return;
                    }

                    tvUsername.setText(profile.username);

                    // Prikaz samo popunjenih polja lokacije
                    List<String> locationParts = new ArrayList<>();

                    if (profile.country != null && !profile.country.trim().isEmpty()) {
                        locationParts.add(CountryFlag.format(profile.country));
                    }

                    if (profile.region != null && !profile.region.trim().isEmpty()) {
                        locationParts.add(profile.region.trim());
                    }

                    if (profile.city != null && !profile.city.trim().isEmpty()) {
                        locationParts.add(profile.city.trim());
                    }

                    if (locationParts.isEmpty()) {
                        tvRegion.setText(R.string.profile_region_empty);
                    } else {
                        tvRegion.setText(TextUtils.join(", ", locationParts));
                    }

                    String biography = profile.biography;

                    if (biography == null || biography.isEmpty()) {
                        tvBio.setText(R.string.profile_bio_empty);
                    } else {
                        tvBio.setText(biography);
                    }
                });

        // Osvezi podatke iz Firestore-a kada je moguce
        ((RecipeApplication) requireActivity().getApplication())
                .syncSignedInUser();
    }

}