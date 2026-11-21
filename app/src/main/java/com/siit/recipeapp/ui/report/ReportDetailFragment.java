package com.siit.recipeapp.ui.report;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.siit.recipeapp.R;
import com.siit.recipeapp.ui.preview.ReportPreviewData;
import com.siit.recipeapp.ui.preview.ReportPreviewData.Report;
import com.siit.recipeapp.ui.preview.ReportPreviewData.Status;
import com.siit.recipeapp.ui.preview.ReportPreviewData.Target;

public final class ReportDetailFragment extends Fragment {
    private static final String ARG_REPORT_ID = "report_id";
    private static final String ARG_ADMIN = "admin";

    public ReportDetailFragment() {
        super(R.layout.fragment_report_detail);
    }

    public static ReportDetailFragment newInstance(String reportId, boolean admin) {
        ReportDetailFragment fragment = new ReportDetailFragment();
        Bundle arguments = new Bundle();
        arguments.putString(ARG_REPORT_ID, reportId);
        arguments.putBoolean(ARG_ADMIN, admin);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle state) {
        MaterialToolbar toolbar = root.findViewById(R.id.report_detail_toolbar);
        toolbar.setNavigationOnClickListener(view -> getParentFragmentManager().popBackStack());
        Report report = ReportPreviewData.findReport(
                requireArguments().getString(ARG_REPORT_ID));
        if (report == null) {
            throw new IllegalArgumentException("Unknown preview report");
        }
        bindReport(root, report);
        bindActions(root, report, requireArguments().getBoolean(ARG_ADMIN));
    }

    private void bindReport(View root, Report report) {
        Target target = ReportPreviewData.TARGETS.get(report.targetIndex);
        setText(root, R.id.report_detail_title, target.title);
        setText(root, R.id.report_detail_type, target.type);
        setText(root, R.id.report_detail_content, target.content);
        setText(root, R.id.report_detail_status, report.status.label);
        setText(root, R.id.report_detail_reporter, report.reporter);
        setText(root, R.id.report_detail_date, report.submittedAt);
        setText(root, R.id.report_detail_reason, report.reason);

        ImageView photo = root.findViewById(R.id.report_detail_photo);
        photo.setVisibility(target.image == 0 ? View.GONE : View.VISIBLE);
        if (target.image != 0) {
            photo.setImageResource(target.image);
        }
        root.findViewById(R.id.report_decision_section).setVisibility(
                report.status == Status.NEW ? View.GONE : View.VISIBLE);
        if (report.status != Status.NEW) {
            setText(root, R.id.report_detail_decision, report.decisionReason);
            setText(root, R.id.report_detail_resolved, report.resolvedAt);
        }
    }

    private void bindActions(View root, Report report, boolean admin) {
        boolean undecided = report.status == Status.NEW;
        bindAction(root, R.id.report_withdraw, !admin && undecided,
                ReportActionDialogFragment.WITHDRAW);
        bindAction(root, R.id.report_accept, admin && undecided,
                ReportActionDialogFragment.ACCEPT);
        bindAction(root, R.id.report_reject, admin && undecided,
                ReportActionDialogFragment.REJECT);
        bindAction(root, R.id.report_remove, admin && report.status == Status.ACCEPTED,
                ReportActionDialogFragment.REMOVE);
    }

    private void bindAction(View root, int viewId, boolean visible, String action) {
        View button = root.findViewById(viewId);
        button.setVisibility(visible ? View.VISIBLE : View.GONE);
        button.setOnClickListener(view ->
                ReportActionDialogFragment.showPreview(getChildFragmentManager(), action));
    }

    private static void setText(View root, int viewId, int textId) {
        ((TextView) root.findViewById(viewId)).setText(textId);
    }
}
