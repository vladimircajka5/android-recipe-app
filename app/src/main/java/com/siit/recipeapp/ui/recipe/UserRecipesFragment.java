package com.siit.recipeapp.ui.recipe;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.siit.recipeapp.R;

public class UserRecipesFragment extends Fragment {

    public UserRecipesFragment() {
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
        return inflater.inflate(R.layout.fragment_user_recipes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rvRecipes = view.findViewById(R.id.rv_user_recipes);
        TextView tvEmpty = view.findViewById(R.id.tv_user_recipes_empty);
        TextView tvCount = view.findViewById(R.id.tv_user_recipes_count);

        // Povezivanje liste sa adapterom
        rvRecipes.setLayoutManager(new LinearLayoutManager(requireContext()));

        RecipeCardAdapter adapter = new RecipeCardAdapter();
        rvRecipes.setAdapter(adapter);

        // Privremeni broj recepata iz adaptera
        tvCount.setText(getString(R.string.user_recipes_count, adapter.getItemCount()));

        if (adapter.getItemCount() == 0) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvRecipes.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvRecipes.setVisibility(View.VISIBLE);
        }
    }
}