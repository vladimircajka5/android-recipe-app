package com.siit.recipeapp.ui.notifications;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class NotificationsPreviewViewModel extends ViewModel {

    // Order matches notification_preview_types in XML.
    public enum Type {
        ACCOUNT,
        EXPIRY,
        TIMER,
        REPORT,
        CATEGORY_PROPOSAL,
        SHOPPING_REMINDER
    }

    public static final int ALL = 0;
    public static final int UNREAD = 1;
    public static final int READ = 2;

    public static final class Entry {
        public final String id;
        public final Type type;
        public final String title;
        public final String message;
        public final long createdAt;
        public final boolean adminOnly;

        private boolean read;
        private long readAt;

        private Entry(
                String id,
                Type type,
                String title,
                String message,
                long createdAt,
                boolean adminOnly,
                boolean read) {

            this.id = id;
            this.type = type;
            this.title = title;
            this.message = message;
            this.createdAt = createdAt;
            this.adminOnly = adminOnly;
            this.read = read;
            this.readAt = read ? createdAt + 60_000L : 0L;
        }

        public boolean isRead() {
            return read;
        }

        public long getReadAt() {
            return readAt;
        }
    }

    private final List<Entry> entries = new ArrayList<>();
    private final MutableLiveData<List<Entry>> observable =
            new MutableLiveData<>();

    // Temporary UI state, retained with this owner-specific ViewModel.
    public int statusFilter = ALL;
    public int typeFilter = 0;

    public NotificationsPreviewViewModel() {
        long now = System.currentTimeMillis();
        long minute = 60_000L;
        long hour = 60L * minute;
        long day = 24L * hour;

        entries.add(new Entry(
                "preview-timer",
                Type.TIMER,
                "Cooking timer ended",
                "Sample event: the baking timer for vegetable lasagne ended. "
                        + "This preview did not start or run a timer.",
                now - 5L * minute,
                false,
                false));

        entries.add(new Entry(
                "preview-expiry",
                Type.EXPIRY,
                "Milk expires soon",
                "Sample event: milk in your pantry expires tomorrow. "
                        + "No pantry records were checked or changed.",
                now - 45L * minute,
                false,
                false));

        entries.add(new Entry(
                "preview-proposal",
                Type.CATEGORY_PROPOSAL,
                "Category proposal reviewed",
                "Sample event: your proposal for Plant-based drinks "
                        + "was accepted. No proposal or category was changed.",
                now - 3L * hour,
                false,
                true));

        entries.add(new Entry(
                "preview-account",
                Type.ACCOUNT,
                "Account notification",
                "Sample account notification used to demonstrate "
                        + "the notification details screen.",
                now - 20L * hour,
                false,
                true));

        entries.add(new Entry(
                "preview-shopping",
                Type.SHOPPING_REMINDER,
                "Shopping reminder",
                "Sample event: your weekend shopping list is ready "
                        + "for review. No reminder was scheduled.",
                now - 2L * day,
                false,
                false));

        entries.add(new Entry(
                "preview-report-result",
                Type.REPORT,
                "Your report was reviewed",
                "Sample event: an administrator reviewed your content "
                        + "report. No report was submitted or processed.",
                now - 3L * day,
                false,
                true));

        entries.add(new Entry(
                "preview-admin-report",
                Type.REPORT,
                "New content report",
                "Administrator sample: a content report is awaiting review. "
                        + "This entry is not connected to the reports feature.",
                now - 15L * minute,
                true,
                false));

        entries.add(new Entry(
                "preview-admin-proposal",
                Type.CATEGORY_PROPOSAL,
                "New category proposal",
                "Administrator sample: a proposal for Soy products "
                        + "is awaiting review. No real proposal was received.",
                now - 90L * minute,
                true,
                false));

        publish();
    }

    public static NotificationsPreviewViewModel obtain(
            FragmentActivity activity,
            String ownerId) {

        return new ViewModelProvider(activity).get(
                "notifications-preview:" + ownerId,
                NotificationsPreviewViewModel.class);
    }

    public LiveData<List<Entry>> observe() {
        return observable;
    }

    public void setRead(String id, boolean read) {
        for (Entry entry : entries) {
            if (entry.id.equals(id)) {
                if (entry.read != read) {
                    entry.read = read;
                    entry.readAt = read ? System.currentTimeMillis() : 0L;
                    publish();
                }
                return;
            }
        }
    }

    private void publish() {
        // Changing read status must not change chronological ordering.
        entries.sort(
                Comparator.comparingLong((Entry entry) -> entry.createdAt)
                        .reversed()
                        .thenComparing(entry -> entry.id));

        observable.setValue(new ArrayList<>(entries));
    }
}