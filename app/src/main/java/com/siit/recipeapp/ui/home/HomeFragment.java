package com.siit.recipeapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.siit.recipeapp.MainActivity;
import com.siit.recipeapp.R;
import com.siit.recipeapp.data.model.account.UserRole;
import com.siit.recipeapp.data.repository.AuthRepository;
import com.siit.recipeapp.data.repository.firebase.FirebaseAuthRepository;
import com.siit.recipeapp.ui.admin.AdminCatalogFragment;
import com.siit.recipeapp.ui.notifications.NotificationsFragment;
import com.siit.recipeapp.ui.proposals.CategoryProposalsFragment;
import com.siit.recipeapp.ui.search.RecipeSearchFragment;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_home_search_recipes).setOnClickListener(v ->
                getParentFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.fragment_container, new RecipeSearchFragment())
                        .addToBackStack(null)
                        .commit());

        Button catalogButton = view.findViewById(R.id.btn_home_catalog);
        AuthRepository catalogAuth = new FirebaseAuthRepository();
        view.findViewById(R.id.btn_home_notifications)
                .setOnClickListener(v ->
                        ((MainActivity) requireActivity())
                                .openProtectedFragment(
                                        new NotificationsFragment()));

        view.findViewById(R.id.btn_home_proposals)
                .setOnClickListener(v -> {
                    if (!catalogAuth.hasVerifiedSession()) {
                        Toast.makeText(
                                requireContext(),
                                R.string.proposal_sign_in,
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(
                                    R.id.fragment_container,
                                    new CategoryProposalsFragment())
                            .addToBackStack(null)
                            .commit();
                });
        catalogButton.setOnClickListener(v ->
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(
                                R.id.fragment_container,
                                new AdminCatalogFragment())
                        .addToBackStack(null)
                        .commit()
        );

        if (catalogAuth.hasVerifiedSession()) {
            catalogAuth.getCurrentRole(new AuthRepository.RoleCallback() {
                @Override
                public void onSuccess(UserRole role) {
                    if (!isAdded() || getView() != view) {
                        return;
                    }

                    catalogButton.setVisibility(role == UserRole.ADMIN ? View.VISIBLE : View.GONE);
                }

                @Override
                public void onError(String message) {
                    if (!isAdded() || getView() != view) {
                        return;
                    }

                    catalogButton.setVisibility(View.GONE);
                }
            });
        }
    }
}