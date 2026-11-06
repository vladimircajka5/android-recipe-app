package com.siit.recipeapp.ui.pantry;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.siit.recipeapp.R;
import com.siit.recipeapp.ui.preview.PantryPreviewData;
import com.siit.recipeapp.ui.preview.PantryPreviewViewModel;
import com.siit.recipeapp.data.model.ingredient.Ingredient;
import com.siit.recipeapp.data.model.ingredient.MeasurementUnit;
import com.siit.recipeapp.data.model.pantry.PantryItem;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class PantryFragment extends Fragment {
    private final List<PantryItem> items = new ArrayList<>();
    private final Map<String, Ingredient> ingredients = new HashMap<>();
    private final Map<String, MeasurementUnit> units = new HashMap<>();
    private final PantryAdapter adapter = new PantryAdapter();
    private PantryPreviewViewModel preview;

    public PantryFragment() { super(R.layout.fragment_pantry); }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        ListView list = view.findViewById(R.id.pantry_list);
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, row, position, id) -> openEditor(adapter.visible.get(position).getId()));
        view.findViewById(R.id.pantry_add).setOnClickListener(v -> openEditor(null));
        EditText filter = view.findViewById(R.id.pantry_filter);
        filter.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateVisible(); }
            @Override public void afterTextChanged(Editable s) { }
        });
        ((TextView) view.findViewById(R.id.pantry_notice)).setText(R.string.pantry_development_notice);
        ingredients.clear();
        for (Ingredient ingredient : PantryPreviewData.INGREDIENTS) ingredients.put(ingredient.getId(), ingredient);
        units.clear();
        for (MeasurementUnit unit : PantryPreviewData.UNITS) units.put(unit.getId(), unit);
        preview = new ViewModelProvider(requireActivity()).get(PantryPreviewViewModel.class);
    }

    @Override public void onResume() {
        super.onResume();
        items.clear();
        items.addAll(preview.getItems());
        updateVisible();
    }

    private void updateVisible() {
        View root = getView();
        if (root == null) return;
        String query = ((EditText) root.findViewById(R.id.pantry_filter)).getText().toString().trim().toLowerCase(Locale.ROOT);
        adapter.visible.clear();
        for (PantryItem item : items) {
            Ingredient ingredient = ingredients.get(item.getIngredientId());
            String searchable = (ingredient == null ? "" : ingredient.getName()) + " "
                    + (item.getBrand() == null ? "" : item.getBrand());
            if (searchable.toLowerCase(Locale.ROOT).contains(query)) adapter.visible.add(item);
        }
        adapter.notifyDataSetChanged();
        boolean empty = adapter.visible.isEmpty();
        root.findViewById(R.id.pantry_list).setVisibility(empty ? View.GONE : View.VISIBLE);
        root.findViewById(R.id.pantry_message).setVisibility(empty ? View.VISIBLE : View.GONE);
        if (empty) showMessage(root, items.isEmpty() ? R.string.pantry_empty : R.string.pantry_no_matches);
    }

    private void showMessage(View root, int message) {
        TextView text = root.findViewById(R.id.pantry_message);
        text.setText(message); text.setVisibility(View.VISIBLE);
    }

    private void openEditor(String id) {
        PantryEditorFragment editor = new PantryEditorFragment();
        Bundle args = new Bundle(); args.putString(PantryEditorFragment.ARG_ITEM_ID, id);
        editor.setArguments(args);
        getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, editor)
                .addToBackStack("pantry-editor").commit();
    }


    private final class PantryAdapter extends BaseAdapter {
        private final List<PantryItem> visible = new ArrayList<>();
        @Override public int getCount() { return visible.size(); }
        @Override public Object getItem(int position) { return visible.get(position); }
        @Override public long getItemId(int position) { return position; }
        @Override public View getView(int position, View recycled, ViewGroup parent) {
            View row = recycled == null ? LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false) : recycled;
            PantryItem item = visible.get(position);
            Ingredient ingredient = ingredients.get(item.getIngredientId());
            TextView name = row.findViewById(R.id.pantry_item_name);
            name.setText(ingredient == null ? item.getIngredientId() : ingredient.getName());
            TextView brand = row.findViewById(R.id.pantry_item_brand);
            brand.setVisibility(item.getBrand() == null ? View.GONE : View.VISIBLE);
            brand.setText(item.getBrand() == null ? "" : getString(R.string.pantry_brand_value, item.getBrand()));
            MeasurementUnit unit = units.get(item.getUnitId());
            String amount = item.getQuantity() == null ? getString(R.string.pantry_quantity_unknown)
                    : Double.toString(item.getQuantity()) + " " + (unit == null ? "" : unit.getAbbreviation());
            ((TextView) row.findViewById(R.id.pantry_item_quantity)).setText(amount);
            TextView expiry = row.findViewById(R.id.pantry_item_expiry);
            expiry.setVisibility(item.getExpirationDate() == null ? View.GONE : View.VISIBLE);
            if (item.getExpirationDate() != null) expiry.setText(getString(
                    item.getExpirationDate().isBefore(LocalDate.now()) ? R.string.pantry_expired_value : R.string.pantry_expiry_value,
                    item.getExpirationDate().toString()));
            return row;
        }
    }
}
