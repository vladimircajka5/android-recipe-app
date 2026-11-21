package com.siit.recipeapp.ui.proposals;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.siit.recipeapp.R;
import com.siit.recipeapp.data.model.account.UserRole;
import com.siit.recipeapp.data.remote.UserDataSession;
import com.siit.recipeapp.data.repository.AuthRepository;
import com.siit.recipeapp.data.repository.firebase.FirebaseAuthRepository;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CategoryProposalsFragment extends Fragment {

    private CategoryProposalPreviewViewModel model;
    private final List<CategoryProposalPreviewViewModel.Proposal> visible =
            new ArrayList<>();

    private Spinner scope;
    private Spinner filter;
    private ArrayAdapter<String> adapter;
    private AlertDialog activeDialog;

    private String ownerId;
    private boolean admin;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_category_proposals, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        admin = false;
        ownerId = UserDataSession.currentOwnerId(requireContext());

        scope = view.findViewById(R.id.spinner_proposal_scope);
        filter = view.findViewById(R.id.spinner_proposal_status);

        ArrayAdapter<String> scopes = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<>()
        );

        scopes.add(getString(R.string.proposal_scope_my));
        scopes.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        scope.setAdapter(scopes);

        ArrayAdapter<CharSequence> filters = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.proposal_filters,
                android.R.layout.simple_spinner_item
        );

        filters.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        filter.setAdapter(filters);

        int restoredScope = savedInstanceState == null
                ? 0 : savedInstanceState.getInt("proposal_scope", 0);

        if (savedInstanceState != null) {
            filter.setSelection(Math.max(0, Math.min(
                    4, savedInstanceState.getInt("proposal_filter", 0))));
        }

        adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                new ArrayList<String>()
        );

        ListView list = view.findViewById(R.id.list_proposals);
        list.setAdapter(adapter);

        AdapterView.OnItemSelectedListener selectionListener =
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View selected,
                            int position,
                            long id) {
                        render();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                };

        scope.setOnItemSelectedListener(selectionListener);
        filter.setOnItemSelectedListener(selectionListener);

        list.setOnItemClickListener((parent, selected, position, id) -> {
            if (activeUser() && position < visible.size()) {
                showDetails(visible.get(position));
            }
        });

        view.findViewById(R.id.btn_proposal_new)
                .setOnClickListener(v -> {
                    if (!activeUser()) {
                        return;
                    }

                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(
                                    R.id.fragment_container,
                                    new CategoryProposalFormFragment())
                            .addToBackStack(null)
                            .commit();
                });

        if (ownerId == null) {
            view.findViewById(R.id.btn_proposal_new).setEnabled(false);
            scope.setEnabled(false);
            filter.setEnabled(false);

            ((TextView) view.findViewById(R.id.tv_proposals_access))
                    .setText(R.string.proposal_sign_in);
            return;
        }

        model = CategoryProposalPreviewViewModel.obtain(
                requireActivity(), ownerId);

        model.observe().observe(getViewLifecycleOwner(), entries -> render());

        AuthRepository auth = new FirebaseAuthRepository();

        auth.getCurrentRole(new AuthRepository.RoleCallback() {
            @Override
            public void onSuccess(UserRole role) {
                if (!isAdded() || getView() != view || !activeUser()) {
                    return;
                }

                admin = role == UserRole.ADMIN;

                if (admin) {
                    scopes.add(getString(R.string.proposal_scope_all));
                    scope.setSelection(restoredScope == 1 ? 1 : 0);
                }

                ((TextView) view.findViewById(R.id.tv_proposals_access))
                        .setText(admin
                                ? R.string.proposal_admin_view
                                : R.string.proposal_user_view);

                render();
            }

            @Override
            public void onError(String message) {
                if (!isAdded() || getView() != view) {
                    return;
                }

                ((TextView) view.findViewById(R.id.tv_proposals_access))
                        .setText(R.string.proposal_role_error);
            }
        });
    }

    private boolean activeUser() {
        return isAdded()
                && ownerId != null
                && ownerId.equals(
                UserDataSession.currentOwnerId(requireContext()));
    }

    private void render() {
        if (getView() == null || model == null || adapter == null) {
            return;
        }

        visible.clear();
        adapter.clear();

        boolean showAll = admin && scope.getSelectedItemPosition() == 1;
        int selectedStatus = filter.getSelectedItemPosition() - 1;

        List<CategoryProposalPreviewViewModel.Proposal> entries =
                model.observe().getValue();

        if (entries != null) {
            for (CategoryProposalPreviewViewModel.Proposal entry : entries) {
                if (!showAll && !entry.own) {
                    continue;
                }

                if (selectedStatus >= 0 && entry.status != selectedStatus) {
                    continue;
                }

                visible.add(entry);

                adapter.add(
                        entry.name + " • " + statusName(entry.status)
                                + "\n" + entry.element
                                + " — " + authorName(entry)
                );
            }
        }

        getView().findViewById(R.id.tv_proposals_empty)
                .setVisibility(visible.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private String statusName(int status) {
        return getResources().getStringArray(
                R.array.proposal_statuses)[status];
    }

    private String authorName(
            CategoryProposalPreviewViewModel.Proposal entry) {

        return getString(entry.own
                ? R.string.proposal_you
                : R.string.proposal_sample_member);
    }

    private String provided(String value) {
        return value == null || value.isEmpty()
                ? getString(R.string.proposal_none)
                : value;
    }

    private void showDetails(
            CategoryProposalPreviewViewModel.Proposal entry) {

        if (!isResumed() || getParentFragmentManager().isStateSaved()
                || (activeDialog != null && activeDialog.isShowing())) {
            return;
        }

        View content = getLayoutInflater().inflate(
                R.layout.fragment_category_proposal_details, null);

        String date = DateFormat.getDateTimeInstance(
                        DateFormat.SHORT, DateFormat.SHORT)
                .format(new Date(entry.createdAt));

        ((TextView) content.findViewById(R.id.tv_proposal_details))
                .setText(getString(
                        R.string.proposal_details_body,
                        entry.element,
                        entry.name,
                        provided(entry.explanation),
                        authorName(entry),
                        date,
                        statusName(entry.status),
                        provided(entry.response),
                        provided(entry.alternative)
                ));

        boolean review = admin
                && entry.status == CategoryProposalPreviewViewModel.PENDING;

        content.findViewById(R.id.layout_proposal_review)
                .setVisibility(review ? View.VISIBLE : View.GONE);

        Spinner decision = content.findViewById(
                R.id.spinner_proposal_decision);

        TextInputLayout alternativeLayout = content.findViewById(
                R.id.til_proposal_alternative);

        decision.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View selected,
                            int position,
                            long id) {

                        alternativeLayout.setError(null);
                        alternativeLayout.setVisibility(
                                position == 2 ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

        MaterialAlertDialogBuilder builder =
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.proposal_details_title)
                        .setView(content)
                        .setNegativeButton(review
                                ? R.string.proposal_cancel
                                : R.string.proposal_close, null);

        if (review) {
            builder.setPositiveButton(R.string.proposal_apply_preview, null);
        }

        AlertDialog dialog = builder.create();
        activeDialog = dialog;

        dialog.setOnDismissListener(ignored -> {
            if (activeDialog == dialog) {
                activeDialog = null;
            }
        });

        if (review) {
            dialog.setOnShowListener(ignored ->
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setOnClickListener(button -> {
                                if (!activeUser() || !admin) {
                                    return;
                                }

                                int status =
                                        decision.getSelectedItemPosition() + 1;

                                String alternative = text(
                                        content.findViewById(
                                                R.id.et_proposal_alternative));

                                String response = text(
                                        content.findViewById(
                                                R.id.et_proposal_response));

                                if (status ==
                                        CategoryProposalPreviewViewModel.ALTERNATIVE
                                        && alternative.isEmpty()) {

                                    alternativeLayout.setError(getString(
                                            R.string.proposal_alternative_required));
                                    return;
                                }

                                boolean changed = model.applyPreviewDecision(
                                        entry.id, status, response, alternative);

                                Toast.makeText(
                                        requireContext(),
                                        changed
                                                ? R.string.proposal_preview_updated
                                                : R.string.proposal_changed,
                                        Toast.LENGTH_LONG
                                ).show();

                                dialog.dismiss();
                            }));
        }

        dialog.show();
    }

    private String text(TextView field) {
        return field.getText() == null
                ? "" : field.getText().toString().trim();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (scope != null && filter != null) {
            outState.putInt(
                    "proposal_scope", scope.getSelectedItemPosition());
            outState.putInt(
                    "proposal_filter", filter.getSelectedItemPosition());
        }
    }

    @Override
    public void onDestroyView() {
        if (activeDialog != null) {
            activeDialog.dismiss();
            activeDialog = null;
        }

        adapter = null;
        scope = null;
        filter = null;
        model = null;
        visible.clear();
        admin = false;

        super.onDestroyView();
    }
}