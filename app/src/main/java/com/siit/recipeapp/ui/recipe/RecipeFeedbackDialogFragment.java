package com.siit.recipeapp.ui.recipe;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.siit.recipeapp.R;

public final class RecipeFeedbackDialogFragment extends DialogFragment {
    private static final String TAG = "recipe_feedback";
    private static final String ARG_RATING = "rating";

    static void showRating(FragmentManager manager) {
        showForm(manager, true);
    }

    static void showComment(FragmentManager manager) {
        showForm(manager, false);
    }

    private static void showForm(FragmentManager manager, boolean rating) {
        if (manager.isStateSaved() || manager.findFragmentByTag(TAG) != null) {
            return;
        }
        RecipeFeedbackDialogFragment fragment = new RecipeFeedbackDialogFragment();
        Bundle arguments = new Bundle();
        arguments.putBoolean(ARG_RATING, rating);
        fragment.setArguments(arguments);
        fragment.showNow(manager, TAG);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle state) {
        boolean rating = requireArguments().getBoolean(ARG_RATING);
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(rating ? R.string.recipe_rating_action : R.string.recipe_comment_action)
                .setView(rating ? R.layout.dialog_recipe_rating : R.layout.dialog_recipe_comment)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(rating ? R.string.recipe_rating_submit : R.string.recipe_comment_post, null)
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) requireDialog();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }
}
