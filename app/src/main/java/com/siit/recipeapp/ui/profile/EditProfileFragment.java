
package com.siit.recipeapp.ui.profile;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.siit.recipeapp.R;
import com.siit.recipeapp.RecipeApplication;
import com.siit.recipeapp.data.local.RecipeDatabase;
import com.siit.recipeapp.data.local.entity.UserProfileEntity;
import com.siit.recipeapp.data.model.account.Profile;
import com.siit.recipeapp.data.model.location.Location;
import com.siit.recipeapp.data.remote.UserDataSession;
import com.siit.recipeapp.data.repository.ProfileImageRepository;
import com.siit.recipeapp.data.repository.ProfileRepository;
import com.siit.recipeapp.data.repository.firebase.FirebaseProfileImageRepository;
import com.siit.recipeapp.data.repository.room.RoomProfileRepository;
import com.siit.recipeapp.domain.validation.ProfileValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    // Automatski pregled profilne slike
    private final Handler previewHandler = new Handler(Looper.getMainLooper());
    private Runnable previewRunnable;

    // Veze
    private final ProfileImageRepository imageRepository =
            new FirebaseProfileImageRepository();

    private final Map<String, String> countryOptions = new HashMap<>();

    private UserProfileEntity currentProfile;

    private boolean imageUrlEdited = false;
    private boolean settingImageUrl = false;

    // Constructor
    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ===== Povezivanje =====

        TextView tvUsername = view.findViewById(R.id.tv_edit_profile_username);
        TextView tvError = view.findViewById(R.id.tv_edit_profile_error);

        ImageView ivAvatar = view.findViewById(R.id.iv_edit_profile_avatar);

        MaterialAutoCompleteTextView etCountry =
                view.findViewById(R.id.et_edit_profile_country);

        TextInputEditText etRegion = view.findViewById(R.id.et_edit_profile_region);
        TextInputEditText etCity = view.findViewById(R.id.et_edit_profile_city);
        TextInputEditText etBio = view.findViewById(R.id.et_edit_profile_bio);
        TextInputEditText etImageUrl =
                view.findViewById(R.id.et_edit_profile_image_url);

        Button btnCancel = view.findViewById(R.id.btn_edit_profile_cancel);
        Button btnSave = view.findViewById(R.id.btn_edit_profile_save);

        btnSave.setEnabled(false);

        setupCountries(etCountry);

        // ===== Navigacija =====

        // Povratak bez cuvanja
        btnCancel.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        // ===== Funkcionalnosti =====

        String userId = UserDataSession.currentOwnerId(requireContext());

        if (userId == null) {
            showError(tvError, R.string.edit_profile_not_ready);
            return;
        }

        RecipeDatabase database = RecipeDatabase.getInstance(requireContext());

        ProfileRepository profileRepository =
                new RoomProfileRepository(database, requireContext());

        // Automatski pregled slike prilikom izmene URL-a
        etImageUrl.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (settingImageUrl) {
                    return;
                }

                imageUrlEdited = true;

                // Otkazi prethodno zakazano ucitavanje
                if (previewRunnable != null) {
                    previewHandler.removeCallbacks(previewRunnable);
                }

                String url = s.toString().trim();

                // Ucitaj sliku kada korisnik prestane da kuca
                previewRunnable = () -> {

                    if (!isAdded() || getView() != view) {
                        return;
                    }

                    showImagePreview(url, ivAvatar);
                };

                previewHandler.postDelayed(previewRunnable, 600);
            }
        });

        // Ucitavanje podataka iz lokalne baze
        database.userProfileDao()
                .observeProfile(userId)
                .observe(getViewLifecycleOwner(), profile -> {

                    if (profile == null) {
                        showError(tvError, R.string.edit_profile_not_ready);
                        return;
                    }

                    // Polja popunjavamo samo prvi put
                    if (currentProfile == null) {

                        tvUsername.setText(getString(
                                R.string.edit_profile_username_value,
                                profile.username
                        ));

                        etCountry.setText(
                                findCountryOption(profile.country), false
                        );

                        etRegion.setText(profile.region);
                        etCity.setText(profile.city);
                        etBio.setText(profile.biography);
                    }

                    currentProfile = profile;
                    btnSave.setEnabled(true);
                    tvError.setVisibility(View.GONE);
                });

        // Ucitavanje trenutnog URL-a slike
        imageRepository.loadImageUrl(userId,
                new ProfileImageRepository.LoadCallback() {

                    @Override
                    public void onSuccess(String imageUrl) {

                        if (!isAdded() || getView() != view) {
                            return;
                        }

                        String url = imageUrl == null ? "" : imageUrl;

                        // Ne menjaj tekst ako je korisnik vec poceo unos
                        if (!imageUrlEdited) {
                            settingImageUrl = true;
                            etImageUrl.setText(url);
                            settingImageUrl = false;

                            showImagePreview(url, ivAvatar);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        // Tekstualni profil i dalje moze da se uredjuje
                    }
                });

        // Cuvanje izmena
        btnSave.setOnClickListener(v -> {

            tvError.setVisibility(View.GONE);

            if (currentProfile == null) {
                showError(tvError, R.string.edit_profile_not_ready);
                return;
            }

            String selectedCountry = etCountry.getText().toString().trim();
            String country = "";

            // Ako drzava postoji, mora biti izabrana iz liste
            if (!selectedCountry.isEmpty()) {
                country = countryOptions.get(selectedCountry);

                if (country == null) {
                    showError(tvError, R.string.edit_profile_invalid_country);
                    return;
                }
            }

            String region = etRegion.getText().toString().trim();
            String city = etCity.getText().toString().trim();
            String biography = etBio.getText().toString().trim();
            String imageUrl = etImageUrl.getText().toString().trim();

            Location location = new Location(
                    country,
                    region,
                    city,
                    currentProfile.latitude,
                    currentProfile.longitude
            );

            Profile profile = new Profile(biography, location);

            if (!ProfileValidator.isValid(profile)) {
                showError(tvError, R.string.edit_profile_invalid_data);
                return;
            }

            btnSave.setEnabled(false);
            btnCancel.setEnabled(false);

            // Menjaj URL samo ako ga je korisnik uredjivao
            if (imageUrlEdited) {

                imageRepository.saveImageUrl(imageUrl,
                        new ProfileImageRepository.SaveCallback() {

                            @Override
                            public void onSuccess() {
                                saveProfile(view, profileRepository, profile,
                                        btnSave, btnCancel, tvError);
                            }

                            @Override
                            public void onError(String message) {
                                handleSaveError(view, btnSave, btnCancel,
                                        tvError, R.string.edit_profile_image_failed);
                            }
                        });

            } else {

                // Bez izmene URL-a cuvamo samo tekstualne podatke
                saveProfile(view, profileRepository, profile,
                        btnSave, btnCancel, tvError);
            }
        });

        // Pokreni sinhronizaciju postojećeg profila
        ((RecipeApplication) requireActivity().getApplication())
                .syncSignedInUser();
    }

    // ===== Privatne metode =====

    // Popunjavanje liste drzava
    private void setupCountries(MaterialAutoCompleteTextView etCountry) {

        List<String> countries = new ArrayList<>();

        for (String code : Locale.getISOCountries()) {

            Locale locale = new Locale.Builder()
                    .setRegion(code)
                    .build();

            String name = locale.getDisplayCountry(Locale.ENGLISH);

            int first = 0x1F1E6 + code.charAt(0) - 'A';
            int second = 0x1F1E6 + code.charAt(1) - 'A';

            String flag = new String(Character.toChars(first))
                    + new String(Character.toChars(second));

            String option = flag + " " + name;

            countries.add(option);
            countryOptions.put(option, name);
        }

        // Sortiranje po nazivu drzave
        countries.sort((a, b) ->
                countryOptions.get(a).compareToIgnoreCase(countryOptions.get(b)));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                countries
        );

        etCountry.setAdapter(adapter);
        etCountry.setThreshold(1);

        etCountry.setOnClickListener(v -> etCountry.showDropDown());
    }

    // Prikaz postojece drzave iz baze
    private String findCountryOption(String country) {

        if (country == null || country.isEmpty()) {
            return "";
        }

        for (Map.Entry<String, String> entry : countryOptions.entrySet()) {
            if (country.equals(entry.getValue())) {
                return entry.getKey();
            }
        }

        return country;
    }

    // Prikaz profilne slike na osnovu URL-a
    private void showImagePreview(String url, ImageView ivAvatar) {

        Glide.with(this)
                .load(url.isEmpty() ? null : url)
                .placeholder(R.drawable.ic_avatar_placeholder)
                .fallback(R.drawable.ic_avatar_placeholder)
                .error(R.drawable.ic_avatar_placeholder)
                .centerCrop()
                .into(ivAvatar);
    }

    // Cuvanje biografije i lokacije u Room bazu
    private void saveProfile(View view,
                             ProfileRepository repository,
                             Profile profile,
                             Button btnSave,
                             Button btnCancel,
                             TextView tvError) {

        if (!isAdded() || getView() != view) {
            return;
        }

        try {
            repository.updateProfile(currentProfile.username, profile);

            Toast.makeText(requireContext(),
                    R.string.edit_profile_success,
                    Toast.LENGTH_SHORT).show();

            getParentFragmentManager().popBackStack();

        } catch (RuntimeException error) {
            handleSaveError(view, btnSave, btnCancel, tvError,
                    R.string.edit_profile_save_failed);
        }
    }

    // Neuspesno cuvanje
    private void handleSaveError(View view,
                                 Button btnSave,
                                 Button btnCancel,
                                 TextView tvError,
                                 int messageId) {

        if (!isAdded() || getView() != view) {
            return;
        }

        btnSave.setEnabled(true);
        btnCancel.setEnabled(true);

        showError(tvError, messageId);
    }

    // Prikaz greske
    private void showError(TextView tvError, int messageId) {
        tvError.setText(messageId);
        tvError.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {

        // Otkazi zakazani pregled ako je ekran zatvoren
        if (previewRunnable != null) {
            previewHandler.removeCallbacks(previewRunnable);
        }

        super.onDestroyView();
    }
}
