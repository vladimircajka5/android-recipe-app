package com.siit.recipeapp.ui.shopping;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.siit.recipeapp.R;
import com.siit.recipeapp.ui.shopping.ShoppingViewModel;
import com.siit.recipeapp.data.model.shopping.ShoppingList;
import com.siit.recipeapp.data.model.shopping.ShoppingListItem;
import com.siit.recipeapp.data.model.shopping.ShoppingListStatus;
import java.util.ArrayList;
import java.util.List;

public final class ShoppingListsFragment extends Fragment {
    private final List<ShoppingList> lists = new ArrayList<>();
    private final ShoppingAdapter adapter = new ShoppingAdapter();
    private ShoppingViewModel viewModel;
    private boolean showingArchived;

    public ShoppingListsFragment() { super(R.layout.fragment_shopping_lists); }

    @Override public void onViewCreated(@NonNull View root, @Nullable Bundle state) {
        viewModel = new ViewModelProvider(requireActivity(), new ShoppingViewModelFactory(requireContext())).get(ShoppingViewModel.class);
        ListView list = root.findViewById(R.id.shopping_lists);
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, row, position, id) ->
                ShoppingScreenHelper.open(this, new ShoppingDetailFragment(), lists.get(position).getId(), null));
        root.findViewById(R.id.shopping_new_list).setOnClickListener(v ->
                ShoppingScreenHelper.open(this, new ShoppingListEditorFragment(), null, null));
        root.findViewById(R.id.shopping_toggle_archived).setOnClickListener(v -> {
            showingArchived = !showingArchived;
            refresh();
        });
    }

    @Override public void onResume() {
        super.onResume();
        refresh();
    }

    void showActiveLists() {
        showingArchived = false;
        if (getView() != null) refresh();
    }

    private void refresh() {
        View root = requireView();
        lists.clear();
        ShoppingListStatus visibleStatus = showingArchived ? ShoppingListStatus.ARCHIVED : ShoppingListStatus.ACTIVE;
        for (ShoppingList list : viewModel.getLists()) {
            if (list.getStatus() == visibleStatus) lists.add(list);
        }
        adapter.notifyDataSetChanged();
        ((TextView) root.findViewById(R.id.shopping_empty)).setText(
                showingArchived ? R.string.shopping_archive_empty : R.string.shopping_empty);
        ((com.google.android.material.button.MaterialButton) root.findViewById(R.id.shopping_toggle_archived))
                .setText(showingArchived ? R.string.shopping_show_active : R.string.shopping_show_archived);
        root.findViewById(R.id.shopping_empty).setVisibility(lists.isEmpty() ? View.VISIBLE : View.GONE);
        root.findViewById(R.id.shopping_lists).setVisibility(lists.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private final class ShoppingAdapter extends BaseAdapter {
        @Override public int getCount() { return lists.size(); }
        @Override public Object getItem(int position) { return lists.get(position); }
        @Override public long getItemId(int position) { return position; }
        @Override public View getView(int position, View recycled, ViewGroup parent) {
            View row = recycled == null ? LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_shopping_list, parent, false) : recycled;
            ShoppingList list = lists.get(position);
            ((TextView) row.findViewById(R.id.shopping_list_name)).setText(list.getName());
            ((TextView) row.findViewById(R.id.shopping_list_created)).setText(
                    getString(R.string.shopping_created_value, ShoppingScreenHelper.date(list.getCreatedAt())));
            List<ShoppingListItem> entries = viewModel.itemsFor(list.getId());
            int purchased = 0;
            for (ShoppingListItem item : entries) if (item.isPurchased()) purchased++;
            ((TextView) row.findViewById(R.id.shopping_list_progress)).setText(
                    getString(R.string.shopping_progress, purchased, entries.size()));
            TextView planned = row.findViewById(R.id.shopping_list_planned);
            planned.setVisibility(list.getPlannedPurchaseAt() == null ? View.GONE : View.VISIBLE);
            if (list.getPlannedPurchaseAt() != null) planned.setText(getString(R.string.shopping_planned_value,
                    ShoppingScreenHelper.date(list.getPlannedPurchaseAt())));
            return row;
        }
    }
}
