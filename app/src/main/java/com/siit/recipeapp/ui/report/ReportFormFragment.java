package com.siit.recipeapp.ui.report;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.siit.recipeapp.R;
import com.siit.recipeapp.ui.preview.ReportPreviewData;
import com.siit.recipeapp.ui.preview.ReportPreviewData.Target;

import java.util.ArrayList;
import java.util.List;

public final class ReportFormFragment extends Fragment {
    public ReportFormFragment() {
        super(R.layout.fragment_report_form);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle state) {
        MaterialToolbar toolbar = root.findViewById(R.id.report_form_toolbar);
        toolbar.setNavigationOnClickListener(view -> getParentFragmentManager().popBackStack());
        root.findViewById(R.id.report_cancel).setOnClickListener(view ->
                getParentFragmentManager().popBackStack());
        setupContentPicker(root);
        root.findViewById(R.id.report_submit).setOnClickListener(view -> previewSubmission(root));
    }

    private void setupContentPicker(View root) {
        List<String> titles = new ArrayList<>();
        for (Target target : ReportPreviewData.TARGETS) {
            titles.add(getString(target.title));
        }
        Spinner picker = root.findViewById(R.id.report_target);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, titles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        picker.setAdapter(adapter);
        picker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                showContent(root, ReportPreviewData.TARGETS.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void showContent(View root, Target target) {
        ((TextView) root.findViewById(R.id.report_target_type)).setText(target.type);
        ((TextView) root.findViewById(R.id.report_target_content)).setText(target.content);
        ImageView photo = root.findViewById(R.id.report_target_photo);
        photo.setVisibility(target.image == 0 ? View.GONE : View.VISIBLE);
        if (target.image != 0) {
            photo.setImageResource(target.image);
        }
    }

    private void previewSubmission(View root) {
        TextInputLayout reasonLayout = root.findViewById(R.id.report_reason_layout);
        TextInputEditText reason = root.findViewById(R.id.report_reason);
        if (reason.getText() == null || reason.getText().toString().trim().isEmpty()) {
            reasonLayout.setError(getString(R.string.report_reason_required));
            reason.requestFocus();
            return;
        }
        reasonLayout.setError(null);
        ReportActionDialogFragment.showPreview(getChildFragmentManager(),
                ReportActionDialogFragment.SUBMIT);
    }
}
