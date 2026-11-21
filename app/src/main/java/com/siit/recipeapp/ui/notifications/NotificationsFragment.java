package com.siit.recipeapp.ui.notifications;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.siit.recipeapp.R;
import com.siit.recipeapp.data.model.account.UserRole;
import com.siit.recipeapp.data.remote.UserDataSession;
import com.siit.recipeapp.data.repository.AuthRepository;
import com.siit.recipeapp.data.repository.firebase.FirebaseAuthRepository;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private final AuthRepository auth = new FirebaseAuthRepository();

    private final List<NotificationsPreviewViewModel.Entry> visible =
            new ArrayList<>();

    private NotificationsPreviewViewModel model;
    private NotificationAdapter adapter;

    private ListView list;
    private View controls;
    private Spinner statusFilter;
    private Spinner typeFilter;
    private AlertDialog activeDialog;

    private String ownerId;
    private boolean admin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,@Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        admin = false;
        ownerId = UserDataSession.currentOwnerId(requireContext());

        list = view.findViewById(R.id.list_notifications);
        controls = getLayoutInflater().inflate(
                R.layout.notifications_controls, list, false);

        // Filters scroll with the list, including in landscape.
        list.addHeaderView(controls, null, false);

        statusFilter = controls.findViewById(
                R.id.spinner_notification_status);
        typeFilter = controls.findViewById(
                R.id.spinner_notification_type);

        configureSpinner(
                statusFilter, R.array.notification_preview_statuses);
        configureSpinner(
                typeFilter, R.array.notification_preview_types);

        adapter = new NotificationAdapter();
        list.setAdapter(adapter);

        list.setOnItemClickListener((parent, selected, position, id) -> {
            int entryPosition = position - list.getHeaderViewsCount();

            if (activeUser() && entryPosition >= 0 && entryPosition < visible.size()) {
                showDetails(visible.get(entryPosition));
            }
        });

        if (!activeUser()) {
            render();
            return;
        }

        model = NotificationsPreviewViewModel.obtain(requireActivity(), ownerId);

        statusFilter.setSelection(model.statusFilter);
        typeFilter.setSelection(model.typeFilter);

        AdapterView.OnItemSelectedListener listener =
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,View selected,int position,long id) {
                        if (model == null) {
                            return;
                        }
                        model.statusFilter = Math.max(0, statusFilter.getSelectedItemPosition());
                        model.typeFilter = Math.max(0, typeFilter.getSelectedItemPosition());
                        render();
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                };

        statusFilter.setOnItemSelectedListener(listener);
        typeFilter.setOnItemSelectedListener(listener);

        model.observe().observe(
                getViewLifecycleOwner(), entries -> render());

        auth.getCurrentRole(new AuthRepository.RoleCallback() {
            @Override
            public void onSuccess(UserRole role) {
                if (!isAdded() || getView() != view) {
                    return;
                }

                if (!activeUser()) {
                    render();
                    return;
                }

                admin = role == UserRole.ADMIN;

                ((TextView) controls.findViewById(
                        R.id.tv_notifications_access))
                        .setText(admin
                                ? R.string.notification_preview_admin
                                : R.string.notification_preview_user);
                render();
            }

            @Override
            public void onError(String message) {
                if (!isAdded() || getView() != view) {
                    return;
                }
                // Keep administrator samples hidden if role lookup fails.
                admin = false;

                ((TextView) controls.findViewById(
                        R.id.tv_notifications_access))
                        .setText(R.string.notification_preview_role_error);

                render();
            }
        });
    }

    private void configureSpinner(Spinner spinner, int arrayResource) {
        ArrayAdapter<CharSequence> choices =
                ArrayAdapter.createFromResource(
                        requireContext(),
                        arrayResource,
                        android.R.layout.simple_spinner_item);

        choices.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(choices);
    }

    private boolean activeUser() {
        return isAdded()
                && ownerId != null
                && auth.hasVerifiedSession()
                && ownerId.equals(
                UserDataSession.currentOwnerId(requireContext()));
    }

    @Override
    public void onResume() {
        super.onResume();
        render();
    }

    private void render() {
        if (controls == null || adapter == null) {
            return;
        }

        visible.clear();

        boolean allowed = activeUser();
        statusFilter.setEnabled(allowed);
        typeFilter.setEnabled(allowed);

        TextView empty = controls.findViewById(
                R.id.tv_notifications_empty);
        TextView summary = controls.findViewById(
                R.id.tv_notifications_summary);

        if (!allowed) {
            admin = false;

            ((TextView) controls.findViewById(
                    R.id.tv_notifications_access))
                    .setText(R.string.notification_preview_sign_in);

            summary.setText("");
            empty.setText(R.string.notification_preview_sign_in);
            empty.setVisibility(View.VISIBLE);

            if (activeDialog != null) {
                activeDialog.dismiss();
            }

            adapter.notifyDataSetChanged();
            return;
        }

        int unread = 0;

        List<NotificationsPreviewViewModel.Entry> entries =
                model == null ? null : model.observe().getValue();

        if (entries != null) {
            for (NotificationsPreviewViewModel.Entry entry : entries) {
                if (entry.adminOnly && !admin) {
                    continue;
                }

                if (!entry.isRead()) {
                    unread++;
                }

                if (model.statusFilter
                        == NotificationsPreviewViewModel.UNREAD
                        && entry.isRead()) {
                    continue;
                }

                if (model.statusFilter
                        == NotificationsPreviewViewModel.READ
                        && !entry.isRead()) {
                    continue;
                }

                if (model.typeFilter > 0
                        && entry.type.ordinal() != model.typeFilter - 1) {
                    continue;
                }

                visible.add(entry);
            }
        }

        summary.setText(getString(
                R.string.notification_preview_summary,
                visible.size(),
                unread));

        empty.setText(R.string.notification_preview_empty);
        empty.setVisibility(visible.isEmpty() ? View.VISIBLE : View.GONE);

        adapter.notifyDataSetChanged();
    }

    private String typeName(NotificationsPreviewViewModel.Type type) {
        return getResources().getStringArray(
                R.array.notification_preview_types)[type.ordinal() + 1];
    }

    private String date(long timestamp) {
        return DateFormat.getDateTimeInstance(
                        DateFormat.MEDIUM, DateFormat.SHORT)
                .format(new Date(timestamp));
    }

    private void showDetails(NotificationsPreviewViewModel.Entry entry) {
        if (!isResumed()
                || getParentFragmentManager().isStateSaved()
                || (activeDialog != null && activeDialog.isShowing())) {
            return;
        }

        String body = getString(
                R.string.notification_preview_details,
                typeName(entry.type),
                date(entry.createdAt),
                entry.message,
                getString(entry.isRead()
                        ? R.string.notification_preview_read
                        : R.string.notification_preview_unread),
                entry.getReadAt() == 0L
                        ? getString(R.string.notification_preview_not_read)
                        : date(entry.getReadAt()),
                getString(R.string.notification_preview_notice));

        AlertDialog dialog =
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(entry.title)
                        .setMessage(body)
                        .setNegativeButton(
                                R.string.notification_preview_close, null)
                        .setPositiveButton(
                                entry.isRead()
                                        ? R.string.notification_preview_mark_unread
                                        : R.string.notification_preview_mark_read,
                                (ignored, which) -> {
                                    if (model == null || !activeUser()) {
                                        render();
                                        return;
                                    }

                                    model.setRead(entry.id, !entry.isRead());

                                    Toast.makeText(
                                            requireContext(),
                                            R.string.notification_preview_updated,
                                            Toast.LENGTH_SHORT
                                    ).show();
                                })
                        .create();

        activeDialog = dialog;

        dialog.setOnDismissListener(ignored -> {
            if (activeDialog == dialog) {
                activeDialog = null;
            }
        });

        dialog.show();
    }

    private final class NotificationAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return visible.size();
        }

        @Override
        public NotificationsPreviewViewModel.Entry getItem(int position) {
            return visible.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(
                int position,
                View convertView,
                ViewGroup parent) {

            View row = convertView;

            if (row == null) {
                row = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_notification_preview,
                        parent,
                        false);
            }

            NotificationsPreviewViewModel.Entry entry = getItem(position);

            TextView title = row.findViewById(
                    R.id.tv_notification_title);
            TextView state = row.findViewById(
                    R.id.tv_notification_state);

            title.setText(entry.title);
            title.setTypeface(
                    null, entry.isRead() ? Typeface.NORMAL : Typeface.BOLD);

            state.setText(entry.isRead()
                    ? R.string.notification_preview_read
                    : R.string.notification_preview_unread);

            state.setTextColor(ContextCompat.getColor(
                    parent.getContext(),
                    entry.isRead()
                            ? R.color.app_text_secondary
                            : R.color.app_primary));

            ((TextView) row.findViewById(R.id.tv_notification_type))
                    .setText(typeName(entry.type));

            ((TextView) row.findViewById(R.id.tv_notification_message))
                    .setText(entry.message);

            ((TextView) row.findViewById(R.id.tv_notification_date))
                    .setText(date(entry.createdAt));

            return row;
        }
    }

    @Override
    public void onDestroyView() {
        if (activeDialog != null) {
            activeDialog.dismiss();
            activeDialog = null;
        }

        if (list != null) {
            list.setAdapter(null);
        }

        visible.clear();
        adapter = null;
        model = null;
        list = null;
        controls = null;
        statusFilter = null;
        typeFilter = null;
        ownerId = null;
        admin = false;

        super.onDestroyView();
    }
}