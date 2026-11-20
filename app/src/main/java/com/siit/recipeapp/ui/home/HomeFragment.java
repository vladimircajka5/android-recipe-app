
package com.siit.recipeapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.siit.recipeapp.MainActivity;
import com.siit.recipeapp.R;
import com.siit.recipeapp.ui.recipe.UserRecipesFragment;


public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hejj Tara - Sklanjaj sve iz ovog slobodno, ja ga koristio za testirannje :)
        // Hejj Tara - Sklanjaj sve iz ovog slobodno, ja ga koristio za testirannje :)
        // Hejj Tara - Sklanjaj sve iz ovog slobodno, ja ga koristio za testirannje :)
        // Hejj Tara - Sklanjaj sve iz ovog slobodno, ja ga koristio za testirannje :)

        Button btnProfile = view.findViewById(R.id.btn_home_profile);

        // Otvaranje profila drugog korisnika
        btnProfile.setOnClickListener(v -> {

            // Gost ne moze da cita korisnicke profile
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(requireContext(),
                        "Please log in to view user profiles.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            EditText etUsername = new EditText(requireContext());
            etUsername.setSingleLine(true);
            etUsername.setHint(R.string.other_profile_username_hint);

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.other_profile_open_title)
                    .setView(etUsername)
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton(R.string.other_profile_open, (dialog, which) -> {

                        String username = etUsername.getText().toString().trim();

                        if (username.isEmpty()) {
                            return;
                        }

                        UserRecipesFragment fragment = new UserRecipesFragment();

                        Bundle args = new Bundle();
                        args.putString(UserRecipesFragment.ARG_USERNAME, username);
                        fragment.setArguments(args);

                        getParentFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit();
                    })
                    .show();
        });
    }
}
