package com.siit.recipeapp.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.siit.recipeapp.R;
import com.siit.recipeapp.data.model.account.UserRole;
import com.siit.recipeapp.data.model.catalog.Allergen;
import com.siit.recipeapp.data.model.catalog.DietType;
import com.siit.recipeapp.data.model.catalog.DishType;
import com.siit.recipeapp.data.model.catalog.IngredientCategory;
import com.siit.recipeapp.data.model.catalog.MealType;
import com.siit.recipeapp.data.repository.AppRepositories;
import com.siit.recipeapp.data.repository.AuthRepository;
import com.siit.recipeapp.data.repository.CatalogRepository;
import com.siit.recipeapp.data.repository.firebase.FirebaseAuthRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class AdminCatalogFragment extends Fragment {

    private CatalogRepository repository;
    private AuthRepository authRepository;

    private Spinner typeSpinner;
    private ArrayAdapter<String> adapter;

    private boolean authorized;
    private int requestVersion;
    private final List<CatalogRow> rows = new ArrayList<>();

    private static final class CatalogRow {
        final String id;
        final String name;
        final String description;

        CatalogRow(String id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description == null ? "" : description;
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_admin_catalog, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        repository = AppRepositories.createCatalogRepository();
        authRepository = new FirebaseAuthRepository();
        authorized = false;

        typeSpinner = view.findViewById(R.id.spinner_catalog_type);

        ArrayAdapter<CharSequence> types = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.admin_catalog_types,
                android.R.layout.simple_spinner_item
        );

        types.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        typeSpinner.setAdapter(types);

        if (savedInstanceState != null) {
            typeSpinner.setSelection(
                    savedInstanceState.getInt("catalog_type", 0));
        }

        adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                new ArrayList<String>()
        );

        ListView list = view.findViewById(R.id.list_catalog);
        list.setAdapter(adapter);

        list.setOnItemClickListener((parent, selected, position, id) -> {
            if (authorized && position >= 0 && position < rows.size()) {
                openEditor(rows.get(position));
            }
        });

        view.findViewById(R.id.btn_catalog_add)
                .setOnClickListener(v -> openEditor(null));

        view.findViewById(R.id.btn_catalog_refresh)
                .setOnClickListener(v -> checkAccess(view));

        typeSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View selected,
                            int position,
                            long id) {

                        if (authorized) {
                            loadCatalog(view);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

        checkAccess(view);
    }

    private boolean isCurrent(View view, int version) {
        return isAdded()
                && getView() == view
                && requestVersion == version;
    }

    private void checkAccess(View view) {

        int version = ++requestVersion;
        authorized = false;
        adapter.clear();
        rows.clear();

        setBusy(view, true);
        showStatus(view, R.string.admin_catalog_checking);

        if (!authRepository.hasVerifiedSession()) {
            setBusy(view, false);
            showStatus(view, R.string.admin_catalog_denied);
            return;
        }

        authRepository.getCurrentRole(new AuthRepository.RoleCallback() {
            @Override
            public void onSuccess(UserRole role) {
                if (!isCurrent(view, version)) {
                    return;
                }

                authorized = role == UserRole.ADMIN;

                if (authorized) {
                    loadCatalog(view);
                } else {
                    setBusy(view, false);
                    showStatus(view, R.string.admin_catalog_denied);
                }
            }

            @Override
            public void onError(String message) {
                if (!isCurrent(view, version)) {
                    return;
                }

                setBusy(view, false);
                showStatus(view, R.string.admin_catalog_role_error);
            }
        });
    }

    private void loadCatalog(View view) {

        if (!authorized) {
            return;
        }

        int version = ++requestVersion;
        adapter.clear();
        rows.clear();

        setBusy(view, true);
        showStatus(view, R.string.admin_catalog_loading);

        switch (typeSpinner.getSelectedItemPosition()) {
            case 0:
                repository.getIngredientCategories(listCallback(
                        view, version,
                        IngredientCategory::getId,
                        IngredientCategory::getName,
                        IngredientCategory::getDescription));
                break;

            case 1:
                repository.getAllergens(listCallback(
                        view, version,
                        Allergen::getId,
                        Allergen::getName,
                        Allergen::getDescription));
                break;

            case 2:
                repository.getMealTypes(listCallback(
                        view, version,
                        MealType::getId,
                        MealType::getName,
                        MealType::getDescription));
                break;

            case 3:
                repository.getDishTypes(listCallback(
                        view, version,
                        DishType::getId,
                        DishType::getName,
                        DishType::getDescription));
                break;

            case 4:
                repository.getDietTypes(listCallback(
                        view, version,
                        DietType::getId,
                        DietType::getName,
                        DietType::getDescription));
                break;
        }
    }

    private <T> CatalogRepository.Callback<List<T>> listCallback(
            View view,
            int version,
            Function<T, String> idOf,
            Function<T, String> nameOf,
            Function<T, String> descriptionOf) {

        return new CatalogRepository.Callback<List<T>>() {
            @Override
            public void onSuccess(List<T> entries) {
                if (!isCurrent(view, version)) {
                    return;
                }

                adapter.clear();
                rows.clear();

                for (T entry : entries) {
                    CatalogRow row = new CatalogRow(
                            idOf.apply(entry),
                            nameOf.apply(entry),
                            descriptionOf.apply(entry)
                    );

                    rows.add(row);

                    adapter.add(
                            row.description.isEmpty()
                                    ? row.name
                                    : row.name + "\n" + row.description
                    );
                }
                setBusy(view, false);

                TextView status = view.findViewById(R.id.tv_catalog_status);

                if (entries.isEmpty()) {
                    status.setText(R.string.admin_catalog_empty);
                } else {
                    status.setText(getString(
                            R.string.admin_catalog_count, entries.size()));
                }
            }

            @Override
            public void onError(Exception error) {
                if (!isCurrent(view, version)) {
                    return;
                }

                Log.e("AdminCatalog", "Could not load catalog", error);
                setBusy(view, false);
                showStatus(view, R.string.admin_catalog_load_error);
            }
        };
    }

    private void setBusy(View view, boolean busy) {
        view.findViewById(R.id.progress_catalog)
                .setVisibility(busy ? View.VISIBLE : View.GONE);

        view.findViewById(R.id.btn_catalog_refresh)
                .setEnabled(!busy);

        typeSpinner.setEnabled(authorized && !busy);
        view.findViewById(R.id.btn_catalog_add)
                .setEnabled(authorized && !busy);

        view.findViewById(R.id.list_catalog)
                .setEnabled(authorized && !busy);
    }

    private void showStatus(View view, int message) {
        ((TextView) view.findViewById(R.id.tv_catalog_status))
                .setText(message);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (typeSpinner != null) {
            outState.putInt(
                    "catalog_type",
                    typeSpinner.getSelectedItemPosition());
        }
    }

    @Override
    public void onDestroyView() {
        requestVersion++;
        authorized = false;
        typeSpinner = null;
        adapter = null;
        super.onDestroyView();
    }

    private void openEditor(CatalogRow row) {
        if (!authorized) {
            return;
        }

        CatalogEditorFragment fragment = CatalogEditorFragment.newInstance(
                typeSpinner.getSelectedItemPosition(),
                row == null ? null : row.id,
                row == null ? "" : row.name,
                row == null ? "" : row.description
        );

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}