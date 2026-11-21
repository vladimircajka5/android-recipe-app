package com.siit.recipeapp.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.siit.recipeapp.R;
import com.siit.recipeapp.data.model.account.UserRole;
import com.siit.recipeapp.data.repository.AppRepositories;
import com.siit.recipeapp.data.repository.AuthRepository;
import com.siit.recipeapp.data.repository.CatalogRepository;
import com.siit.recipeapp.data.repository.firebase.FirebaseAuthRepository;
import com.siit.recipeapp.domain.service.CatalogService;

import java.util.UUID;

public class CatalogEditorFragment extends Fragment {

    private static final String ARG_TYPE = "catalog_type";
    private static final String ARG_ID = "catalog_id";
    private static final String ARG_NAME = "catalog_name";
    private static final String ARG_DESCRIPTION = "catalog_description";

    private int catalogType;
    private String entryId;
    private boolean editing;
    private boolean authorized;
    private boolean busy;
    private boolean completed;
    private int completionMessage;

    private CatalogRepository repository;
    private CatalogService service;
    private AlertDialog activeDialog;

    public static CatalogEditorFragment newInstance(
            int type, String id, String name, String description) {

        Bundle arguments = new Bundle();
        arguments.putInt(ARG_TYPE, type);
        arguments.putString(ARG_ID, id);
        arguments.putString(ARG_NAME, name);
        arguments.putString(ARG_DESCRIPTION, description);

        CatalogEditorFragment fragment = new CatalogEditorFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = requireArguments();
        catalogType = arguments.getInt(ARG_TYPE, -1);
        editing = arguments.getString(ARG_ID) != null;

        if (catalogType < 0 || catalogType > 4) {
            throw new IllegalArgumentException("Invalid catalog type.");
        }

        entryId = savedInstanceState == null
                ? arguments.getString(ARG_ID)
                : savedInstanceState.getString("entry_id");

        if (entryId == null) {
            entryId = UUID.randomUUID().toString();
        }

        completed = savedInstanceState != null
                && savedInstanceState.getBoolean("completed", false);

        completionMessage = savedInstanceState == null
                ? R.string.admin_catalog_saved
                : savedInstanceState.getInt(
                "completion_message",
                R.string.admin_catalog_saved);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_catalog_editor, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        repository = AppRepositories.createCatalogRepository();
        service = AppRepositories.createCatalogService(repository);

        authorized = false;
        busy = false;

        ((TextView) view.findViewById(R.id.tv_catalog_editor_title))
                .setText(editing
                        ? R.string.admin_catalog_edit_title
                        : R.string.admin_catalog_add_title);

        ((TextView) view.findViewById(R.id.tv_catalog_editor_type))
                .setText(getResources()
                        .getStringArray(R.array.admin_catalog_types)[catalogType]);

        TextInputEditText name = view.findViewById(R.id.et_catalog_name);
        TextInputEditText description =
                view.findViewById(R.id.et_catalog_description);

        Bundle arguments = requireArguments();

        name.setText(savedInstanceState == null
                ? arguments.getString(ARG_NAME, "")
                : savedInstanceState.getString(
                "draft_name", arguments.getString(ARG_NAME, "")));

        description.setText(savedInstanceState == null
                ? arguments.getString(ARG_DESCRIPTION, "")
                : savedInstanceState.getString(
                "draft_description",
                arguments.getString(ARG_DESCRIPTION, "")));

        view.findViewById(R.id.layout_catalog_delete)
                .setVisibility(editing ? View.VISIBLE : View.GONE);

        view.findViewById(R.id.btn_catalog_save)
                .setOnClickListener(v -> save(view));

        view.findViewById(R.id.btn_catalog_delete)
                .setOnClickListener(v -> confirmDelete(view));

        renderControls(view);

        if (completed) {
            showStatus(view, completionMessage);
            return;
        }

        busy = true;
        renderControls(view);
        showStatus(view, R.string.admin_catalog_checking);

        AuthRepository auth = new FirebaseAuthRepository();

        auth.getCurrentRole(new AuthRepository.RoleCallback() {
            @Override
            public void onSuccess(UserRole role) {
                if (!isCurrent(view)) {
                    return;
                }

                busy = false;
                authorized = role == UserRole.ADMIN;
                renderControls(view);

                if (authorized) {
                    clearStatus(view);
                } else {
                    showStatus(view, R.string.admin_catalog_denied);
                }
            }

            @Override
            public void onError(String message) {
                if (!isCurrent(view)) {
                    return;
                }

                busy = false;
                renderControls(view);
                showStatus(view, R.string.admin_catalog_role_error);
            }
        });
    }

    private boolean isCurrent(View view) {
        return isAdded() && getView() == view;
    }

    private void save(View view) {
        if (!authorized || busy || completed) {
            return;
        }

        TextInputEditText nameField =
                view.findViewById(R.id.et_catalog_name);

        TextInputEditText descriptionField =
                view.findViewById(R.id.et_catalog_description);

        TextInputLayout nameLayout =
                view.findViewById(R.id.til_catalog_name);

        String name = text(nameField);
        String description = text(descriptionField);

        nameLayout.setError(null);

        if (name.isEmpty()) {
            nameLayout.setError(
                    getString(R.string.admin_catalog_name_required));
            return;
        }

        busy = true;
        renderControls(view);
        showStatus(view, R.string.admin_catalog_saving);

        CatalogRepository.Callback<Void> callback =
                operationCallback(view, false);

        switch (catalogType) {
            case 0:
                service.saveIngredientCategory(
                        entryId, name, description, callback);
                break;
            case 1:
                service.saveAllergen(
                        entryId, name, description, callback);
                break;
            case 2:
                service.saveMealType(
                        entryId, name, description, callback);
                break;
            case 3:
                service.saveDishType(
                        entryId, name, description, callback);
                break;
            case 4:
                service.saveDietType(
                        entryId, name, description, callback);
                break;
        }
    }

    private void confirmDelete(View view) {
        if (!authorized || busy || completed || !editing
                || !isResumed()
                || getParentFragmentManager().isStateSaved()) {
            return;
        }

        if (activeDialog != null && activeDialog.isShowing()) {
            return;
        }

        String originalName = requireArguments().getString(ARG_NAME, "");

        activeDialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.admin_catalog_delete_title)
                .setMessage(getString(
                        R.string.admin_catalog_delete_message,
                        originalName))
                .setNegativeButton(R.string.admin_catalog_cancel, null)
                .setPositiveButton(
                        R.string.admin_catalog_delete,
                        (dialog, which) -> delete(view))
                .create();

        activeDialog.setOnDismissListener(dialog -> activeDialog = null);
        activeDialog.show();
    }

    private void delete(View view) {
        if (!isCurrent(view)
                || !authorized || busy || completed || !editing) {
            return;
        }

        busy = true;
        renderControls(view);
        showStatus(view, R.string.admin_catalog_deleting);

        CatalogRepository.Callback<Void> callback =
                operationCallback(view, true);

        switch (catalogType) {
            case 0:
                repository.deleteIngredientCategory(entryId, callback);
                break;
            case 1:
                repository.deleteAllergen(entryId, callback);
                break;
            case 2:
                repository.deleteMealType(entryId, callback);
                break;
            case 3:
                repository.deleteDishType(entryId, callback);
                break;
            case 4:
                repository.deleteDietType(entryId, callback);
                break;
        }
    }

    private CatalogRepository.Callback<Void> operationCallback(
            View view, boolean deleting) {

        return new CatalogRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                if (!isCurrent(view)) {
                    return;
                }

                busy = false;
                completed = true;
                completionMessage = deleting
                        ? R.string.admin_catalog_deleted
                        : R.string.admin_catalog_saved;

                renderControls(view);
                showStatus(view, completionMessage);

                if (isResumed()) {
                    Toast.makeText(
                            requireContext(),
                            completionMessage,
                            Toast.LENGTH_SHORT
                    ).show();
                }

                closeIfCompleted();
            }

            @Override
            public void onError(Exception error) {
                if (!isCurrent(view)) {
                    return;
                }

                busy = false;
                renderControls(view);

                Log.e("AdminCatalog", "Catalog operation failed", error);

                if (deleting
                        && error instanceof
                        CatalogRepository.EntryInUseException) {

                    showStatus(view, R.string.admin_catalog_in_use_message);
                    showEntryInUse();
                    return;
                }

                if (!deleting
                        && error instanceof IllegalArgumentException) {

                    ((TextInputLayout) view.findViewById(
                            R.id.til_catalog_name))
                            .setError(error.getMessage());

                    clearStatus(view);
                    return;
                }

                if (deleting
                        && error instanceof FirebaseFirestoreException
                        && ((FirebaseFirestoreException) error).getCode()
                        == FirebaseFirestoreException.Code.PERMISSION_DENIED) {

                    showStatus(view, R.string.admin_catalog_delete_denied);
                    return;
                }

                showStatus(view, deleting
                        ? R.string.admin_catalog_delete_error
                        : R.string.admin_catalog_save_error);
            }
        };
    }

    private void showEntryInUse() {
        if (!isResumed()
                || getParentFragmentManager().isStateSaved()) {
            return;
        }

        activeDialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.admin_catalog_in_use_title)
                .setMessage(R.string.admin_catalog_in_use_message)
                .setPositiveButton(R.string.admin_catalog_ok, null)
                .create();

        activeDialog.setOnDismissListener(dialog -> activeDialog = null);
        activeDialog.show();
    }

    private String text(TextInputEditText field) {
        return field.getText() == null
                ? ""
                : field.getText().toString().trim();
    }

    private void renderControls(View view) {
        boolean enabled = authorized && !busy && !completed;

        view.findViewById(R.id.et_catalog_name).setEnabled(enabled);
        view.findViewById(R.id.et_catalog_description).setEnabled(enabled);
        view.findViewById(R.id.btn_catalog_save).setEnabled(enabled);
        view.findViewById(R.id.btn_catalog_delete)
                .setEnabled(enabled && editing);

        view.findViewById(R.id.progress_catalog_editor)
                .setVisibility(busy ? View.VISIBLE : View.GONE);
    }

    private void showStatus(View view, int message) {
        ((TextView) view.findViewById(R.id.tv_catalog_editor_status))
                .setText(message);
    }

    private void clearStatus(View view) {
        ((TextView) view.findViewById(R.id.tv_catalog_editor_status))
                .setText("");
    }

    private void closeIfCompleted() {
        if (completed && isResumed()
                && !getParentFragmentManager().isStateSaved()) {
            getParentFragmentManager().popBackStack();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        closeIfCompleted();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("entry_id", entryId);
        outState.putBoolean("completed", completed);
        outState.putInt("completion_message", completionMessage);

        View view = getView();

        if (view != null) {
            outState.putString("draft_name", text(
                    view.findViewById(R.id.et_catalog_name)));

            outState.putString("draft_description", text(
                    view.findViewById(R.id.et_catalog_description)));
        }
    }

    @Override
    public void onDestroyView() {
        if (activeDialog != null) {
            activeDialog.dismiss();
            activeDialog = null;
        }

        authorized = false;
        super.onDestroyView();
    }
}