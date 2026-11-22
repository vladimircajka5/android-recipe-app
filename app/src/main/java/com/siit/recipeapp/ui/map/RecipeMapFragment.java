package com.siit.recipeapp.ui.map;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.siit.recipeapp.R;
import com.siit.recipeapp.data.model.location.Location;
import com.siit.recipeapp.ui.map.RecipeMapPreviewData.Entry;
import com.siit.recipeapp.ui.map.RecipeMapPreviewData.Filters;
import com.siit.recipeapp.ui.map.RecipeMapPreviewData.Place;
import com.siit.recipeapp.ui.recipe.RecipeDetailFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecipeMapFragment extends Fragment {

    private static final String STATE_FILTERS = "recipe_map_filters";

    private Filters filters = new Filters();

    private View screen;
    private MaterialAutoCompleteTextView locationField;
    private TextInputLayout locationInput;
    private OnBackPressedCallback levelBack;
    private AlertDialog filterDialog;

    public RecipeMapFragment() {
        super(R.layout.fragment_recipe_map);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            filters = new Filters(
                    savedInstanceState.getBundle(STATE_FILTERS));
        }
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        screen = view;

        MaterialToolbar toolbar = view.findViewById(
                R.id.toolbar_recipe_map);

        toolbar.setNavigationOnClickListener(v -> goBack());

        levelBack = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                goBack();
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(), levelBack);

        locationInput = view.findViewById(R.id.input_map_location);
        locationField = view.findViewById(R.id.edit_map_location);

        locationInput.setEndIconOnClickListener(v -> findLocation());

        locationField.setOnItemClickListener(
                (parent, selected, position, id) -> findLocation());

        locationField.setOnEditorActionListener((field, action, event) -> {
            if (action == EditorInfo.IME_ACTION_SEARCH) {
                findLocation();
                return true;
            }

            return false;
        });

        Spinner source = view.findViewById(R.id.spinner_map_source);

        ArrayAdapter<CharSequence> sources =
                ArrayAdapter.createFromResource(
                        requireContext(),
                        R.array.map_sources,
                        android.R.layout.simple_spinner_item);

        sources.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        source.setAdapter(sources);
        source.setSelection(filters.source);

        source.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View selected,
                            int position,
                            long id) {

                        if (screen != view || filters.source == position) {
                            return;
                        }

                        filters.source = position;
                        filters.country = "";
                        filters.region = "";
                        render();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

        view.findViewById(R.id.btn_map_filters)
                .setOnClickListener(v -> showFilters());

        view.findViewById(R.id.btn_map_clear)
                .setOnClickListener(v -> clearFilters());

        view.findViewById(R.id.btn_map_up)
                .setOnClickListener(v -> goBack());

        render();
    }

    private void render() {
        if (screen == null) {
            return;
        }

        List<Entry> entries = RecipeMapPreviewData.select(filters);

        LinearLayout groups = screen.findViewById(
                R.id.layout_map_groups);

        LinearLayout recipes = screen.findViewById(
                R.id.layout_map_recipes);

        groups.removeAllViews();
        recipes.removeAllViews();

        List<String> path = new ArrayList<>();
        path.add(getString(R.string.map_world));

        if (!filters.country.isEmpty()) {
            path.add(filters.country);
        }

        if (!filters.region.isEmpty()) {
            path.add(filters.region);
        }

        text(R.id.tv_map_path).setText(String.join(" › ", path));
        text(R.id.tv_map_count).setText(recipeCount(entries.size()));

        int active = filters.count();

        ((MaterialButton) screen.findViewById(R.id.btn_map_filters))
                .setText(active == 0
                        ? getString(R.string.map_filters)
                        : getString(R.string.map_filters_count, active));

        screen.findViewById(R.id.btn_map_clear)
                .setEnabled(active > 0);

        boolean canGoUp = !filters.country.isEmpty();
        levelBack.setEnabled(canGoUp);

        screen.findViewById(R.id.btn_map_up)
                .setVisibility(canGoUp ? View.VISIBLE : View.GONE);

        screen.findViewById(R.id.tv_map_empty)
                .setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);

        boolean showRecipes =
                !filters.region.isEmpty() && !entries.isEmpty();

        screen.findViewById(R.id.tv_map_recipes_heading)
                .setVisibility(showRecipes ? View.VISIBLE : View.GONE);

        if (entries.isEmpty()) {
            text(R.id.tv_map_hint).setVisibility(View.GONE);
        } else {
            text(R.id.tv_map_hint).setVisibility(View.VISIBLE);

            if (filters.country.isEmpty()) {
                text(R.id.tv_map_hint)
                        .setText(R.string.map_choose_country);
            } else if (filters.region.isEmpty()) {
                text(R.id.tv_map_hint)
                        .setText(R.string.map_choose_region);
            } else {
                text(R.id.tv_map_hint)
                        .setText(R.string.map_choose_recipe);
            }
        }

        if (filters.region.isEmpty()) {
            for (Map.Entry<String, Integer> group :
                    RecipeMapPreviewData.groups(entries, filters).entrySet()) {

                String label = group.getKey();

                addGroup(
                        groups,
                        getString(
                                R.string.map_group,
                                label,
                                recipeCount(group.getValue())),
                        () -> {
                            if (filters.country.isEmpty()) {
                                filters.country = label;
                            } else {
                                filters.region = label;
                            }

                            render();
                        });
            }
        } else if (!entries.isEmpty()) {
            addGroup(
                    groups,
                    getString(R.string.map_view_recipes),
                    () -> {
                        NestedScrollView scroll =
                                screen.findViewById(R.id.scroll_recipe_map);

                        scroll.smoothScrollTo(
                                0,
                                screen.findViewById(
                                        R.id.tv_map_recipes_heading).getTop());
                    });

            for (Entry entry : entries) {
                addRecipe(recipes, entry);
            }
        }

        List<String> suggestions = new ArrayList<>();

        for (Place place : RecipeMapPreviewData.places(filters.source)) {
            suggestions.add(place.label());
        }

        locationField.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                suggestions));

        String selectedLocation = filters.region.isEmpty()
                ? filters.country
                : filters.region + " · " + filters.country;

        locationField.setText(selectedLocation, false);
        locationInput.setError(null);
    }

    private void addGroup(
            LinearLayout container,
            String label,
            Runnable action) {

        MaterialButton button = new MaterialButton(requireContext());

        button.setText(label);
        button.setTextColor(color(R.color.app_text));
        button.setBackgroundTintList(
                ColorStateList.valueOf(color(R.color.app_surface)));
        button.setStrokeColor(
                ColorStateList.valueOf(color(R.color.app_outline)));
        button.setStrokeWidth(dp(1));
        button.setCornerRadius(dp(24));
        button.setIconResource(R.drawable.ic_recipe_map);
        button.setIconTint(
                ColorStateList.valueOf(color(R.color.app_primary)));
        button.setMinHeight(dp(48));
        button.setAllCaps(false);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        params.bottomMargin = dp(4);
        button.setLayoutParams(params);
        button.setOnClickListener(v -> action.run());

        container.addView(button);
    }

    private void addRecipe(LinearLayout container, Entry entry) {
        View card = LayoutInflater.from(container.getContext()).inflate(
                R.layout.item_map_recipe, container, false);

        ((TextView) card.findViewById(R.id.tv_map_recipe_title))
                .setText(entry.recipe.title);

        ((TextView) card.findViewById(R.id.tv_map_recipe_author))
                .setText(getString(
                        R.string.map_recipe_author, entry.author));

        ((TextView) card.findViewById(R.id.tv_map_recipe_meta))
                .setText(getString(
                        R.string.search_result_meta,
                        getString(entry.recipe.dish),
                        getString(entry.recipe.meal),
                        entry.recipe.minutes));

        ((TextView) card.findViewById(R.id.tv_map_recipe_origin))
                .setText(getString(
                        R.string.map_recipe_origin,
                        locationName(entry.origin)));

        ((TextView) card.findViewById(R.id.tv_map_recipe_rating))
                .setText(entry.rating == null
                        ? getString(R.string.map_recipe_not_rated)
                        : getString(
                        R.string.map_recipe_rating, entry.rating));

        ImageView photo = card.findViewById(R.id.iv_map_recipe);

        if (entry.recipe.photo == 0) {
            photo.setImageResource(R.drawable.ic_nav_recipes);
            photo.setImageTintList(
                    ColorStateList.valueOf(color(R.color.app_text_secondary)));
            photo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            photo.setPadding(dp(36), dp(36), dp(36), dp(36));
            photo.setContentDescription(getString(R.string.map_no_photo));
        } else {
            photo.setImageResource(entry.recipe.photo);
        }

        card.setOnClickListener(v -> {
            if (!isResumed()
                    || getParentFragmentManager().isStateSaved()) {
                return;
            }

            getParentFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(
                            R.id.fragment_container,
                            RecipeDetailFragment.newPreview(entry.recipe.id))
                    .addToBackStack(null)
                    .commit();
        });

        container.addView(card);
    }

    private void findLocation() {
        String query = locationField.getText().toString().trim();

        if (query.isEmpty()) {
            filters.country = "";
            filters.region = "";
        } else {
            Place place = RecipeMapPreviewData.findPlace(
                    filters.source, query);

            if (place == null) {
                locationInput.setError(
                        getString(R.string.map_location_not_found));
                return;
            }

            filters.country = place.country;
            filters.region = place.region;
        }

        locationField.dismissDropDown();

        InputMethodManager keyboard = (InputMethodManager)
                requireContext().getSystemService(
                        Context.INPUT_METHOD_SERVICE);

        if (keyboard != null) {
            keyboard.hideSoftInputFromWindow(
                    locationField.getWindowToken(), 0);
        }

        locationField.clearFocus();
        render();
    }

    private void goBack() {
        if (screen == null || !isResumed()) {
            return;
        }

        if (!filters.region.isEmpty()) {
            filters.region = "";
            render();
        } else if (!filters.country.isEmpty()) {
            filters.country = "";
            render();
        } else if (!getParentFragmentManager().isStateSaved()) {
            getParentFragmentManager().popBackStack();
        }
    }

    private void clearFilters() {
        int source = filters.source;
        filters = new Filters();
        filters.source = source;
        render();
    }

    private void showFilters() {
        if (!isResumed()
                || (filterDialog != null && filterDialog.isShowing())) {
            return;
        }

        LinearLayout form = new LinearLayout(requireContext());
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(24), dp(8), dp(24), dp(8));

        ScrollView scroll = new ScrollView(requireContext());
        scroll.addView(form);

        List<Integer> dishes = RecipeMapPreviewData.types(true);
        List<Integer> meals = RecipeMapPreviewData.types(false);

        Spinner dish = addChoice(
                form,
                R.string.search_dish,
                typeLabels(dishes),
                index(dishes, filters.dish));

        Spinner meal = addChoice(
                form,
                R.string.search_meal,
                typeLabels(meals),
                index(meals, filters.meal));

        List<String> countries = new ArrayList<>();
        countries.add("");
        countries.addAll(
                RecipeMapPreviewData.countries(filters.source));

        List<String> countryLabels = new ArrayList<>(countries);
        countryLabels.set(0, getString(R.string.map_any));

        Spinner country = addChoice(
                form,
                R.string.map_country,
                countryLabels,
                index(countries, filters.country));

        Spinner region = addChoice(
                form,
                R.string.map_region,
                List.of(getString(R.string.map_any)),
                0);

        List<Place> regionOptions = new ArrayList<>();

        updateRegions(
                region,
                regionOptions,
                countries.get(country.getSelectedItemPosition()),
                filters.region);

        int[] lastCountry = {country.getSelectedItemPosition()};

        country.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View selected,
                            int position,
                            long id) {

                        if (position == lastCountry[0]) {
                            return;
                        }

                        lastCountry[0] = position;

                        updateRegions(
                                region,
                                regionOptions,
                                countries.get(position),
                                "");
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

        List<Integer> times = List.of(0, 15, 30, 45, 60, 90, 120);
        List<String> timeLabels = new ArrayList<>();

        for (int minutes : times) {
            timeLabels.add(minutes == 0
                    ? getString(R.string.map_any)
                    : getString(R.string.map_time_option, minutes));
        }

        Spinner time = addChoice(
                form,
                R.string.map_max_time,
                timeLabels,
                index(times, filters.maxMinutes));

        List<Double> ratings = List.of(
                0.0, 3.0, 3.5, 4.0, 4.5, 5.0);

        List<String> ratingLabels = new ArrayList<>();

        for (double rating : ratings) {
            ratingLabels.add(rating == 0
                    ? getString(R.string.map_any)
                    : getString(R.string.map_rating_option, rating));
        }

        Spinner rating = addChoice(
                form,
                R.string.map_min_rating,
                ratingLabels,
                index(ratings, filters.minRating));

        AlertDialog dialog =
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.map_filters)
                        .setView(scroll)
                        .setNegativeButton(R.string.map_cancel, null)
                        .setNeutralButton(
                                R.string.map_clear,
                                (ignored, which) -> clearFilters())
                        .setPositiveButton(
                                R.string.map_apply,
                                (ignored, which) -> {
                                    filters.dish = dishes.get(
                                            dish.getSelectedItemPosition());

                                    filters.meal = meals.get(
                                            meal.getSelectedItemPosition());

                                    filters.country = countries.get(
                                            country.getSelectedItemPosition());

                                    Place selectedRegion = regionOptions.get(
                                            region.getSelectedItemPosition());

                                    filters.region = selectedRegion.region;

                                    if (!selectedRegion.region.isEmpty()) {
                                        filters.country = selectedRegion.country;
                                    }

                                    filters.maxMinutes = times.get(
                                            time.getSelectedItemPosition());

                                    filters.minRating = ratings.get(
                                            rating.getSelectedItemPosition());

                                    render();
                                })
                        .create();

        filterDialog = dialog;

        dialog.setOnDismissListener(ignored -> {
            if (filterDialog == dialog) {
                filterDialog = null;
            }
        });

        dialog.show();
    }

    private void updateRegions(
            Spinner spinner,
            List<Place> options,
            String country,
            String selectedRegion) {

        options.clear();
        options.add(new Place("", ""));
        options.addAll(
                RecipeMapPreviewData.regions(filters.source, country));

        List<String> labels = new ArrayList<>();
        int selection = 0;

        for (int i = 0; i < options.size(); i++) {
            Place place = options.get(i);

            labels.add(i == 0
                    ? getString(R.string.map_any)
                    : place.label());

            if (i > 0
                    && place.country.equals(country)
                    && place.region.equals(selectedRegion)) {
                selection = i;
            }
        }

        configureSpinner(spinner, labels, selection);
    }

    private Spinner addChoice(
            LinearLayout form,
            int labelResource,
            List<String> labels,
            int selection) {

        TextView label = new TextView(requireContext());
        label.setText(labelResource);
        label.setTextColor(color(R.color.app_text));
        label.setTextSize(14);

        LinearLayout.LayoutParams labelParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        labelParams.topMargin = dp(12);
        form.addView(label, labelParams);

        Spinner spinner = new Spinner(
                requireContext(), Spinner.MODE_DIALOG);

        spinner.setPrompt(getString(labelResource));
        spinner.setMinimumHeight(dp(48));

        configureSpinner(spinner, labels, selection);

        form.addView(spinner, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        return spinner;
    }

    private void configureSpinner(
            Spinner spinner,
            List<String> labels,
            int selection) {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                labels);

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setSelection(selection);
    }

    private List<String> typeLabels(List<Integer> types) {
        List<String> labels = new ArrayList<>();

        for (int type : types) {
            labels.add(getString(
                    type == 0 ? R.string.map_any : type));
        }

        return labels;
    }

    private int index(List<?> values, Object selected) {
        return Math.max(0, values.indexOf(selected));
    }

    private String locationName(Location location) {
        if (location == null) {
            return getString(R.string.map_unknown_origin);
        }

        List<String> parts = new ArrayList<>();

        for (String part : new String[]{
                location.getCity(),
                location.getRegion(),
                location.getCountry()
        }) {
            if (part != null && !part.trim().isEmpty()) {
                parts.add(part.trim());
            }
        }

        return parts.isEmpty()
                ? getString(R.string.map_unknown_origin)
                : String.join(", ", parts);
    }

    private String recipeCount(int count) {
        return getResources().getQuantityString(
                R.plurals.map_recipe_count, count, count);
    }

    private TextView text(int id) {
        return screen.findViewById(id);
    }

    private int color(int id) {
        return ContextCompat.getColor(requireContext(), id);
    }

    private int dp(int value) {
        return Math.round(
                value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBundle(STATE_FILTERS, filters.save());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        if (filterDialog != null) {
            filterDialog.dismiss();
            filterDialog = null;
        }

        locationField = null;
        locationInput = null;
        levelBack = null;
        screen = null;

        super.onDestroyView();
    }
}