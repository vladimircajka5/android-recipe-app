package com.siit.recipeapp.ui.pantry;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.siit.recipeapp.R;
import com.siit.recipeapp.data.model.ingredient.Ingredient;
import com.siit.recipeapp.data.model.ingredient.IngredientKind;
import com.siit.recipeapp.data.model.ingredient.MeasurementUnit;
import com.siit.recipeapp.data.model.ingredient.Product;
import com.siit.recipeapp.data.model.pantry.PantryItem;
import com.siit.recipeapp.ui.preview.PantryPreviewData;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class PantryProductReviewFragment extends Fragment {
    public static final String ARG_BARCODE = "barcode";
    public static final String ARG_NAME = "product_name";
    public static final String ARG_BRAND = "product_brand";
    public static final String ARG_QUANTITY = "package_quantity";
    public static final String ARG_UNIT = "package_unit";
    private final List<Ingredient> ingredients = new ArrayList<>();
    private final List<MeasurementUnit> units = new ArrayList<>();
    private PantryViewModel viewModel;
    private String selectedIngredientId;
    private LocalDate expiry;

    public PantryProductReviewFragment() { super(R.layout.fragment_pantry_product_review); }

    @Override public void onViewCreated(@NonNull View root, @Nullable Bundle state) {
        viewModel = new ViewModelProvider(requireActivity(), new PantryViewModelFactory(requireContext())).get(PantryViewModel.class);
        ingredients.addAll(viewModel.getIngredients());
        units.addAll(PantryPreviewData.UNITS);
        Bundle args = getArguments() == null ? Bundle.EMPTY : getArguments();
        EditText barcode = root.findViewById(R.id.product_barcode); barcode.setText(args.getString(ARG_BARCODE, ""));
        ((EditText) root.findViewById(R.id.product_name)).setText(args.getString(ARG_NAME, ""));
        ((EditText) root.findViewById(R.id.product_brand)).setText(args.getString(ARG_BRAND, ""));
        if (args.containsKey(ARG_QUANTITY)) ((EditText) root.findViewById(R.id.product_quantity)).setText(Double.toString(args.getDouble(ARG_QUANTITY)));
        setupUnits(root, args.getString(ARG_UNIT));
        setupIngredients(root, args.getString(ARG_NAME));
        setupCategories(root);
        root.findViewById(R.id.product_create_ingredient).setOnClickListener(v -> updateIngredientMode(root));
        root.findViewById(R.id.product_date).setOnClickListener(v -> chooseDate(root));
        root.findViewById(R.id.product_clear_date).setOnClickListener(v -> { expiry = null; updateDate(root); });
        root.findViewById(R.id.product_cancel).setOnClickListener(v -> getParentFragmentManager().popBackStack("pantry-scanner", androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE));
        root.findViewById(R.id.product_save).setOnClickListener(v -> save(root));
        ((TextView) root.findViewById(R.id.product_review_result)).setText(args.containsKey(ARG_NAME)
                ? R.string.product_review_found : R.string.product_review_not_found);
        updateIngredientMode(root);
    }

    private void setupUnits(View root, String selectedUnit) {
        List<String> labels = new ArrayList<>(); labels.add(getString(R.string.pantry_no_unit));
        int selected = 0;
        for (int i = 0; i < units.size(); i++) {
            MeasurementUnit unit = units.get(i); labels.add(unit.toString());
            if (unit.getId().equals(selectedUnit)) selected = i + 1;
        }
        Spinner spinner = root.findViewById(R.id.product_unit);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); spinner.setAdapter(adapter); spinner.setSelection(selected);
    }

    private void setupIngredients(View root, String productName) {
        Spinner spinner = root.findViewById(R.id.product_ingredient);
        List<String> labels = new ArrayList<>();
        labels.add(getString(R.string.product_select_ingredient_hint));
        for (Ingredient ingredient : ingredients) labels.add(ingredient.getName());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View row, int position, long id) {
                selectedIngredientId = position == 0 ? null : ingredients.get(position - 1).getId();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { selectedIngredientId = null; }
        });
        if (productName == null) return;
        String normalized = productName.toLowerCase(Locale.ROOT);
        for (int i = 0; i < ingredients.size(); i++) {
            Ingredient candidate = ingredients.get(i);
            String candidateName = candidate.getName().toLowerCase(Locale.ROOT);
            if (normalized.equals(candidateName) || normalized.contains(candidateName) || candidateName.contains(normalized)) {
                spinner.setSelection(i + 1);
                selectedIngredientId = candidate.getId();

                ((TextView) root.findViewById(R.id.product_suggestion)).setText(
                        getString(R.string.product_suggested_match, candidate.getName()));
                return;
            }
        }
    }

    private void setupCategories(View root) {
        Spinner spinner = root.findViewById(R.id.product_category);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.ingredient_categories,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); spinner.setAdapter(adapter);
    }

    private void updateIngredientMode(View root) {
        boolean create = ((MaterialCheckBox) root.findViewById(R.id.product_create_ingredient)).isChecked();
        root.findViewById(R.id.product_ingredient_input).setVisibility(create ? View.GONE : View.VISIBLE);
        root.findViewById(R.id.product_category_input).setVisibility(create ? View.VISIBLE : View.GONE);
        root.findViewById(R.id.product_suggestion).setVisibility(create ? View.GONE : View.VISIBLE);
        if (create) {
            selectedIngredientId = null;
            ((Spinner) root.findViewById(R.id.product_ingredient)).setSelection(0);
        }
    }

    private void chooseDate(View root) {
        LocalDate initial = expiry == null ? LocalDate.now() : expiry;
        new DatePickerDialog(requireContext(), (picker, year, month, day) -> { expiry = LocalDate.of(year, month + 1, day); updateDate(root); },
                initial.getYear(), initial.getMonthValue() - 1, initial.getDayOfMonth()).show();
    }

    private void updateDate(View root) {
        ((MaterialButton) root.findViewById(R.id.product_date)).setText(expiry == null ? getString(R.string.pantry_choose_date) : expiry.toString());
        root.findViewById(R.id.product_clear_date).setVisibility(expiry == null ? View.GONE : View.VISIBLE);
    }

    private void save(View root) {
        EditText codeField = root.findViewById(R.id.product_barcode);
        EditText nameField = root.findViewById(R.id.product_name);
        EditText brandField = root.findViewById(R.id.product_brand);
        String code = codeField.getText().toString().trim();
        if (code.isEmpty()) { codeField.setError(getString(R.string.barcode_required)); return; }
        if (!code.matches("[0-9]{8,14}")) { codeField.setError(getString(R.string.barcode_invalid)); return; }
        if (viewModel.findProductByBarcode(code) != null) {
            codeField.setError(getString(R.string.product_barcode_duplicate)); return;
        }

        Double packageQuantity = null;
        String raw = ((EditText) root.findViewById(R.id.product_quantity)).getText().toString().trim();
        if (!raw.isEmpty()) {
            try { packageQuantity = Double.parseDouble(raw.replace(',', '.')); }
            catch (NumberFormatException invalid) {
                ((EditText) root.findViewById(R.id.product_quantity)).setError(getString(R.string.pantry_quantity_invalid)); return;
            }
            if (!Double.isFinite(packageQuantity) || packageQuantity <= 0) {
                ((EditText) root.findViewById(R.id.product_quantity)).setError(getString(R.string.pantry_quantity_invalid)); return;
            }
        }
        String unitId = selectedUnit(root);
        if (packageQuantity != null && unitId == null) {
            ((EditText) root.findViewById(R.id.product_quantity)).setError(getString(R.string.pantry_quantity_needs_unit)); return;
        }

        boolean create = ((MaterialCheckBox) root.findViewById(R.id.product_create_ingredient)).isChecked();
        String ingredientId;
        if (create) {
            String name = nameField.getText().toString().trim();
            if (name.isEmpty()) { nameField.setError(getString(R.string.product_name_required)); return; }
            String[] categoryIds = getResources().getStringArray(R.array.ingredient_category_ids);
            int categoryIndex = ((Spinner) root.findViewById(R.id.product_category)).getSelectedItemPosition();
            ingredientId = UUID.randomUUID().toString();
            viewModel.saveIngredient(new Ingredient(ingredientId, name, categoryIds[categoryIndex], unitId, IngredientKind.BRANDED));
        } else {
            if (selectedIngredientId == null) {
                ((TextView) root.findViewById(R.id.product_suggestion)).setText(R.string.product_choose_ingredient_error); return;
            }
            ingredientId = selectedIngredientId;
        }

        String brand = brandField.getText().toString().trim();
        if (brand.isEmpty()) brand = null;
        String productId = UUID.randomUUID().toString();
        Product product = new Product(productId, code, brand, packageQuantity, unitId, null, ingredientId);
        PantryItem item = new PantryItem(UUID.randomUUID().toString(), PantryPreviewData.OWNER_ID,
                ingredientId, productId, brand, 1.0, "piece", expiry);
        viewModel.saveProduct(product);
        viewModel.saveItem(item);
        getParentFragmentManager().popBackStack("pantry-scanner", androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    private String selectedUnit(View root) {
        int position = ((Spinner) root.findViewById(R.id.product_unit)).getSelectedItemPosition();
        return position <= 0 || position > units.size() ? null : units.get(position - 1).getId();
    }
}
