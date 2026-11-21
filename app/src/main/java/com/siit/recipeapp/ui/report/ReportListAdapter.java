package com.siit.recipeapp.ui.report;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.siit.recipeapp.R;
import com.siit.recipeapp.ui.preview.ReportPreviewData;
import com.siit.recipeapp.ui.preview.ReportPreviewData.Report;
import com.siit.recipeapp.ui.preview.ReportPreviewData.Target;

import java.util.ArrayList;
import java.util.List;

final class ReportListAdapter extends RecyclerView.Adapter<ReportListAdapter.ReportHolder> {
    interface OnReportClickListener {
        void onReportClick(Report report);
    }

    private final List<Report> reports = new ArrayList<>();
    private final OnReportClickListener listener;

    ReportListAdapter(OnReportClickListener listener) {
        this.listener = listener;
    }

    void showReports(List<Report> visibleReports) {
        int previousCount = reports.size();
        reports.clear();
        notifyItemRangeRemoved(0, previousCount);
        reports.addAll(visibleReports);
        notifyItemRangeInserted(0, reports.size());
    }

    @NonNull
    @Override
    public ReportHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ReportHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportHolder holder, int position) {
        Report report = reports.get(position);
        Target target = ReportPreviewData.TARGETS.get(report.targetIndex);
        holder.title.setText(target.title);
        holder.type.setText(target.type);
        holder.status.setText(report.status.label);
        holder.reason.setText(report.reason);
        holder.date.setText(report.submittedAt);
        holder.itemView.setOnClickListener(view -> listener.onReportClick(report));
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    static final class ReportHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView type;
        final TextView status;
        final TextView reason;
        final TextView date;

        ReportHolder(View row) {
            super(row);
            title = row.findViewById(R.id.report_row_title);
            type = row.findViewById(R.id.report_row_type);
            status = row.findViewById(R.id.report_row_status);
            reason = row.findViewById(R.id.report_row_reason);
            date = row.findViewById(R.id.report_row_date);
        }
    }
}
