
package com.siit.recipeapp.ui.recipe;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.siit.recipeapp.R;
import com.siit.recipeapp.domain.validation.AuthValidator;
import com.siit.recipeapp.ui.profile.CountryFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserRecipesFragment extends Fragment {

    // Argumenti
    public static final String ARG_USER_ID = "userId";
    public static final String ARG_USERNAME = "username";

    // Veze
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    // Constructor
    public UserRecipesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_recipes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ===== Povezivanje =====

        RecyclerView rvRecipes = view.findViewById(R.id.rv_user_recipes);
        TextView tvEmpty = view.findViewById(R.id.tv_user_recipes_empty);
        TextView tvCount = view.findViewById(R.id.tv_user_recipes_count);

        // ===== Navigacija =====

        // Nema posebne navigacije

        // ===== Funkcionalnosti =====

        // Recepti jos nisu povezani sa bazom
        // Ne prikazujemo probne recepte kao stvarne
        rvRecipes.setVisibility(View.GONE);
        tvCount.setVisibility(View.GONE);

        tvEmpty.setText(R.string.user_recipes_not_ready);
        tvEmpty.setVisibility(View.VISIBLE);

        Bundle args = getArguments();

        if (args == null) {
            showUnavailable(view);
            return;
        }

        String userId = args.getString(ARG_USER_ID);
        String username = args.getString(ARG_USERNAME);

        // Ako vec imamo UID, odmah ucitaj profil
        if (userId != null && !userId.trim().isEmpty()) {
            loadProfile(userId.trim(), view);
            return;
        }

        // Inace pronadji UID na osnovu korisnickog imena
        if (AuthValidator.isValidUsername(username)) {
            findUserByUsername(username, view);
        } else {
            showUnavailable(view);
        }
    }

    // ===== Privatne metode =====

    // Pronalazenje korisnika na osnovu username-a
    private void findUserByUsername(String username, View view) {

        String usernameKey = username.trim().toLowerCase(Locale.ROOT);

        firestore.collection("usernames")
                .document(usernameKey)
                .get()
                .addOnSuccessListener(document -> {

                    if (!isAdded() || getView() != view) {
                        return;
                    }

                    String userId = document.getString("uid");

                    if (userId == null || userId.isEmpty()) {
                        showUnavailable(view);
                        return;
                    }

                    loadProfile(userId, view);
                })
                .addOnFailureListener(error -> showUnavailable(view));
    }

    // Ucitavanje korisnickog profila iz Firestore-a
    private void loadProfile(String userId, View view) {

        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {

                    if (!isAdded() || getView() != view) {
                        return;
                    }

                    if (!document.exists()) {
                        showUnavailable(view);
                        return;
                    }

                    bindProfile(document, view);
                })
                .addOnFailureListener(error -> showUnavailable(view));
    }

    // Popunjavanje elemenata ekrana
    private void bindProfile(DocumentSnapshot document, View view) {

        TextView tvUsername =
                view.findViewById(R.id.tv_user_recipes_username);

        TextView tvRegion =
                view.findViewById(R.id.tv_user_recipes_region);

        ImageView ivAvatar =
                view.findViewById(R.id.iv_user_recipes_avatar);

        String username = document.getString("username");
        String country = document.getString("country");
        String region = document.getString("region");
        String city = document.getString("city");
        String imageUrl = document.getString("profileImageUrl");

        // Korisnicko ime
        tvUsername.setText(username == null ? "" : username);

        // Lokacija sa zastavom
        List<String> locationParts = new ArrayList<>();

        if (country != null && !country.trim().isEmpty()) {
            locationParts.add(CountryFlag.format(country));
        }

        if (region != null && !region.trim().isEmpty()) {
            locationParts.add(region.trim());
        }

        if (city != null && !city.trim().isEmpty()) {
            locationParts.add(city.trim());
        }

        if (locationParts.isEmpty()) {
            tvRegion.setText(R.string.profile_region_empty);
        } else {
            tvRegion.setText(TextUtils.join(", ", locationParts));
        }

        // Profilna slika
        Glide.with(this)
                .load(imageUrl == null || imageUrl.isEmpty() ? null : imageUrl)
                .placeholder(R.drawable.ic_avatar_placeholder)
                .fallback(R.drawable.ic_avatar_placeholder)
                .error(R.drawable.ic_avatar_placeholder)
                .centerCrop()
                .into(ivAvatar);
    }

    // Profil ne postoji ili nije dostupan
    private void showUnavailable(View view) {

        if (!isAdded() || getView() != view) {
            return;
        }

        TextView tvUsername =
                view.findViewById(R.id.tv_user_recipes_username);

        TextView tvRegion =
                view.findViewById(R.id.tv_user_recipes_region);

        tvUsername.setText(R.string.other_profile_unavailable);
        tvRegion.setText("");
    }
}
