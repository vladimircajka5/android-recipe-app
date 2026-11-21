package com.siit.recipeapp.ui.recipe;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.siit.recipeapp.MainActivity;
import com.siit.recipeapp.R;
import com.siit.recipeapp.ui.preview.RecipeSearchPreviewData;
import com.siit.recipeapp.ui.preview.RecipeSearchPreviewData.Amount;
import com.siit.recipeapp.ui.preview.RecipeSearchPreviewData.Recipe;
import com.siit.recipeapp.ui.report.ReportFormFragment;
import com.siit.recipeapp.ui.shopping.RecipeIngredientsToListFragment;

import java.util.ArrayList;
import java.util.List;

public class RecipeDetailFragment extends Fragment {
    private static final String ARG_PREVIEW_ID = "preview_recipe_id";

    public RecipeDetailFragment() {
        super(R.layout.fragment_recipe_detail);
    }

    public static RecipeDetailFragment newPreview(String recipeId) {
        RecipeDetailFragment fragment = new RecipeDetailFragment();
        Bundle arguments = new Bundle();
        arguments.putString(ARG_PREVIEW_ID, recipeId);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle state) {
        root.findViewById(R.id.recipe_rate).setOnClickListener(view ->
                RecipeFeedbackDialogFragment.showRating(getChildFragmentManager()));
        root.findViewById(R.id.recipe_add_comment).setOnClickListener(view ->
                RecipeFeedbackDialogFragment.showComment(getChildFragmentManager()));

        Bundle arguments = getArguments();
        if (arguments == null) return;
        Recipe recipe = RecipeSearchPreviewData.findRecipe(arguments.getString(ARG_PREVIEW_ID));
        if (recipe == null) return;

        MaterialToolbar toolbar = root.findViewById(R.id.recipe_detail_preview_toolbar);
        toolbar.setVisibility(View.VISIBLE);
        toolbar.setNavigationOnClickListener(view -> getParentFragmentManager().popBackStack());
        setupRecipeMenu(toolbar, recipe);
        root.findViewById(R.id.tv_recipe_detail_title).setVisibility(View.GONE);
        root.findViewById(R.id.tv_recipe_detail_region).setVisibility(View.GONE);
        root.findViewById(R.id.recipe_detail_actions).setVisibility(View.GONE);
        root.findViewById(R.id.btn_recipe_detail_decrease).setVisibility(View.GONE);
        root.findViewById(R.id.btn_recipe_detail_increase).setVisibility(View.GONE);

        showPhoto(root, recipe);
        text(root, R.id.tv_recipe_detail_name).setText(recipe.title);
        text(root, R.id.tv_recipe_detail_author).setText(R.string.search_sample_author);
        text(root, R.id.tv_recipe_detail_duration).setText(getString(
                R.string.search_detail_duration, recipe.minutes));
        text(root, R.id.tv_recipe_detail_description).setVisibility(View.VISIBLE);
        text(root, R.id.tv_recipe_detail_description).setText(recipe.description);
        text(root, R.id.tv_recipe_detail_servings).setText(getResources().getQuantityString(
                R.plurals.search_detail_servings, recipe.servings, recipe.servings));
        ArrayList<String> ingredientRows = formatIngredientRows(recipe);
        text(root, R.id.tv_recipe_detail_ingredients_preview).setText(String.join("\n", ingredientRows));
        setupAddToShoppingList(root, recipe, ingredientRows);
        text(root, R.id.tv_recipe_detail_allergens).setText(formatAllergens(recipe));
        text(root, R.id.tv_recipe_detail_steps_preview).setText(recipe.directions);
    }

    private void setupRecipeMenu(MaterialToolbar toolbar, Recipe recipe) {
        toolbar.inflateMenu(R.menu.recipe_detail_menu);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() != R.id.recipe_detail_report) return false;

            MainActivity activity = (MainActivity) requireActivity();
            activity.openProtectedFragment(ReportFormFragment.newForRecipe(
                    recipe.title, recipe.description, recipe.photo));
            return true;
        });
    }

    private void setupAddToShoppingList(View root, Recipe recipe, ArrayList<String> ingredientRows) {
        View addButton = root.findViewById(R.id.recipe_detail_add_to_shopping);
        if (ingredientRows.isEmpty()) return;

        addButton.setVisibility(View.VISIBLE);
        addButton.setOnClickListener(view -> {
            MainActivity activity = (MainActivity) requireActivity();
            activity.openProtectedFragment(RecipeIngredientsToListFragment.newInstance(
                    getString(recipe.title), recipe.servings, ingredientRows));
        });
    }

    private void showPhoto(View root, Recipe recipe) {
        ImageView photo = root.findViewById(R.id.iv_recipe_detail_photo);
        if (recipe.photo == 0) {
            photo.setVisibility(View.GONE);
        } else {
            photo.setImageResource(recipe.photo);
        }
    }

    private ArrayList<String> formatIngredientRows(Recipe recipe) {
        ArrayList<String> ingredients = new ArrayList<>();
        for (Amount amount : recipe.ingredients) {
            ingredients.add(getString(R.string.search_detail_ingredient,
                    RecipeSearchPreviewData.ingredientName(amount.ingredientId), amount.quantity));
        }
        return ingredients;
    }

    private String formatAllergens(Recipe recipe) {
        List<String> names = new ArrayList<>();
        for (int allergen : recipe.allergens) names.add(getString(allergen));
        return names.isEmpty() ? getString(R.string.search_detail_no_allergens)
                : getString(R.string.search_detail_allergens, String.join(", ", names));
    }

    private static TextView text(View root, int viewId) {
        return root.findViewById(viewId);
    }
}
