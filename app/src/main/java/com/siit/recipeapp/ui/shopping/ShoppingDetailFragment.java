package com.siit.recipeapp.ui.shopping;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.siit.recipeapp.R;
import com.siit.recipeapp.data.model.shopping.ShoppingList;
import com.siit.recipeapp.data.model.shopping.ShoppingListItem;
import com.siit.recipeapp.data.model.shopping.ShoppingListStatus;
import java.util.ArrayList;
import java.util.List;

public final class ShoppingDetailFragment extends Fragment {
    private final List<ShoppingListItem> items = new ArrayList<>();
    private final ItemAdapter adapter = new ItemAdapter();
    private ShoppingViewModel viewModel;
    private String listId;

    public ShoppingDetailFragment() { super(R.layout.fragment_shopping_detail); }

    @Override public void onViewCreated(@NonNull View root, @Nullable Bundle state) {
        listId = requireArguments().getString(ShoppingScreenHelper.LIST_ID);
        viewModel = new ViewModelProvider(requireActivity(), new ShoppingViewModelFactory(requireContext())).get(ShoppingViewModel.class);
        ListView list = root.findViewById(R.id.shopping_items);
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, row, position, id) -> {
            ShoppingList current = viewModel.findList(listId);
            if (current != null && current.getStatus() == ShoppingListStatus.ACTIVE) {
                ShoppingScreenHelper.open(this, new ShoppingItemEditorFragment(), listId, items.get(position).getId());
            }
        });
        root.findViewById(R.id.shopping_add_item).setOnClickListener(v ->
                ShoppingScreenHelper.open(this, new ShoppingItemEditorFragment(), listId, null));
        root.findViewById(R.id.shopping_edit_list).setOnClickListener(v ->
                ShoppingScreenHelper.open(this, new ShoppingListEditorFragment(), listId, null));
        root.findViewById(R.id.shopping_mark_all).setOnClickListener(v -> {
            viewModel.markAllPurchased(listId);
            refresh(root);
        });
        root.findViewById(R.id.shopping_back).setOnClickListener(v -> getParentFragmentManager().popBackStack());
        root.findViewById(R.id.shopping_archive_list).setOnClickListener(v -> {
            viewModel.archiveList(listId);
            refresh(root);
        });
        root.findViewById(R.id.shopping_reactivate_list).setOnClickListener(v -> {
            viewModel.reactivateList(listId);
            refresh(root);
        });
        root.findViewById(R.id.shopping_copy_list).setOnClickListener(v -> {
            ShoppingList copy = viewModel.copyList(listId);
            if (copy != null) {
                ShoppingScreenHelper.open(this, new ShoppingDetailFragment(), copy.getId(), null);
            }
        });
        root.findViewById(R.id.shopping_delete_list).setOnClickListener(v ->
                new MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.shopping_delete_list)
                        .setMessage(R.string.shopping_delete_list_message)
                        .setNegativeButton(R.string.shopping_cancel, null)
                        .setPositiveButton(R.string.shopping_delete, (dialog, which) -> {
                            viewModel.removeList(listId);
                            getParentFragmentManager().popBackStack();
                        }).show());

    }

    @Override public void onResume() {
        super.onResume();
        refresh(requireView());
    }

    private void refresh(View root) {
        ShoppingList list = viewModel.findList(listId);
        boolean exists = list != null;
        boolean archived = exists && list.getStatus() == ShoppingListStatus.ARCHIVED;
        for (int id : new int[]{R.id.shopping_add_item, R.id.shopping_edit_list, R.id.shopping_delete_list}) {
            root.findViewById(id).setEnabled(exists && !archived);
        }
        root.findViewById(R.id.shopping_add_item).setVisibility(archived ? View.GONE : View.VISIBLE);
        root.findViewById(R.id.shopping_edit_list).setVisibility(archived ? View.GONE : View.VISIBLE);
        root.findViewById(R.id.shopping_mark_all).setVisibility(archived ? View.GONE : View.VISIBLE);
        root.findViewById(R.id.shopping_delete_list).setVisibility(archived ? View.GONE : View.VISIBLE);
        root.findViewById(R.id.shopping_archive_list).setVisibility(exists && !archived ? View.VISIBLE : View.GONE);
        root.findViewById(R.id.shopping_reactivate_list).setVisibility(archived ? View.VISIBLE : View.GONE);
        root.findViewById(R.id.shopping_copy_list).setVisibility(archived ? View.VISIBLE : View.GONE);
        ((TextView) root.findViewById(R.id.shopping_detail_title)).setText(exists ? list.getName() : getString(R.string.shopping_missing_list));
        items.clear();
        if (exists) items.addAll(viewModel.itemsFor(listId));
        adapter.notifyDataSetChanged();
        int purchased = 0;
        for (ShoppingListItem item : items) if (item.isPurchased()) purchased++;
        ((TextView) root.findViewById(R.id.shopping_detail_progress)).setText(getString(R.string.shopping_progress, purchased, items.size()));
        root.findViewById(R.id.shopping_mark_all).setEnabled(exists && !archived && purchased < items.size());
        root.findViewById(R.id.shopping_items).setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
        TextView empty = root.findViewById(R.id.shopping_items_empty);
        empty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        empty.setText(exists ? R.string.shopping_items_empty : R.string.shopping_missing_list);
    }

    private final class ItemAdapter extends BaseAdapter {
        @Override public int getCount() { return items.size(); }
        @Override public Object getItem(int position) { return items.get(position); }
        @Override public long getItemId(int position) { return position; }
        @Override public View getView(int position, View recycled, ViewGroup parent) {
            View row = recycled == null ? LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_shopping_ingredient, parent, false) : recycled;
            ShoppingListItem item = items.get(position);
            CheckBox purchased = row.findViewById(R.id.shopping_purchased);
            purchased.setOnCheckedChangeListener(null);
            purchased.setText(ShoppingScreenHelper.ingredientName(item.getIngredientId()));
            purchased.setChecked(item.isPurchased());
            ShoppingList current = viewModel.findList(listId);
            purchased.setEnabled(current != null && current.getStatus() == ShoppingListStatus.ACTIVE);
            purchased.setOnCheckedChangeListener((button, checked) -> {
                viewModel.setPurchased(item.getId(), checked);
                refresh(requireView());
            });
            ((TextView) row.findViewById(R.id.shopping_item_amount)).setText(ShoppingScreenHelper.amount(item));
            return row;
        }
    }
}
