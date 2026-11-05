package com.siit.recipeapp.ui.shopping;

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.siit.recipeapp.R;
import com.siit.recipeapp.ui.preview.ShoppingPreviewViewModel;
import com.siit.recipeapp.data.model.ingredient.Ingredient;
import com.siit.recipeapp.data.model.ingredient.MeasurementUnit;
import com.siit.recipeapp.data.model.shopping.ShoppingListItem;
import com.siit.recipeapp.ui.preview.IngredientPreviewCatalog;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ShoppingItemEditorFragment extends Fragment {

    private final List<Ingredient> ingredients = new ArrayList<>();
    private final List<MeasurementUnit> units = new ArrayList<>();
    private String selectedIngredientId;
    private String boundIngredientId;
    private ShoppingPreviewViewModel preview;
    private String listId;
    private String itemId;
    private boolean editing;
    private boolean loaded;

    public ShoppingItemEditorFragment() { super(R.layout.fragment_shopping_item_editor); }

    @Override public void onViewCreated(@NonNull View root, @Nullable Bundle state) {
        preview = new ViewModelProvider(requireActivity()).get(ShoppingPreviewViewModel.class);
        listId = requireArguments().getString(ShoppingScreenHelper.LIST_ID);
        String existingId = requireArguments().getString(ShoppingScreenHelper.ITEM_ID);
        editing = existingId != null;
        itemId = state != null ? state.getString("draft_id") : existingId;
        if (itemId == null) itemId = UUID.randomUUID().toString();
        ((TextView) root.findViewById(R.id.shopping_item_editor_title)).setText(editing ? R.string.shopping_item_edit_title : R.string.shopping_item_add_title);
        root.findViewById(R.id.shopping_item_cancel).setOnClickListener(v -> getParentFragmentManager().popBackStack());
        root.findViewById(R.id.shopping_item_save).setOnClickListener(v -> save());
        root.findViewById(R.id.shopping_item_delete).setVisibility(editing ? View.VISIBLE : View.GONE);
        root.findViewById(R.id.shopping_item_delete).setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.shopping_item_delete_title).setMessage(R.string.shopping_item_delete_message)
                .setNegativeButton(R.string.shopping_item_cancel, null).setPositiveButton(R.string.shopping_item_delete, (dialog, which) -> delete()).show());
        ingredients.clear(); ingredients.addAll(IngredientPreviewCatalog.INGREDIENTS);
        units.clear(); units.addAll(IngredientPreviewCatalog.UNITS);
        ShoppingListItem current = null;
        if (editing) {
            for (ShoppingListItem item : preview.itemsFor(listId)) {
                if (itemId.equals(item.getId())) current = item;
            }
        }
        if (preview.findList(listId) == null || (editing && (current == null || !listId.equals(current.getShoppingListId())))) {
            error(root, R.string.shopping_item_missing_item);
            root.findViewById(R.id.shopping_item_save).setEnabled(false);
            root.findViewById(R.id.shopping_item_delete).setEnabled(false);
            return;
        }
        bind(root, current, state);
        loaded = true;
    }

    @Override public void onViewStateRestored(@Nullable Bundle state) {
        super.onViewStateRestored(state);
        // Restoring the text field can trigger its watcher; restore only a valid selection.
        if (!loaded) return;
        EditText field = requireView().findViewById(R.id.shopping_item_ingredient);
        for (Ingredient ingredient : ingredients) {
            if (ingredient.getId().equals(boundIngredientId)
                    && ingredient.getName().contentEquals(field.getText())) {
                selectedIngredientId = boundIngredientId;
            }
        }
    }

    private void bind(View root, ShoppingListItem item, Bundle state) {
        MaterialAutoCompleteTextView ingredient = root.findViewById(R.id.shopping_item_ingredient);
        ingredient.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, ingredients));
        List<String> labels = new ArrayList<>(); labels.add(getString(R.string.shopping_item_no_unit));
        for (MeasurementUnit unit : units) labels.add(unit.toString());
        Spinner spinner = root.findViewById(R.id.shopping_item_unit);
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, labels);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(unitAdapter);
        if (state != null && !state.getBoolean("draft_ready")) state = null;
        selectedIngredientId = state != null ? state.getString("ingredient_id") : item == null ? null : item.getIngredientId();
        String selectedUnit = state != null ? state.getString("unit_id") : item == null ? null : item.getUnitId();
        for (Ingredient entry : ingredients) if (entry.getId().equals(selectedIngredientId)) ingredient.setText(entry.getName(), false);
        if (state != null) ingredient.setText(state.getString("ingredient_text", ""), false);
        setUnit(spinner, selectedUnit);
        ((EditText) root.findViewById(R.id.shopping_item_quantity)).setText(state != null ? state.getString("quantity")
                : item == null || item.getQuantity() == null ? "" : item.getQuantity().toPlainString());
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
            ((TextInputLayout) root.findViewById(R.id.shopping_item_ingredient_input)).setError(null);
        });
    }

    private void save() {
        View root = getView();
        if (root == null || !loaded) return;
        TextInputLayout ingredientInput = root.findViewById(R.id.shopping_item_ingredient_input);
        TextInputLayout quantityInput = root.findViewById(R.id.shopping_item_quantity_input);
        ingredientInput.setError(null); quantityInput.setError(null);
        if (selectedIngredientId == null) {
            ingredientInput.setError(getString(R.string.shopping_item_select_ingredient)); return;
        }
        BigDecimal quantity;
        String raw = ((EditText) root.findViewById(R.id.shopping_item_quantity)).getText().toString().trim();
        if (raw.isEmpty()) { quantityInput.setError(getString(R.string.shopping_item_quantity_invalid)); return; }
        try { quantity = new BigDecimal(raw.replace(',', '.')); }
        catch (NumberFormatException invalid) { quantityInput.setError(getString(R.string.shopping_item_quantity_invalid)); return; }
        if (quantity.signum() <= 0) { quantityInput.setError(getString(R.string.shopping_item_quantity_invalid)); return; }
        String unitId = selectedUnit(root);
        if (unitId == null) {
            quantityInput.setError(getString(R.string.shopping_item_quantity_needs_unit)); return;
        }
        if (preview.findList(listId) == null) { error(root, R.string.shopping_missing_list); return; }
        ShoppingListItem existing = editing ? preview.findItem(itemId) : null;
        if (editing && existing == null) { error(root, R.string.shopping_item_missing_item); return; }
        ShoppingListItem item = new ShoppingListItem(itemId, listId, selectedIngredientId, quantity, unitId,
                existing != null && existing.isPurchased());
        preview.saveItem(item);
        getParentFragmentManager().popBackStack();
    }

    private void delete() {
        if (getView() == null || !loaded) return;
        preview.removeItem(itemId);
        getParentFragmentManager().popBackStack();
    }

    private String selectedUnit(View root) {
        int position = ((Spinner) root.findViewById(R.id.shopping_item_unit)).getSelectedItemPosition();
        return position <= 0 || position > units.size() ? null : units.get(position - 1).getId();
    }

    private void setUnit(Spinner spinner, String unitId) {
        spinner.setSelection(0);
        for (int i = 0; i < units.size(); i++) if (units.get(i).getId().equals(unitId)) spinner.setSelection(i + 1);
    }

    private void error(View root, int text) {
        TextView error = root.findViewById(R.id.shopping_item_editor_error);
        error.setText(text); error.setVisibility(View.VISIBLE);
    }

    @Override public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        View root = getView();
        out.putString("draft_id", itemId);
        if (root == null || !loaded) return;
        out.putBoolean("draft_ready", true);
        out.putString("ingredient_id", selectedIngredientId);
        out.putString("ingredient_text", ((EditText) root.findViewById(R.id.shopping_item_ingredient)).getText().toString());
        out.putString("unit_id", selectedUnit(root));
        out.putString("quantity", ((EditText) root.findViewById(R.id.shopping_item_quantity)).getText().toString());
    }

    @Override public void onDestroyView() { loaded = false; super.onDestroyView(); }
}
