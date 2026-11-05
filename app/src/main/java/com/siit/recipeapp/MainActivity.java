
package com.siit.recipeapp;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.siit.recipeapp.ui.auth.LoginFragment;
import com.siit.recipeapp.ui.auth.RegisterFragment;
import com.siit.recipeapp.ui.auth.ForgotPasswordFragment;
import com.siit.recipeapp.ui.home.HomeFragment;
import com.siit.recipeapp.ui.profile.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        int themeMode = getSharedPreferences("app_settings", MODE_PRIVATE)
                .getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_YES);

        AppCompatDelegate.setDefaultNightMode(themeMode);

        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        getWindow().setNavigationBarContrastEnforced(false);

        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottom_navigation);

        // Prilagodjavanje prostora sistemskim trakama
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            int bottomPadding = bottomNavigation.getVisibility() == View.VISIBLE
                    ? 0 : systemBars.bottom;

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding);
            return insets;
        });

        // Uklanja obojenu pozadinu iza aktivne ikonice
        bottomNavigation.setItemActiveIndicatorEnabled(false);

        // Promena tabova
        bottomNavigation.setOnItemSelectedListener(item -> {

            Fragment fragment;

            if (item.getItemId() == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (item.getItemId() == R.id.nav_profile) {
                fragment = new ProfileFragment();
            } else {
                return false;
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();

            return true;
        });

        // Prikazivanje ili sakrivanje navigacije
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(
                new FragmentManager.FragmentLifecycleCallbacks() {

                    @Override
                    public void onFragmentResumed(
                            @NonNull FragmentManager fm,
                            @NonNull Fragment fragment) {

                        if (fragment.getId() != R.id.fragment_container) {
                            return;
                        }

                        boolean isAuthScreen =
                                fragment instanceof LoginFragment
                                        || fragment instanceof RegisterFragment
                                        || fragment instanceof ForgotPasswordFragment;

                        bottomNavigation.setVisibility(
                                isAuthScreen ? View.GONE : View.VISIBLE
                        );

                        // Ponovo izracunaj razmake kada se meni pojavi ili nestane
                        ViewCompat.requestApplyInsets(findViewById(R.id.main));

                        if (fragment instanceof HomeFragment) {
                            bottomNavigation.getMenu()
                                    .findItem(R.id.nav_home)
                                    .setChecked(true);
                        } else if (fragment instanceof ProfileFragment) {
                            bottomNavigation.getMenu()
                                    .findItem(R.id.nav_profile)
                                    .setChecked(true);
                        }
                    }
                },
                false
        );

        // Pocetni Login ekran
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .commit();
        }
    }
}
