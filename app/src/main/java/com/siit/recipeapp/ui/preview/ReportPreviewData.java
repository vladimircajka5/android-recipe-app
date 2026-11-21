package com.siit.recipeapp.ui.preview;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.siit.recipeapp.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Display fixtures for the reporting screens; no reports are stored or changed. */
public final class ReportPreviewData {
    private ReportPreviewData() {}

    public enum Status {
        NEW(R.string.report_status_new),
        ACCEPTED(R.string.report_status_accepted),
        REJECTED(R.string.report_status_rejected);

        @StringRes public final int label;

        Status(@StringRes int label) {
            this.label = label;
        }
    }

    public static final class Target {
        @StringRes public final int type;
        @StringRes public final int title;
        @StringRes public final int content;
        @DrawableRes public final int image;

        private Target(int type, int title, int content, int image) {
            this.type = type;
            this.title = title;
            this.content = content;
            this.image = image;
        }
    }

    public static final class Report {
        public final String id;
        public final int targetIndex;
        public final boolean belongsToCurrentUser;
        public final Status status;
        @StringRes public final int reporter;
        @StringRes public final int reason;
        @StringRes public final int submittedAt;
        @StringRes public final int decisionReason;
        @StringRes public final int resolvedAt;

        private Report(String id, int targetIndex, boolean belongsToCurrentUser,
                       Status status, int reporter, int reason, int submittedAt,
                       int decisionReason, int resolvedAt) {
            this.id = id;
            this.targetIndex = targetIndex;
            this.belongsToCurrentUser = belongsToCurrentUser;
            this.status = status;
            this.reporter = reporter;
            this.reason = reason;
            this.submittedAt = submittedAt;
            this.decisionReason = decisionReason;
            this.resolvedAt = resolvedAt;
        }
    }

    public static final List<Target> TARGETS = Collections.unmodifiableList(Arrays.asList(
            new Target(R.string.report_type_recipe, R.string.report_sample_recipe,
                    R.string.report_sample_recipe_content, 0),
            new Target(R.string.report_type_comment, R.string.report_sample_comment,
                    R.string.report_sample_comment_content, 0),
            new Target(R.string.report_type_photo, R.string.report_sample_photo,
                    R.string.report_sample_photo_content, R.drawable.recipe_sample_pancakes)
    ));

    public static final List<Report> REPORTS = Collections.unmodifiableList(Arrays.asList(
            new Report("preview-report-recipe", 0, true, Status.NEW,
                    R.string.report_sample_self, R.string.report_sample_reason_recipe,
                    R.string.report_sample_date_new, 0, 0),
            new Report("preview-report-comment", 1, true, Status.ACCEPTED,
                    R.string.report_sample_self, R.string.report_sample_reason_comment,
                    R.string.report_sample_date_old, R.string.report_sample_accepted,
                    R.string.report_sample_date_resolved),
            new Report("preview-report-photo", 2, true, Status.REJECTED,
                    R.string.report_sample_self, R.string.report_sample_reason_photo,
                    R.string.report_sample_date_old, R.string.report_sample_rejected,
                    R.string.report_sample_date_resolved),
            new Report("preview-report-other-user", 1, false, Status.NEW,
                    R.string.report_sample_other_user, R.string.report_sample_reason_comment,
                    R.string.report_sample_date_new, 0, 0)
    ));

    public static Report findReport(String id) {
        for (Report report : REPORTS) {
            if (report.id.equals(id)) {
                return report;
            }
        }
        return null;
    }
}
