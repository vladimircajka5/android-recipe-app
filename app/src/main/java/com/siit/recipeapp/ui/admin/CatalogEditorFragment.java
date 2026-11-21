package com.siit.recipeapp.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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
    private boolean saving;

    private CatalogService service;

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

        service = AppRepositories.createCatalogService(
                AppRepositories.createCatalogRepository());

        authorized = false;
        saving = false;

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

        if (savedInstanceState == null) {
            name.setText(requireArguments().getString(ARG_NAME, ""));
            description.setText(
                    requireArguments().getString(ARG_DESCRIPTION, ""));
        }

        view.findViewById(R.id.btn_catalog_save)
                .setOnClickListener(v -> save(view));

        setFormEnabled(view, false);
        showStatus(view, R.string.admin_catalog_checking);

        AuthRepository auth = new FirebaseAuthRepository();

        auth.getCurrentRole(new AuthRepository.RoleCallback() {
            @Override
            public void onSuccess(UserRole role) {
                if (!isCurrent(view)) {
                    return;
                }

                authorized = role == UserRole.ADMIN;
                setFormEnabled(view, authorized);

                if (authorized) {
                    ((TextView) view.findViewById(
                            R.id.tv_catalog_editor_status)).setText("");
                } else {
                    showStatus(view, R.string.admin_catalog_denied);
                }
            }

            @Override
            public void onError(String message) {
                if (!isCurrent(view)) {
                    return;
                }

                showStatus(view, R.string.admin_catalog_role_error);
            }
        });
    }

    private boolean isCurrent(View view) {
        return isAdded() && getView() == view;
    }

    private void save(View view) {
        if (!authorized || saving) {
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

        saving = true;
        setFormEnabled(view, false);

        ((TextView) view.findViewById(
                R.id.tv_catalog_editor_status)).setText("");

        view.findViewById(R.id.progress_catalog_editor)
                .setVisibility(View.VISIBLE);

        CatalogRepository.Callback<Void> callback =
                new CatalogRepository.Callback<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        if (!isCurrent(view)) {
                            return;
                        }

                        saving = false;
                        view.findViewById(R.id.progress_catalog_editor)
                                .setVisibility(View.GONE);

                        showStatus(view, R.string.admin_catalog_saved);

                        if (!getParentFragmentManager().isStateSaved()) {
                            getParentFragmentManager().popBackStack();
                        }
                    }

                    @Override
                    public void onError(Exception error) {
                        if (!isCurrent(view)) {
                            return;
                        }

                        saving = false;
                        setFormEnabled(view, true);

                        view.findViewById(R.id.progress_catalog_editor)
                                .setVisibility(View.GONE);

                        Log.e("AdminCatalog", "Could not save entry", error);

                        TextView status = view.findViewById(
                                R.id.tv_catalog_editor_status);

                        if (error instanceof IllegalArgumentException) {
                            status.setText(error.getMessage());
                        } else {
                            status.setText(
                                    R.string.admin_catalog_save_error);
                        }
                    }
                };

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

    private String text(TextInputEditText field) {
        return field.getText() == null
                ? ""
                : field.getText().toString().trim();
    }

    private void setFormEnabled(View view, boolean enabled) {
        view.findViewById(R.id.et_catalog_name).setEnabled(enabled);
        view.findViewById(R.id.et_catalog_description).setEnabled(enabled);
        view.findViewById(R.id.btn_catalog_save).setEnabled(enabled);
    }

    private void showStatus(View view, int message) {
        ((TextView) view.findViewById(R.id.tv_catalog_editor_status))
                .setText(message);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("entry_id", entryId);
    }
}