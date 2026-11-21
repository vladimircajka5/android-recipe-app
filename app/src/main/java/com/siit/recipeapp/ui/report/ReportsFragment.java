package com.siit.recipeapp.ui.report;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.siit.recipeapp.R;
import com.siit.recipeapp.ui.preview.ReportPreviewData;
import com.siit.recipeapp.ui.preview.ReportPreviewData.Report;
import com.siit.recipeapp.ui.preview.ReportPreviewData.Status;

import java.util.ArrayList;
import java.util.List;

public final class ReportsFragment extends Fragment {
    private static final String ARG_ADMIN = "admin";
    private static final String STATE_FILTER = "filter";
    private int selectedFilter;

    public ReportsFragment() {
        super(R.layout.fragment_reports);
    }

    public static ReportsFragment newInstance(boolean admin) {
        ReportsFragment fragment = new ReportsFragment();
        Bundle arguments = new Bundle();
        arguments.putBoolean(ARG_ADMIN, admin);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle state) {
        boolean admin = requireArguments().getBoolean(ARG_ADMIN);
        selectedFilter = state == null ? 0 : state.getInt(STATE_FILTER);
        setupToolbar(root, admin);
        setupList(root, admin);
    }

    private void setupToolbar(View root, boolean admin) {
        MaterialToolbar toolbar = root.findViewById(R.id.reports_toolbar);
        toolbar.setTitle(admin ? R.string.reports_admin_title : R.string.reports_my_title);
        toolbar.setNavigationOnClickListener(view -> getParentFragmentManager().popBackStack());
        View newReport = root.findViewById(R.id.reports_new);
        newReport.setVisibility(admin ? View.GONE : View.VISIBLE);
        newReport.setOnClickListener(view -> open(new ReportFormFragment()));
    }

    private void setupList(View root, boolean admin) {
        RecyclerView list = root.findViewById(R.id.reports_list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        ReportListAdapter adapter = new ReportListAdapter(report ->
                open(ReportDetailFragment.newInstance(report.id, admin)));
        list.setAdapter(adapter);

        Spinner filter = root.findViewById(R.id.reports_filter);
        ArrayAdapter<CharSequence> filters = ArrayAdapter.createFromResource(requireContext(),
                admin ? R.array.report_admin_filters : R.array.report_user_filters,
                android.R.layout.simple_spinner_item);
        filters.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filter.setAdapter(filters);
        filter.setSelection(selectedFilter);
        filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedFilter = position;
                showReports(root, adapter, admin);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        showReports(root, adapter, admin);
    }

    private void showReports(View root, ReportListAdapter adapter, boolean admin) {
        List<Report> visibleReports = new ArrayList<>();
        for (Report report : ReportPreviewData.REPORTS) {
            if (isVisible(report, admin)) {
                visibleReports.add(report);
            }
        }
        adapter.showReports(visibleReports);
        root.findViewById(R.id.reports_empty).setVisibility(
                visibleReports.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private boolean isVisible(Report report, boolean admin) {
        if (admin) {
            return selectedFilter == 0
                    ? report.status == Status.NEW : report.status != Status.NEW;
        }
        return report.belongsToCurrentUser
                && (selectedFilter == 0 || report.status.ordinal() == selectedFilter - 1);
    }

    private void open(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(STATE_FILTER, selectedFilter);
        super.onSaveInstanceState(outState);
    }
}
