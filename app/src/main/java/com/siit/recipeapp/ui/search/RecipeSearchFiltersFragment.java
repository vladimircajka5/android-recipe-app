package com.siit.recipeapp.ui.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.siit.recipeapp.R;
import com.siit.recipeapp.data.model.ingredient.Ingredient;
import com.siit.recipeapp.ui.preview.IngredientPreviewCatalog;
import com.siit.recipeapp.ui.preview.RecipeSearchPreviewData;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class RecipeSearchFiltersFragment extends Fragment {
    static final String RESULT = "recipe_search_filters";
    private static final String STATE_MANUAL_INGREDIENTS = "manual_ingredients";
    private static final int[] DISHES = {RecipeSearchFilters.ANY, R.string.search_dish_main,
            R.string.search_dish_dessert};
    private static final int[] DIETS = {RecipeSearchFilters.ANY, R.string.search_diet_vegetarian,
            R.string.search_diet_vegan};
    private static final int[] MEALS = {RecipeSearchFilters.ANY, R.string.search_meal_breakfast,
            R.string.search_meal_lunch, R.string.search_meal_dinner};
    private static final int[] METHODS = {RecipeSearchFilters.ANY, R.string.search_method_boiling,
            R.string.search_method_baking, R.string.search_method_frying};
    private static final int[] UNITS = {R.string.search_minutes, R.string.search_hours};
    private static final int[] SOURCES = {R.string.search_ingredients_off,
            R.string.search_ingredients_manual, R.string.search_ingredients_pantry};

    private final Set<String> selectedIngredients = new LinkedHashSet<>();

    public RecipeSearchFiltersFragment() {
        super(R.layout.fragment_recipe_search_filters);
    }

    static RecipeSearchFiltersFragment newInstance(Bundle filters) {
        RecipeSearchFiltersFragment fragment = new RecipeSearchFiltersFragment();
        fragment.setArguments(new Bundle(filters));
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle state) {
        MaterialToolbar toolbar = root.findViewById(R.id.recipe_filters_toolbar);
        toolbar.setNavigationOnClickListener(view -> getParentFragmentManager().popBackStack());
        Bundle applied = requireArguments();
        setupChoice(root, R.id.recipe_filter_dish, DISHES, applied.getInt(RecipeSearchFilters.DISH));
        setupChoice(root, R.id.recipe_filter_diet, DIETS, applied.getInt(RecipeSearchFilters.DIET));
        setupChoice(root, R.id.recipe_filter_meal, MEALS, applied.getInt(RecipeSearchFilters.MEAL));
        setupChoice(root, R.id.recipe_filter_method, METHODS, applied.getInt(RecipeSearchFilters.METHOD));
        setupSpinner(root, R.id.recipe_filter_min_unit, UNITS, applied.getInt(RecipeSearchFilters.MIN_UNIT));
        setupSpinner(root, R.id.recipe_filter_max_unit, UNITS, applied.getInt(RecipeSearchFilters.MAX_UNIT));
        restoreFields(root, applied);

        if (state != null) {
            restoreIngredientSelection(state.getStringArrayList(STATE_MANUAL_INGREDIENTS));
        } else if (selectedIngredients.isEmpty()) {
            restoreIngredientSelection(applied.getStringArrayList(RecipeSearchFilters.MANUAL_IDS));
        }
        setupIngredientPicker(root, applied.getInt(RecipeSearchFilters.INGREDIENT_SOURCE));
        root.findViewById(R.id.recipe_filters_apply).setOnClickListener(view -> applyFilters(root));
        root.findViewById(R.id.recipe_filters_clear).setOnClickListener(view -> publish(new Bundle()));
    }

    private void restoreFields(View root, Bundle values) {
        restoreNumber(root, R.id.recipe_filter_min_time, values, RecipeSearchFilters.MIN_TIME);
        restoreNumber(root, R.id.recipe_filter_max_time, values, RecipeSearchFilters.MAX_TIME);
        restoreNumber(root, R.id.recipe_filter_max_missing, values, RecipeSearchFilters.MAX_MISSING);
        List<Integer> allergens = values.getIntegerArrayList(RecipeSearchFilters.ALLERGENS);
        if (allergens != null) {
            checkbox(root, R.id.recipe_filter_gluten).setChecked(allergens.contains(R.string.search_allergen_gluten));
            checkbox(root, R.id.recipe_filter_milk).setChecked(allergens.contains(R.string.search_allergen_milk));
            checkbox(root, R.id.recipe_filter_eggs).setChecked(allergens.contains(R.string.search_allergen_egg));
        }
    }

    private void restoreIngredientSelection(@Nullable List<String> selected) {
        selectedIngredients.clear();
        if (selected != null) selectedIngredients.addAll(selected);
    }

    private void setupIngredientPicker(View root, int source) {
        Spinner sourcePicker = setupSpinner(root, R.id.recipe_filter_ingredients_source, SOURCES, source);
        EditText ingredientQuery = root.findViewById(R.id.recipe_filter_ingredient_query);
        sourcePicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View row, int position, long id) {
                renderIngredientPicker(root, position, ingredientQuery.getText().toString());
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        ingredientQuery.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence text, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence text, int start, int before, int count) {
                if (sourcePicker.getSelectedItemPosition() == RecipeSearchFilters.INGREDIENTS_MANUAL) {
                    showManualIngredients(root, text.toString());
                }
            }
            @Override public void afterTextChanged(Editable text) {}
        });
        renderIngredientPicker(root, source, ingredientQuery.getText().toString());
    }

    private void renderIngredientPicker(View root, int source, String query) {
        boolean manual = source == RecipeSearchFilters.INGREDIENTS_MANUAL;
        boolean pantry = source == RecipeSearchFilters.INGREDIENTS_PANTRY;
        root.findViewById(R.id.recipe_filter_manual_ingredients).setVisibility(manual ? View.VISIBLE : View.GONE);
        root.findViewById(R.id.recipe_filter_max_missing_layout)
                .setVisibility(manual || pantry ? View.VISIBLE : View.GONE);
        TextView pantrySummary = root.findViewById(R.id.recipe_filter_pantry_summary);
        pantrySummary.setVisibility(pantry ? View.VISIBLE : View.GONE);
        if (pantry) {
            List<String> names = new ArrayList<>();
            for (String id : RecipeSearchPreviewData.pantryIngredientIds()) {
                names.add(RecipeSearchPreviewData.ingredientName(id));
            }
            pantrySummary.setText(getString(R.string.search_pantry_contents, String.join(", ", names)));
        }
        if (manual) showManualIngredients(root, query);
    }

    private void showManualIngredients(View root, String query) {
        LinearLayout list = root.findViewById(R.id.recipe_filter_ingredient_choices);
        list.removeAllViews();
        String needle = query.trim().toLowerCase(Locale.ROOT);
        for (Ingredient ingredient : IngredientPreviewCatalog.INGREDIENTS) {
            if (!ingredient.getName().toLowerCase(Locale.ROOT).contains(needle)) continue;
            MaterialCheckBox choice = new MaterialCheckBox(requireContext());
            choice.setText(ingredient.getName());
            choice.setMinHeight(Math.round(48 * getResources().getDisplayMetrics().density));
            choice.setChecked(selectedIngredients.contains(ingredient.getId()));
            choice.setOnCheckedChangeListener((button, checked) -> {
                if (checked) selectedIngredients.add(ingredient.getId());
                else selectedIngredients.remove(ingredient.getId());
                updateSelectedCount(root);
            });
            list.addView(choice);
        }
        root.findViewById(R.id.recipe_filter_ingredient_empty)
                .setVisibility(list.getChildCount() == 0 ? View.VISIBLE : View.GONE);
        updateSelectedCount(root);
    }

    private void updateSelectedCount(View root) {
        TextView selectedCount = root.findViewById(R.id.recipe_filter_ingredient_count);
        selectedCount.setText(getString(R.string.search_selected_ingredients, selectedIngredients.size()));
    }

    private void applyFilters(View root) {
        Bundle values = new Bundle();
        values.putInt(RecipeSearchFilters.DISH, selectedChoice(root, R.id.recipe_filter_dish, DISHES));
        values.putInt(RecipeSearchFilters.DIET, selectedChoice(root, R.id.recipe_filter_diet, DIETS));
        values.putInt(RecipeSearchFilters.MEAL, selectedChoice(root, R.id.recipe_filter_meal, MEALS));
        values.putInt(RecipeSearchFilters.METHOD, selectedChoice(root, R.id.recipe_filter_method, METHODS));
        values.putInt(RecipeSearchFilters.MIN_UNIT, selection(root, R.id.recipe_filter_min_unit));
        values.putInt(RecipeSearchFilters.MAX_UNIT, selection(root, R.id.recipe_filter_max_unit));
        values.putInt(RecipeSearchFilters.INGREDIENT_SOURCE, selection(root, R.id.recipe_filter_ingredients_source));
        values.putStringArrayList(RecipeSearchFilters.MANUAL_IDS, new ArrayList<>(selectedIngredients));
        if (!readNumber(root, values, R.id.recipe_filter_min_time, RecipeSearchFilters.MIN_TIME)
                || !readNumber(root, values, R.id.recipe_filter_max_time, RecipeSearchFilters.MAX_TIME)
                || !readNumber(root, values, R.id.recipe_filter_max_missing, RecipeSearchFilters.MAX_MISSING)) return;
        if (!validateTimeRange(root, values)) return;
        values.putIntegerArrayList(RecipeSearchFilters.ALLERGENS, excludedAllergens(root));
        publish(values);
    }

    private boolean validateTimeRange(View root, Bundle values) {
        if (!values.containsKey(RecipeSearchFilters.MIN_TIME) || !values.containsKey(RecipeSearchFilters.MAX_TIME)) return true;
        int min = RecipeSearchFilters.toMinutes(values.getInt(RecipeSearchFilters.MIN_TIME), values.getInt(RecipeSearchFilters.MIN_UNIT));
        int max = RecipeSearchFilters.toMinutes(values.getInt(RecipeSearchFilters.MAX_TIME), values.getInt(RecipeSearchFilters.MAX_UNIT));
        if (min <= max) return true;
        EditText maximum = root.findViewById(R.id.recipe_filter_max_time);
        maximum.setError(getString(R.string.search_time_range_error));
        maximum.requestFocus();
        return false;
    }

    private ArrayList<Integer> excludedAllergens(View root) {
        ArrayList<Integer> allergens = new ArrayList<>();
        if (checkbox(root, R.id.recipe_filter_gluten).isChecked()) allergens.add(R.string.search_allergen_gluten);
        if (checkbox(root, R.id.recipe_filter_milk).isChecked()) allergens.add(R.string.search_allergen_milk);
        if (checkbox(root, R.id.recipe_filter_eggs).isChecked()) allergens.add(R.string.search_allergen_egg);
        return allergens;
    }

    private void publish(Bundle values) {
        getParentFragmentManager().setFragmentResult(RESULT, values);
        getParentFragmentManager().popBackStack();
    }

    private void setupChoice(View root, int viewId, int[] options, int value) {
        int position = 0;
        for (int i = 0; i < options.length; i++) {
            if (options[i] == value) position = i;
        }
        setupSpinner(root, viewId, options, position);
    }

    private Spinner setupSpinner(View root, int viewId, int[] options, int position) {
        Spinner spinner = root.findViewById(viewId);
        List<String> labels = new ArrayList<>();
        for (int option : options) labels.add(getString(option));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(position);
        return spinner;
    }

    private static int selectedChoice(View root, int viewId, int[] options) {
        return options[selection(root, viewId)];
    }

    private static int selection(View root, int viewId) {
        Spinner spinner = root.findViewById(viewId);
        return Math.max(0, spinner.getSelectedItemPosition());
    }

    private static MaterialCheckBox checkbox(View root, int viewId) {
        return root.findViewById(viewId);
    }

    private static void restoreNumber(View root, int viewId, Bundle values, String key) {
        if (values.containsKey(key)) {
            EditText field = root.findViewById(viewId);
            field.setText(String.valueOf(values.getInt(key)));
        }
    }

    private boolean readNumber(View root, Bundle values, int viewId, String key) {
        EditText field = root.findViewById(viewId);
        String text = field.getText().toString().trim();
        if (text.isEmpty()) return true;
        try {
            int value = Integer.parseInt(text);
            if (value < 0 || value > 999) throw new NumberFormatException();
            values.putInt(key, value);
            return true;
        } catch (NumberFormatException failure) {
            field.setError(getString(R.string.search_number_error));
            field.requestFocus();
            return false;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle state) {
        state.putStringArrayList(STATE_MANUAL_INGREDIENTS, new ArrayList<>(selectedIngredients));
        super.onSaveInstanceState(state);
    }
}
