package com.siit.recipeapp.ui.shopping;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.siit.recipeapp.R;

import java.util.ArrayList;

public final class RecipeIngredientsToListFragment extends Fragment {
    private static final String ARG_RECIPE_NAME = "recipe_name";
    private static final String ARG_SERVINGS = "servings";
    private static final String ARG_INGREDIENT_ROWS = "ingredient_rows";
    private static final String STATE_MODE = "ingredient_mode";
    private static final String STATE_CHECKED = "checked_ingredients";
    private static final String STATE_DESTINATION = "destination_name";

    private ArrayList<String> ingredientRows;
    private boolean[] checkedIngredients;
    private int ingredientMode = R.id.recipe_shopping_selected;
    private String destinationName;

    public RecipeIngredientsToListFragment() {
        super(R.layout.fragment_recipe_ingredients_to_list);
    }

    public static RecipeIngredientsToListFragment newInstance(String recipeName, int servings,
                                                              ArrayList<String> ingredientRows) {
        RecipeIngredientsToListFragment fragment = new RecipeIngredientsToListFragment();
        Bundle arguments = new Bundle();
        arguments.putString(ARG_RECIPE_NAME, recipeName);
        arguments.putInt(ARG_SERVINGS, servings);
        arguments.putStringArrayList(ARG_INGREDIENT_ROWS, new ArrayList<>(ingredientRows));
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        ingredientRows = requireArguments().getStringArrayList(ARG_INGREDIENT_ROWS);
        if (state != null) {
            checkedIngredients = state.getBooleanArray(STATE_CHECKED);
            ingredientMode = state.getInt(STATE_MODE, R.id.recipe_shopping_selected);
            destinationName = state.getString(STATE_DESTINATION);
        }
        if (checkedIngredients == null) {
            checkedIngredients = new boolean[ingredientRows.size()];
        }
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle state) {
        MaterialToolbar toolbar = root.findViewById(R.id.recipe_shopping_toolbar);
        toolbar.setNavigationOnClickListener(view -> getParentFragmentManager().popBackStack());

        showRecipe(root);
        setupIngredientSelection(root);
        setupDestination(root);
    }

    private void showRecipe(View root) {
        Bundle arguments = requireArguments();
        TextView title = root.findViewById(R.id.recipe_shopping_name);
        title.setText(arguments.getString(ARG_RECIPE_NAME));
        TextView servings = root.findViewById(R.id.recipe_shopping_servings);
        int count = arguments.getInt(ARG_SERVINGS);
        servings.setText(getResources().getQuantityString(R.plurals.search_detail_servings, count, count));
    }

    private void setupIngredientSelection(View root) {
        LinearLayout choices = root.findViewById(R.id.recipe_shopping_ingredient_choices);
        for (int index = 0; index < ingredientRows.size(); index++) {
            int position = index;
            MaterialCheckBox choice = new MaterialCheckBox(requireContext());
            choice.setText(ingredientRows.get(position));
            choice.setChecked(checkedIngredients[position]);
            choice.setOnCheckedChangeListener((button, checked) -> checkedIngredients[position] = checked);
            choices.addView(choice);
        }

        TextView allRows = root.findViewById(R.id.recipe_shopping_all_rows);
        allRows.setText(String.join("\n", ingredientRows));

        RadioGroup modes = root.findViewById(R.id.recipe_shopping_modes);
        modes.check(ingredientMode);
        modes.setOnCheckedChangeListener((group, checkedId) -> {
            ingredientMode = checkedId;
            showIngredientMode(root);
        });
        showIngredientMode(root);
    }

    private void showIngredientMode(View root) {
        root.findViewById(R.id.recipe_shopping_ingredient_choices).setVisibility(
                ingredientMode == R.id.recipe_shopping_selected ? View.VISIBLE : View.GONE);
        root.findViewById(R.id.recipe_shopping_all_rows).setVisibility(
                ingredientMode == R.id.recipe_shopping_all ? View.VISIBLE : View.GONE);
        root.findViewById(R.id.recipe_shopping_missing_message).setVisibility(
                ingredientMode == R.id.recipe_shopping_missing ? View.VISIBLE : View.GONE);
    }

    private void setupDestination(View root) {
        root.findViewById(R.id.recipe_shopping_choose_list).setOnClickListener(view ->
                getParentFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.fragment_container, ShoppingListsFragment.newPicker())
                        .addToBackStack(null)
                        .commit());
        getParentFragmentManager().setFragmentResultListener(ShoppingListsFragment.PICK_RESULT,
                getViewLifecycleOwner(), (key, result) -> {
                    destinationName = result.getString(ShoppingListsFragment.PICKED_NAME);
                    showDestination(root);
                });
        showDestination(root);
    }

    private void showDestination(View root) {
        TextView destination = root.findViewById(R.id.recipe_shopping_destination);
        MaterialButton button = root.findViewById(R.id.recipe_shopping_choose_list);
        destination.setText(destinationName == null ? getString(R.string.recipe_shopping_no_list)
                : destinationName);
        button.setText(destinationName == null ? R.string.recipe_shopping_choose_list
                : R.string.recipe_shopping_change_list);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle state) {
        state.putBooleanArray(STATE_CHECKED, checkedIngredients);
        state.putInt(STATE_MODE, ingredientMode);
        state.putString(STATE_DESTINATION, destinationName);
        super.onSaveInstanceState(state);
    }
}
