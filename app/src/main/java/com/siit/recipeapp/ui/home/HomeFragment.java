package com.siit.recipeapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.siit.recipeapp.MainActivity;
import com.siit.recipeapp.R;
import com.siit.recipeapp.data.model.account.UserRole;
import com.siit.recipeapp.data.remote.UserDataSession;
import com.siit.recipeapp.data.repository.AuthRepository;
import com.siit.recipeapp.data.repository.firebase.FirebaseAuthRepository;
import com.siit.recipeapp.ui.admin.AdminCatalogFragment;
import com.siit.recipeapp.ui.auth.LoginFragment;
import com.siit.recipeapp.ui.notifications.NotificationsFragment;
import com.siit.recipeapp.ui.preview.RecipeSearchPreviewData.Recipe;
import com.siit.recipeapp.ui.proposals.CategoryProposalsFragment;
import com.siit.recipeapp.ui.recipe.RecipeDetailFragment;
import com.siit.recipeapp.ui.search.RecipeSearchFragment;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String STATE_MODE = "home_feed_mode";

    private final AuthRepository auth = new FirebaseAuthRepository();

    private int selectedMode = HomePreviewData.POPULAR;
    private int roleRequestVersion;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            selectedMode = Math.max(
                    0,
                    Math.min(
                            HomePreviewData.MODE_COUNT - 1,
                            savedInstanceState.getInt(
                                    STATE_MODE, HomePreviewData.POPULAR)));
        }
    }

    @Override
    public void onViewCreated(
            @NonNull View root,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(root, savedInstanceState);

        setupDrawer(root);

        root.findViewById(R.id.btn_home_search_recipes)
                .setOnClickListener(v -> open(new RecipeSearchFragment()));

        root.findViewById(R.id.btn_home_notifications)
                .setOnClickListener(v ->
                        openProtected(new NotificationsFragment()));

        root.findViewById(R.id.btn_home_proposals)
                .setOnClickListener(v ->
                        openProtected(new CategoryProposalsFragment()));

        root.findViewById(R.id.btn_home_catalog)
                .setOnClickListener(v ->
                        openProtected(new AdminCatalogFragment()));

        root.findViewById(R.id.btn_home_feed_sign_in)
                .setOnClickListener(v -> {
                    if (!auth.hasVerifiedSession()) {
                        open(new LoginFragment());
                    } else {
                        renderFeed(root);
                    }
                });

        Spinner feed = root.findViewById(R.id.spinner_home_feed);

        ArrayAdapter<CharSequence> choices =
                ArrayAdapter.createFromResource(
                        requireContext(),
                        R.array.home_dashboard_modes,
                        android.R.layout.simple_spinner_item);

        choices.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        feed.setAdapter(choices);
        feed.setSelection(selectedMode);

        feed.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View selected,
                            int position,
                            long id) {

                        selectedMode = position;
                        renderFeed(root);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

        root.findViewById(R.id.btn_home_can_make)
                .setOnClickListener(v -> {
                    selectedMode = HomePreviewData.CAN_MAKE;
                    feed.setSelection(selectedMode);
                    renderFeed(root);
                    closeMenu(root);
                    root.findViewById(R.id.scroll_home).scrollTo(0, 0);
                });

        renderFeed(root);
    }
    private void setupDrawer(View root) {
        DrawerLayout drawer = root.findViewById(R.id.drawer_home);
        MaterialToolbar toolbar = root.findViewById(R.id.toolbar_home);

        drawer.setScrimColor(0x66000000);
        drawer.setDrawerTitle(
                GravityCompat.END, getString(R.string.home_menu_title));

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_home_menu) {
                drawer.openDrawer(GravityCompat.END);
                return true;
            }

            return false;
        });

        root.findViewById(R.id.btn_home_menu_close)
                .setOnClickListener(v ->
                        drawer.closeDrawer(GravityCompat.END));

        OnBackPressedCallback closeDrawerOnBack =
                new OnBackPressedCallback(false) {
                    @Override
                    public void handleOnBackPressed() {
                        drawer.closeDrawer(GravityCompat.END);
                    }
                };

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(), closeDrawerOnBack);

        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(
                    @NonNull View drawerView, float slideOffset) {
                closeDrawerOnBack.setEnabled(slideOffset > 0f);
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                closeDrawerOnBack.setEnabled(true);
                refreshAccess(root);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                closeDrawerOnBack.setEnabled(false);
            }
        });
    }
    private void closeMenu(View root) {
        DrawerLayout drawer = root.findViewById(R.id.drawer_home);
        drawer.closeDrawer(GravityCompat.END, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        View root = getView();

        if (root != null) {
            renderFeed(root);
            refreshAccess(root);
        }
    }

    private void renderFeed(View root) {
        LinearLayout recipes = root.findViewById(R.id.layout_home_recipes);
        TextView empty = root.findViewById(R.id.tv_home_feed_empty);
        TextView count = root.findViewById(R.id.tv_home_feed_count);

        recipes.removeAllViews();

        ((TextView) root.findViewById(R.id.tv_home_access))
                .setText(auth.hasVerifiedSession()
                        ? R.string.home_dashboard_member
                        : R.string.home_dashboard_guest);

        ((TextView) root.findViewById(R.id.tv_home_feed_description))
                .setText(getResources().getStringArray(
                        R.array.home_dashboard_descriptions)[selectedMode]);

        boolean requiresSignIn =
                HomePreviewData.isPersonalMode(selectedMode)
                        && !auth.hasVerifiedSession();

        root.findViewById(R.id.btn_home_feed_sign_in)
                .setVisibility(requiresSignIn ? View.VISIBLE : View.GONE);

        if (requiresSignIn) {
            count.setVisibility(View.GONE);
            empty.setText(R.string.home_dashboard_personal_access);
            empty.setVisibility(View.VISIBLE);
            return;
        }

        List<HomePreviewData.Entry> entries =
                HomePreviewData.select(selectedMode);

        count.setVisibility(View.VISIBLE);
        count.setText(getResources().getQuantityString(
                R.plurals.home_dashboard_count,
                entries.size(),
                entries.size()));

        empty.setText(selectedMode == HomePreviewData.CAN_MAKE
                ? R.string.home_dashboard_pantry_empty
                : R.string.home_dashboard_empty);

        empty.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);

        for (HomePreviewData.Entry entry : entries) {
            addRecipe(recipes, root, entry);
        }
    }

    private void addRecipe(
            LinearLayout container,
            View root,
            HomePreviewData.Entry entry) {

        Recipe recipe = entry.recipe;

        View card = LayoutInflater.from(container.getContext())
                .inflate(R.layout.item_home_recipe, container, false);

        ((TextView) card.findViewById(R.id.tv_home_recipe_title))
                .setText(recipe.title);

        ((TextView) card.findViewById(R.id.tv_home_recipe_description))
                .setText(recipe.description);

        ((TextView) card.findViewById(R.id.tv_home_recipe_meta))
                .setText(getString(
                        R.string.search_result_meta,
                        getString(recipe.dish),
                        getString(recipe.meal),
                        recipe.minutes));

        ((TextView) card.findViewById(R.id.tv_home_recipe_context))
                .setText(contextText(entry));

        ImageView photo = card.findViewById(R.id.iv_home_recipe_photo);

        if (recipe.photo == 0) {
            photo.setVisibility(View.GONE);
        } else {
            photo.setImageResource(recipe.photo);
        }

        card.setOnClickListener(v -> {
            if (HomePreviewData.isPersonalMode(selectedMode)
                    && !auth.hasVerifiedSession()) {
                renderFeed(root);
                return;
            }

            open(RecipeDetailFragment.newPreview(recipe.id));
        });

        container.addView(card);
    }

    private String contextText(HomePreviewData.Entry entry) {
        switch (selectedMode) {
            case HomePreviewData.NEWEST:
                return getString(
                        R.string.home_dashboard_published,
                        date(entry.publishedAt));

            case HomePreviewData.VIEWED:
                return getString(
                        R.string.home_dashboard_viewed,
                        date(entry.viewedAt));

            case HomePreviewData.COOKED:
                return getString(
                        R.string.home_dashboard_cooked,
                        date(entry.cookingStartedAt));

            case HomePreviewData.CAN_MAKE:
                return getString(R.string.home_dashboard_available);

            case HomePreviewData.POPULAR:
            default:
                return getString(
                        R.string.home_dashboard_monthly_rating,
                        entry.monthlyAverage,
                        entry.monthlyRatingCount);
        }
    }

    private String date(long timestamp) {
        return DateFormat.getDateTimeInstance(
                        DateFormat.SHORT, DateFormat.SHORT)
                .format(new Date(timestamp));
    }

    private void refreshAccess(View root) {
        int request = ++roleRequestVersion;

        View adminSection = root.findViewById(R.id.layout_home_admin);
        TextView roleState = root.findViewById(R.id.tv_home_role_state);

        adminSection.setVisibility(View.GONE);

        String ownerId = UserDataSession.currentOwnerId(requireContext());

        if (!auth.hasVerifiedSession() || ownerId == null) {
            roleState.setVisibility(View.GONE);
            return;
        }

        roleState.setText(R.string.home_dashboard_checking_role);
        roleState.setVisibility(View.VISIBLE);

        auth.getCurrentRole(new AuthRepository.RoleCallback() {
            @Override
            public void onSuccess(UserRole role) {
                if (!sameRoleRequest(root, ownerId, request)) {
                    return;
                }

                roleState.setVisibility(View.GONE);
                adminSection.setVisibility(
                        role == UserRole.ADMIN ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String message) {
                if (!sameRoleRequest(root, ownerId, request)) {
                    return;
                }

                adminSection.setVisibility(View.GONE);
                roleState.setText(R.string.home_dashboard_role_error);
                roleState.setVisibility(View.VISIBLE);
            }
        });
    }

    private boolean sameRoleRequest(
            View root,
            String ownerId,
            int request) {

        return isAdded()
                && getView() == root
                && request == roleRequestVersion
                && auth.hasVerifiedSession()
                && ownerId.equals(
                UserDataSession.currentOwnerId(requireContext()));
    }

    private void open(Fragment fragment) {
        if (!isResumed() || getParentFragmentManager().isStateSaved()) {
            return;
        }

        View root = getView();
        if (root != null) {
            closeMenu(root);
        }

        getParentFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void openProtected(Fragment fragment) {
        if (!isResumed() || getParentFragmentManager().isStateSaved()) {
            return;
        }

        View root = getView();
        if (root != null) {
            closeMenu(root);
        }

        ((MainActivity) requireActivity()).openProtectedFragment(fragment);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(STATE_MODE, selectedMode);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        roleRequestVersion++;
        super.onDestroyView();
    }
}