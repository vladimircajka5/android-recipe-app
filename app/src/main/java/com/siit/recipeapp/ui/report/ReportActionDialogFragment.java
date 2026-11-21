package com.siit.recipeapp.ui.report;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.siit.recipeapp.R;

/** Demonstrates the action dialogs without submitting or modifying reports. */
public final class ReportActionDialogFragment extends DialogFragment {
    static final String SUBMIT = "submit";
    static final String WITHDRAW = "withdraw";
    static final String ACCEPT = "accept";
    static final String REJECT = "reject";
    static final String REMOVE = "remove";
    private static final String TAG = "report_action";
    private static final String ARG_ACTION = "action";
    private static final String STATE_REASON = "reason";
    private static final String STATE_ERROR = "reason_error";
    private TextInputEditText rejectionReason;
    private TextInputLayout reasonLayout;

    static void showPreview(FragmentManager manager, String action) {
        if (manager.isStateSaved() || manager.findFragmentByTag(TAG) != null) {
            return;
        }
        ReportActionDialogFragment dialog = new ReportActionDialogFragment();
        Bundle arguments = new Bundle();
        arguments.putString(ARG_ACTION, action);
        dialog.setArguments(arguments);
        dialog.showNow(manager, TAG);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle state) {
        String action = requireArguments().getString(ARG_ACTION);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(titleFor(action))
                .setMessage(R.string.report_preview_action_message)
                .setNegativeButton(R.string.report_cancel, null);
        if (REJECT.equals(action)) {
            View content = getLayoutInflater().inflate(R.layout.dialog_report_rejection, null);
            rejectionReason = content.findViewById(R.id.report_rejection_reason);
            reasonLayout = content.findViewById(R.id.report_rejection_reason_layout);
            if (state != null) {
                rejectionReason.setText(state.getString(STATE_REASON));
                if (state.getBoolean(STATE_ERROR)) {
                    reasonLayout.setError(getString(R.string.report_rejection_required));
                }
            }
            builder.setView(content);
        }
        return builder.setPositiveButton(R.string.report_preview_action, null).create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) requireDialog();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            if (rejectionReason != null && (rejectionReason.getText() == null
                    || rejectionReason.getText().toString().trim().isEmpty())) {
                reasonLayout.setError(getString(R.string.report_rejection_required));
                rejectionReason.requestFocus();
                return;
            }
            Toast.makeText(requireContext(), R.string.report_preview_unchanged,
                    Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }

    private int titleFor(String action) {
        if (WITHDRAW.equals(action)) return R.string.report_withdraw;
        if (ACCEPT.equals(action)) return R.string.report_accept;
        if (REJECT.equals(action)) return R.string.report_reject;
        if (REMOVE.equals(action)) return R.string.report_remove;
        return R.string.report_submit;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (rejectionReason != null && rejectionReason.getText() != null) {
            outState.putString(STATE_REASON, rejectionReason.getText().toString());
            outState.putBoolean(STATE_ERROR, reasonLayout.getError() != null);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        rejectionReason = null;
        reasonLayout = null;
        super.onDestroyView();
    }
}
