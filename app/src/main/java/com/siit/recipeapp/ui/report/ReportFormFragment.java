package com.siit.recipeapp.ui.report;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
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
    private static final String ARG_RECIPE_TITLE = "recipe_title";
    private static final String ARG_RECIPE_DESCRIPTION = "recipe_description";
    private static final String ARG_RECIPE_PHOTO = "recipe_photo";

    public ReportFormFragment() {
        super(R.layout.fragment_report_form);
    }

    public static ReportFormFragment newForRecipe(@StringRes int title,
                                                  @StringRes int description,
                                                  @DrawableRes int photo) {
        ReportFormFragment fragment = new ReportFormFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARG_RECIPE_TITLE, title);
        arguments.putInt(ARG_RECIPE_DESCRIPTION, description);
        arguments.putInt(ARG_RECIPE_PHOTO, photo);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle state) {
        MaterialToolbar toolbar = root.findViewById(R.id.report_form_toolbar);
        toolbar.setNavigationOnClickListener(view -> getParentFragmentManager().popBackStack());
        root.findViewById(R.id.report_cancel).setOnClickListener(view ->
                getParentFragmentManager().popBackStack());
        setupTarget(root);
        root.findViewById(R.id.report_submit).setOnClickListener(view -> previewSubmission(root));
    }

    private void setupTarget(View root) {
        Bundle arguments = getArguments();
        if (arguments == null || !arguments.containsKey(ARG_RECIPE_TITLE)) {
            setupContentPicker(root);
            return;
        }

        root.findViewById(R.id.report_target).setVisibility(View.GONE);
        TextView title = root.findViewById(R.id.report_target_title);
        title.setText(arguments.getInt(ARG_RECIPE_TITLE));
        title.setVisibility(View.VISIBLE);
        showContent(root, R.string.report_type_recipe, arguments.getInt(ARG_RECIPE_DESCRIPTION),
                arguments.getInt(ARG_RECIPE_PHOTO), R.string.recipe_detail_photo_description);
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
                Target target = ReportPreviewData.TARGETS.get(position);
                showContent(root, target.type, target.content, target.image,
                        R.string.report_photo_description);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void showContent(View root, @StringRes int type, @StringRes int content,
                             @DrawableRes int image, @StringRes int photoDescription) {
        ((TextView) root.findViewById(R.id.report_target_type)).setText(type);
        ((TextView) root.findViewById(R.id.report_target_content)).setText(content);
        ImageView photo = root.findViewById(R.id.report_target_photo);
        photo.setVisibility(image == 0 ? View.GONE : View.VISIBLE);
        if (image != 0) {
            photo.setImageResource(image);
            photo.setContentDescription(getString(photoDescription));
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
