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
    private String saveDraftId;
    private boolean editing;
    private boolean loaded;
    private String currentProductId() {
        if (viewModel == null) return null;
        for (PantryItem item : viewModel.getItems()) if (itemId.equals(item.getId())) return item.getProductId();
        return null;
    }

    public PantryEditorFragment() { super(R.layout.fragment_pantry_editor); }

    @Override public void onViewCreated(@NonNull View root, @Nullable Bundle state) {
        viewModel = new ViewModelProvider(requireActivity(), new PantryViewModelFactory(requireContext())).get(PantryViewModel.class);

        String existingId = getArguments() == null ? null : getArguments().getString(ARG_ITEM_ID);

        editing = existingId != null;

        itemId = state != null ? state.getString("draft_id") : existingId;
        if (itemId == null) itemId = UUID.randomUUID().toString();

        saveDraftId = state == null ? null : state.getString("save_draft_id");
        if (saveDraftId == null) {
            saveDraftId = UUID.randomUUID().toString();
        }
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

        ingredients.clear(); ingredients.addAll(viewModel.getIngredients());

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

        viewModel.observeSaveResult().observe(
                getViewLifecycleOwner(),
                result -> renderSaveResult(root, result)
        );
    }

    @Override
    public void onResume() {
        super.onResume();

        View root = getView();
        if (root != null && loaded) {
            renderSaveResult(root, viewModel.observeSaveResult().getValue());
        }
    }

    private void renderSaveResult(View root, PantryViewModel.SaveResult result) {
        boolean saving = result != null
                && result.status == PantryViewModel.SaveStatus.SAVING;

        root.findViewById(R.id.pantry_save).setEnabled(!saving);
        root.findViewById(R.id.pantry_delete).setEnabled(!saving);
        root.findViewById(R.id.pantry_cancel).setEnabled(!saving);

        if (result == null || saving || !saveDraftId.equals(result.draftId)) {
            return;
        }

        if (result.status == PantryViewModel.SaveStatus.FAILED) {
            viewModel.clearSaveResult(saveDraftId);
            error(root, R.string.pantry_save_failed);
            return;
        }

        if (isResumed() && !getParentFragmentManager().isStateSaved()) {
            viewModel.clearSaveResult(saveDraftId);
            getParentFragmentManager().popBackStack();
        }
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
        MaterialAutoCompleteTextView ingredientField = root.findViewById(R.id.pantry_ingredient);
        Spinner unitSpinner = root.findViewById(R.id.pantry_unit);

        ingredientField.setAdapter(new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, ingredients));
        setupUnitSpinner(unitSpinner);

        if (state != null && state.getBoolean("draft_ready")) {
            restoreDraft(root, ingredientField, unitSpinner, state);
        } else {
            showItem(root, ingredientField, unitSpinner, item);
        }

        boolean canChangeIngredient = item == null || item.getProductId() == null;
        ingredientField.setEnabled(canChangeIngredient);
        unitSpinner.setEnabled(canChangeIngredient);

        updateDate(root);
        boundIngredientId = selectedIngredientId;
        listenForIngredientChanges(root, ingredientField, unitSpinner);
    }

    private void setupUnitSpinner(Spinner spinner) {
        List<String> labels = new ArrayList<>();
        labels.add(getString(R.string.pantry_no_unit));
        for (MeasurementUnit unit : units) {
            labels.add(unit.toString());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void restoreDraft(
            View root, MaterialAutoCompleteTextView ingredientField,
            Spinner unitSpinner, Bundle draft) {
        selectedIngredientId = draft.getString("ingredient_id");
        ingredientField.setText(draft.getString("ingredient_text", ""), false);
        setUnit(unitSpinner, draft.getString("unit_id"));

        EditText brandField = root.findViewById(R.id.pantry_brand);
        EditText quantityField = root.findViewById(R.id.pantry_quantity);
        brandField.setText(draft.getString("brand"));
        quantityField.setText(draft.getString("quantity"));

        String savedExpiry = draft.getString("expiry");
        expirationDate = savedExpiry == null ? null : LocalDate.parse(savedExpiry);
    }

    private void showItem(
            View root, MaterialAutoCompleteTextView ingredientField,
            Spinner unitSpinner, PantryItem item) {
        EditText brandField = root.findViewById(R.id.pantry_brand);
        EditText quantityField = root.findViewById(R.id.pantry_quantity);

        selectedIngredientId = item == null ? null : item.getIngredientId();
        for (Ingredient ingredient : ingredients) {
            if (ingredient.getId().equals(selectedIngredientId)) {
                ingredientField.setText(ingredient.getName(), false);
                break;
            }
        }

        setUnit(unitSpinner, item == null ? null : item.getUnitId());
        brandField.setText(item == null ? "" : item.getBrand());

        Double quantity = item == null ? null : item.getQuantity();
        quantityField.setText(quantity == null ? "" : Double.toString(quantity));
        expirationDate = item == null ? null : item.getExpirationDate();
    }

    private void listenForIngredientChanges(
            View root, MaterialAutoCompleteTextView ingredientField, Spinner unitSpinner) {
        ingredientField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                selectedIngredientId = null;
            }

            @Override
            public void afterTextChanged(Editable text) { }
        });

        ingredientField.setOnItemClickListener((parent, row, position, id) -> {
            Ingredient chosen = (Ingredient) parent.getItemAtPosition(position);
            selectedIngredientId = chosen.getId();
            setUnit(unitSpinner, chosen.getDefaultUnitId());

            TextInputLayout input = root.findViewById(R.id.pantry_ingredient_input);
            input.setError(null);
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
        PantryItem item = new PantryItem(itemId, PantryPreviewData.OWNER_ID, selectedIngredientId, editing ? currentProductId() : null, brand, quantity, unitId, expirationDate);
        root.findViewById(R.id.pantry_editor_error).setVisibility(View.GONE);
        viewModel.saveManualItem(saveDraftId, item);
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
        out.putString("save_draft_id", saveDraftId);
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
