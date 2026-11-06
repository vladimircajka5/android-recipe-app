
package com.siit.recipeapp.ui.recipe;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.siit.recipeapp.R;

public class RecipeListFragment extends Fragment {

    public static final String ARG_FAVORITES = "favorites";

    // Svojstva
    private boolean favorites = false;

    // Constructor
    public RecipeListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipe_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Vrati prethodno izabrani prikaz ili procitaj argument
        if (savedInstanceState != null) {
            favorites = savedInstanceState.getBoolean(ARG_FAVORITES);
        } else if (getArguments() != null) {
            favorites = getArguments().getBoolean(ARG_FAVORITES, false);
        }

        MaterialButton btnMyRecipes = view.findViewById(R.id.btn_recipe_list_my_recipes);
        MaterialButton btnFavorites = view.findViewById(R.id.btn_recipe_list_favorites);
        FloatingActionButton btnCreate = view.findViewById(R.id.btn_recipe_list_create);

        btnMyRecipes.setOnClickListener(v -> {
            favorites = false;
            updateMode(view);
        });

        btnFavorites.setOnClickListener(v -> {
            favorites = true;
            updateMode(view);
        });

        btnCreate.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new RecipeEditorFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Povezivanje liste sa adapterom
        RecyclerView rvRecipes = view.findViewById(R.id.rv_recipe_list);

        rvRecipes.setLayoutManager(new LinearLayoutManager(requireContext()));

        RecipeCardAdapter adapter = new RecipeCardAdapter();

        rvRecipes.setAdapter(adapter);

        updateMode(view);
    }

    // Azuriranje prikaza u zavisnosti od izabrane liste
    private void updateMode(View view) {

        MaterialButton btnMyRecipes = view.findViewById(R.id.btn_recipe_list_my_recipes);
        MaterialButton btnFavorites = view.findViewById(R.id.btn_recipe_list_favorites);
        FloatingActionButton btnCreate = view.findViewById(R.id.btn_recipe_list_create);
        TextView tvEmpty = view.findViewById(R.id.tv_recipe_list_empty);
        RecyclerView rvRecipes = view.findViewById(R.id.rv_recipe_list);

        // Preuzimanje boja iz trenutne teme
        int primary = MaterialColors.getColor(view,
                androidx.appcompat.R.attr.colorPrimary);

        int onPrimary = MaterialColors.getColor(view,
                com.google.android.material.R.attr.colorOnPrimary);

        int surface = MaterialColors.getColor(view,
                com.google.android.material.R.attr.colorSurface);

        // Pretvaranje 1dp u piksele
        int stroke = Math.round(getResources().getDisplayMetrics().density);

        if (favorites) {

            // My Recipes - sekundarni izgled
            btnMyRecipes.setBackgroundTintList(ColorStateList.valueOf(surface));
            btnMyRecipes.setTextColor(primary);
            btnMyRecipes.setStrokeColor(ColorStateList.valueOf(primary));
            btnMyRecipes.setStrokeWidth(stroke);

            // Favorites - primarni izgled
            btnFavorites.setBackgroundTintList(ColorStateList.valueOf(primary));
            btnFavorites.setTextColor(onPrimary);
            btnFavorites.setStrokeWidth(0);

            // Sakrij probni recept i dugme za kreiranje
            tvEmpty.setText(R.string.recipe_list_empty_favorites);
            tvEmpty.setVisibility(View.VISIBLE);
            rvRecipes.setVisibility(View.GONE);
            btnCreate.setVisibility(View.GONE);

        } else {

            // My Recipes - primarni izgled
            btnMyRecipes.setBackgroundTintList(ColorStateList.valueOf(primary));
            btnMyRecipes.setTextColor(onPrimary);
            btnMyRecipes.setStrokeWidth(0);

            // Favorites - sekundarni izgled
            btnFavorites.setBackgroundTintList(ColorStateList.valueOf(surface));
            btnFavorites.setTextColor(primary);
            btnFavorites.setStrokeColor(ColorStateList.valueOf(primary));
            btnFavorites.setStrokeWidth(stroke);

            // Prikazi probni recept i dugme za kreiranje
            tvEmpty.setVisibility(View.GONE);
            rvRecipes.setVisibility(View.VISIBLE);
            btnCreate.setVisibility(View.VISIBLE);
        }
    }

    // Cuvanje izabranog prikaza
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ARG_FAVORITES, favorites);
    }
}
