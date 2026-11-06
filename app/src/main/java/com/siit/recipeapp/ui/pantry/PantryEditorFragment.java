package com.siit.recipeapp.ui.pantry;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.siit.recipeapp.R;
import com.siit.recipeapp.ui.preview.PantryPreviewData;
import com.siit.recipeapp.ui.pantry.PantryViewModel;
import com.siit.recipeapp.data.model.ingredient.Ingredient;
import com.siit.recipeapp.data.model.ingredient.MeasurementUnit;
import com.siit.recipeapp.data.model.pantry.PantryItem;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PantryEditorFragment extends Fragment {
    public static final String ARG_ITEM_ID = "pantry_item_id";
    private final List<Ingredient> ingredients = new ArrayList<>();
    private final List<MeasurementUnit> units = new ArrayList<>();
    private String selectedIngredientId;
    private String boundIngredientId;
    private PantryViewModel viewModel;
    private LocalDate expirationDate;
    private String itemId;
    private boolean editing;
    private boolean loaded;

    public PantryEditorFragment() { super(R.layout.fragment_pantry_editor); }

    @Override public void onViewCreated(@NonNull View root, @Nullable Bundle state) {
        viewModel = new ViewModelProvider(requireActivity(), new PantryViewModelFactory()).get(PantryViewModel.class);
        String existingId = getArguments() == null ? null : getArguments().getString(ARG_ITEM_ID);
        editing = existingId != null;
        itemId = state != null ? state.getString("draft_id") : existingId;
        if (itemId == null) itemId = UUID.randomUUID().toString();
        ((TextView) root.findViewById(R.id.pantry_editor_title)).setText(editing ? R.string.pantry_edit_title : R.string.pantry_add_title);
        root.findViewById(R.id.pantry_cancel).setOnClickListener(v -> getParentFragmentManager().popBackStack());
        root.findViewById(R.id.pantry_save).setOnClickListener(v -> save());
        root.findViewById(R.id.pantry_delete).setVisibility(editing ? View.VISIBLE : View.GONE);
        root.findViewById(R.id.pantry_delete).setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.pantry_delete_title).setMessage(R.string.pantry_delete_message)
                .setNegativeButton(R.string.pantry_cancel, null).setPositiveButton(R.string.pantry_delete, (dialog, which) -> delete()).show());
        root.findViewById(R.id.pantry_date).setOnClickListener(v -> {
            LocalDate date = expirationDate == null ? LocalDate.now() : expirationDate;
            new DatePickerDialog(requireContext(), (picker, year, month, day) -> {
                expirationDate = LocalDate.of(year, month + 1, day); updateDate(root);
            }, date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth()).show();
        });
        root.findViewById(R.id.pantry_clear_date).setOnClickListener(v -> { expirationDate = null; updateDate(root); });
        ingredients.clear(); ingredients.addAll(PantryPreviewData.INGREDIENTS);
        units.clear(); units.addAll(PantryPreviewData.UNITS);
        PantryItem current = null;
        if (editing) {
            for (PantryItem item : viewModel.getItems()) {
                if (itemId.equals(item.getId())) current = item;
            }
        }
        if (editing && current == null) {
            error(root, R.string.pantry_missing_item);
            root.findViewById(R.id.pantry_save).setEnabled(false);
            root.findViewById(R.id.pantry_delete).setEnabled(false);
            return;
        }
        bind(root, current, state);
        loaded = true;
    }

    @Override public void onViewStateRestored(@Nullable Bundle state) {
        super.onViewStateRestored(state);
        // Restoring the text field can trigger its watcher; restore only a valid selection.
        if (!loaded) return;
        EditText field = requireView().findViewById(R.id.pantry_ingredient);
        for (Ingredient ingredient : ingredients) {
            if (ingredient.getId().equals(boundIngredientId)
                    && ingredient.getName().contentEquals(field.getText())) {
                selectedIngredientId = boundIngredientId;
            }
        }
    }

    private void bind(View root, PantryItem item, Bundle state) {
        MaterialAutoCompleteTextView ingredient = root.findViewById(R.id.pantry_ingredient);
        ingredient.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, ingredients));
        List<String> labels = new ArrayList<>(); labels.add(getString(R.string.pantry_no_unit));
        for (MeasurementUnit unit : units) labels.add(unit.toString());
        Spinner spinner = root.findViewById(R.id.pantry_unit);
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, labels);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(unitAdapter);
        if (state != null && !state.getBoolean("draft_ready")) state = null;
        selectedIngredientId = state != null ? state.getString("ingredient_id") : item == null ? null : item.getIngredientId();
        String selectedUnit = state != null ? state.getString("unit_id") : item == null ? null : item.getUnitId();
        for (Ingredient entry : ingredients) if (entry.getId().equals(selectedIngredientId)) ingredient.setText(entry.getName(), false);
        if (state != null) ingredient.setText(state.getString("ingredient_text", ""), false);
        setUnit(spinner, selectedUnit);
        ((EditText) root.findViewById(R.id.pantry_brand)).setText(state != null ? state.getString("brand") : item == null ? "" : item.getBrand());
        ((EditText) root.findViewById(R.id.pantry_quantity)).setText(state != null ? state.getString("quantity")
                : item == null || item.getQuantity() == null ? "" : Double.toString(item.getQuantity()));
        String expiry = state != null ? state.getString("expiry") : item == null || item.getExpirationDate() == null ? null : item.getExpirationDate().toString();
        expirationDate = expiry == null ? null : LocalDate.parse(expiry);
        updateDate(root);
        boundIngredientId = selectedIngredientId;
        ingredient.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                selectedIngredientId = null;
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        ingredient.setOnItemClickListener((parent, row, position, id) -> {
            Ingredient chosen = (Ingredient) parent.getItemAtPosition(position);
            selectedIngredientId = chosen.getId(); setUnit(spinner, chosen.getDefaultUnitId());
            ((TextInputLayout) root.findViewById(R.id.pantry_ingredient_input)).setError(null);
        });
    }

    private void save() {
        View root = getView();
        if (root == null || !loaded) return;
        TextInputLayout ingredientInput = root.findViewById(R.id.pantry_ingredient_input);
        TextInputLayout quantityInput = root.findViewById(R.id.pantry_quantity_input);
        ingredientInput.setError(null); quantityInput.setError(null);
        if (selectedIngredientId == null) {
            ingredientInput.setError(getString(R.string.pantry_select_ingredient)); return;
        }
        Double quantity = null;
        String raw = ((EditText) root.findViewById(R.id.pantry_quantity)).getText().toString().trim();
        if (!raw.isEmpty()) {
            try { quantity = Double.parseDouble(raw.replace(',', '.')); }
            catch (NumberFormatException invalid) { quantityInput.setError(getString(R.string.pantry_quantity_invalid)); return; }
            if (!Double.isFinite(quantity) || quantity <= 0) { quantityInput.setError(getString(R.string.pantry_quantity_invalid)); return; }
        }
        String unitId = selectedUnit(root);
        if (quantity != null && unitId == null) {
            quantityInput.setError(getString(R.string.pantry_quantity_needs_unit)); return;
        }
        String brand = ((EditText) root.findViewById(R.id.pantry_brand)).getText().toString().trim();
        if (brand.isEmpty()) brand = null;
        PantryItem item = new PantryItem(itemId, PantryPreviewData.OWNER_ID, selectedIngredientId, brand, quantity, unitId, expirationDate);
        viewModel.saveItem(item);
        getParentFragmentManager().popBackStack();
    }

    private void delete() {
        if (getView() == null || !loaded) return;
        viewModel.removeItem(itemId);
        getParentFragmentManager().popBackStack();
    }

    private void updateDate(View root) {
        ((MaterialButton) root.findViewById(R.id.pantry_date)).setText(expirationDate == null
                ? getString(R.string.pantry_choose_date) : expirationDate.toString());
        root.findViewById(R.id.pantry_clear_date).setVisibility(expirationDate == null ? View.GONE : View.VISIBLE);
    }

    private String selectedUnit(View root) {
        int position = ((Spinner) root.findViewById(R.id.pantry_unit)).getSelectedItemPosition();
        return position <= 0 || position > units.size() ? null : units.get(position - 1).getId();
    }

    private void setUnit(Spinner spinner, String unitId) {
        spinner.setSelection(0);
        for (int i = 0; i < units.size(); i++) if (units.get(i).getId().equals(unitId)) spinner.setSelection(i + 1);
    }

    private void error(View root, int text) {
        TextView error = root.findViewById(R.id.pantry_editor_error);
        error.setText(text); error.setVisibility(View.VISIBLE);
    }

    @Override public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        View root = getView();
        out.putString("draft_id", itemId);
        if (root == null || !loaded) return;
        out.putBoolean("draft_ready", true);
        out.putString("ingredient_id", selectedIngredientId);
        out.putString("ingredient_text", ((EditText) root.findViewById(R.id.pantry_ingredient)).getText().toString());
        out.putString("unit_id", selectedUnit(root));
        out.putString("brand", ((EditText) root.findViewById(R.id.pantry_brand)).getText().toString());
        out.putString("quantity", ((EditText) root.findViewById(R.id.pantry_quantity)).getText().toString());
        out.putString("expiry", expirationDate == null ? null : expirationDate.toString());
    }

    @Override public void onDestroyView() { loaded = false; super.onDestroyView(); }
}
