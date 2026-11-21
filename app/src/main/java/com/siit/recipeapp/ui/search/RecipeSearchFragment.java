package com.siit.recipeapp.ui.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.siit.recipeapp.R;
import com.siit.recipeapp.ui.preview.RecipeSearchPreviewData;
import com.siit.recipeapp.ui.preview.RecipeSearchPreviewData.Recipe;
import com.siit.recipeapp.ui.recipe.RecipeDetailFragment;

import android.widget.LinearLayout;
import java.util.List;
import java.util.Locale;

public final class RecipeSearchFragment extends Fragment {
    private static final String STATE_FILTERS = "filters";
    private Bundle filterValues = new Bundle();

    public RecipeSearchFragment() {
        super(R.layout.fragment_recipe_search);
    }

    @Override
    public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        if (state != null && state.getBundle(STATE_FILTERS) != null) {
            filterValues = state.getBundle(STATE_FILTERS);
        }
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle state) {
        MaterialToolbar toolbar = root.findViewById(R.id.recipe_search_toolbar);
        toolbar.setNavigationOnClickListener(view -> getParentFragmentManager().popBackStack());
        MaterialButton filters = root.findViewById(R.id.recipe_search_filters);
        filters.setOnClickListener(view -> open(RecipeSearchFiltersFragment.newInstance(filterValues)));

        TextInputEditText query = root.findViewById(R.id.recipe_search_query);
        query.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence text, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence text, int start, int before, int count) {
                showResults(root, text.toString());
            }
            @Override public void afterTextChanged(Editable text) {}
        });
        getParentFragmentManager().setFragmentResultListener(RecipeSearchFiltersFragment.RESULT,
                getViewLifecycleOwner(), (key, values) -> {
                    filterValues = new Bundle(values);
                    showResults(root, query.getText() == null ? "" : query.getText().toString());
                });
        showResults(root, "");
    }

    private void showResults(View root, String query) {
        RecipeSearchFilters filters = new RecipeSearchFilters(filterValues);
        MaterialButton filterButton = root.findViewById(R.id.recipe_search_filters);
        filterButton.setText(filters.hasAdvancedFilters()
                ? R.string.search_filters_active : R.string.search_filters);

        LinearLayout results = root.findViewById(R.id.recipe_search_results);
        results.removeAllViews();
        String needle = query.trim().toLowerCase(Locale.ROOT);
        for (Recipe recipe : RecipeSearchPreviewData.RECIPES) {
            String title = getString(recipe.title).toLowerCase(Locale.ROOT);
            if (!title.contains(needle) || !filters.matches(recipe)) continue;

            List<String> missing = filters.missingIngredients(recipe);
            if (missing.size() <= filters.maxMissing()) {
                addResult(results, recipe, filters.usesIngredients(), missing);
            }
        }
        root.findViewById(R.id.recipe_search_empty).setVisibility(
                results.getChildCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private void addResult(LinearLayout results, Recipe recipe, boolean showAvailability,
                           List<String> missing) {
        View row = LayoutInflater.from(results.getContext())
                .inflate(R.layout.item_recipe_search_result, results, false);
        ((TextView) row.findViewById(R.id.search_result_title)).setText(recipe.title);
        ((TextView) row.findViewById(R.id.search_result_summary)).setText(recipe.description);
        ((TextView) row.findViewById(R.id.search_result_meta)).setText(getString(
                R.string.search_result_meta, getString(recipe.dish), getString(recipe.meal), recipe.minutes));
        TextView availability = row.findViewById(R.id.search_result_availability);
        if (showAvailability) {
            availability.setVisibility(View.VISIBLE);
            availability.setText(missing.isEmpty() ? getString(R.string.search_can_make)
                    : getString(R.string.search_missing, missing.size(), TextUtils.join(", ", missing)));
        }
        row.setOnClickListener(view -> open(RecipeDetailFragment.newPreview(recipe.id)));
        results.addView(row);
    }

    private void open(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle state) {
        state.putBundle(STATE_FILTERS, filterValues);
        super.onSaveInstanceState(state);
    }
}
