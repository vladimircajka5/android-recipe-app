package com.siit.recipeapp.ui.shopping;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.siit.recipeapp.R;
import com.siit.recipeapp.ui.preview.ShoppingPreviewData;
import com.siit.recipeapp.ui.shopping.ShoppingViewModel;
import com.siit.recipeapp.data.model.shopping.ShoppingList;
import com.siit.recipeapp.data.model.shopping.ShoppingListStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public final class ShoppingListEditorFragment extends Fragment {
    private ShoppingViewModel viewModel;
    private String listId;
    private boolean editing;
    private LocalDateTime planned;
    private Dialog dateDialog;
    private Dialog timeDialog;

    public ShoppingListEditorFragment() { super(R.layout.fragment_shopping_list_editor); }

    @Override public void onViewCreated(@NonNull View root, @Nullable Bundle state) {
        viewModel = new ViewModelProvider(requireActivity(), new ShoppingViewModelFactory()).get(ShoppingViewModel.class);
        String existingId = requireArguments().getString(ShoppingScreenHelper.LIST_ID);
        editing = existingId != null;
        listId = state == null ? existingId : state.getString("draft_id", existingId);
        if (listId == null) listId = UUID.randomUUID().toString();
        ShoppingList current = editing ? viewModel.findList(listId) : null;
        ((TextView) root.findViewById(R.id.shopping_editor_title)).setText(editing ? R.string.shopping_edit_list : R.string.shopping_new_list);
        root.findViewById(R.id.shopping_cancel).setOnClickListener(v -> getParentFragmentManager().popBackStack());
        if (editing && current == null) {
            ((TextView) root.findViewById(R.id.shopping_editor_error)).setText(R.string.shopping_missing_list);
            root.findViewById(R.id.shopping_editor_error).setVisibility(View.VISIBLE);
            root.findViewById(R.id.shopping_save).setEnabled(false);
            root.findViewById(R.id.shopping_date).setEnabled(false);
            return;
        }
        ((EditText) root.findViewById(R.id.shopping_name)).setText(current == null ? "" : current.getName());
        ((CheckBox) root.findViewById(R.id.shopping_permanent)).setChecked(current != null && current.isPermanent());
        planned = state == null ? current == null ? null : current.getPlannedPurchaseAt()
                : state.getString("planned") == null ? null : LocalDateTime.parse(state.getString("planned"));
        updateDate(root);
        root.findViewById(R.id.shopping_date).setOnClickListener(v -> {
            LocalDateTime initial = planned == null ? LocalDateTime.now().withSecond(0).withNano(0) : planned;
            dateDialog = new DatePickerDialog(requireContext(), (picker, year, month, day) -> {
                    timeDialog = new TimePickerDialog(requireContext(), (time, hour, minute) -> {
                        planned = LocalDateTime.of(year, month + 1, day, hour, minute);
                        updateDate(root);
                    }, initial.getHour(), initial.getMinute(), android.text.format.DateFormat.is24HourFormat(requireContext()));
                    timeDialog.show();
                }, initial.getYear(), initial.getMonthValue() - 1, initial.getDayOfMonth());
            dateDialog.show();
        });
        root.findViewById(R.id.shopping_clear_date).setOnClickListener(v -> { planned = null; updateDate(root); });
        root.findViewById(R.id.shopping_save).setOnClickListener(v -> {
            String name = ((EditText) root.findViewById(R.id.shopping_name)).getText().toString().trim();
            TextInputLayout input = root.findViewById(R.id.shopping_name_input);
            input.setError(null);
            if (name.isEmpty()) { input.setError(getString(R.string.shopping_name_required)); return; }
            ShoppingList existing = editing ? viewModel.findList(listId) : null;
            if (editing && existing == null) return;
            viewModel.saveList(new ShoppingList(listId, existing == null ? ShoppingPreviewData.OWNER_ID : existing.getOwnerId(),
                    name, existing == null ? LocalDateTime.now() : existing.getCreatedAt(), planned,
                    ((CheckBox) root.findViewById(R.id.shopping_permanent)).isChecked(),
                    existing == null ? ShoppingListStatus.ACTIVE : existing.getStatus()));
            viewModel.archiveCompletedListIfNeeded(listId);
            getParentFragmentManager().popBackStack();
        });
    }

    private void updateDate(View root) {
        ((MaterialButton) root.findViewById(R.id.shopping_date)).setText(planned == null
                ? getString(R.string.shopping_choose_date) : ShoppingScreenHelper.date(planned));
        root.findViewById(R.id.shopping_clear_date).setVisibility(planned == null ? View.GONE : View.VISIBLE);
    }

    @Override public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        out.putString("draft_id", listId);
        out.putString("planned", planned == null ? null : planned.toString());
    }

    @Override public void onDestroyView() {
        if (dateDialog != null) dateDialog.dismiss();
        if (timeDialog != null) timeDialog.dismiss();
        dateDialog = null;
        timeDialog = null;
        super.onDestroyView();
    }
}
