package com.siit.recipeapp.data.repository.firebase;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.siit.recipeapp.data.repository.ProfileImageRepository;

public class FirebaseProfileImageRepository implements ProfileImageRepository {
    // Veze
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    // ===== Funkcionalnosti =====

    // Citanje URL-a slike
    @Override
    public void loadImageUrl(String userId, LoadCallback callback) {

        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {

                    String imageUrl = document.getString("profileImageUrl");
                    callback.onSuccess(imageUrl);
                })
                .addOnFailureListener(error -> {
                    callback.onError("IMAGE_LOAD_FAILED");
                });
    }

    // Cuvanje URL-a slike
    @Override
    public void saveImageUrl(String imageUrl, SaveCallback callback) {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null || !user.isEmailVerified()) {
            callback.onError("NO_USER");
            return;
        }

        String url = imageUrl.trim();

        // Prazan URL znaci uklanjanje profilne slike
        if (!url.isEmpty() && !isValidImageUrl(url)) {
            callback.onError("INVALID_URL");
            return;
        }

        firestore.collection("users")
                .document(user.getUid())
                .update("profileImageUrl", url)
                .addOnSuccessListener(unused -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(error -> {
                    callback.onError("IMAGE_SAVE_FAILED");
                });
    }

    // ===== Privatne metode =====

    // Dozvoljavamo samo HTTPS URL-ove
    private boolean isValidImageUrl(String url) {

        if (url.length() > 2048) {
            return false;
        }

        Uri uri = Uri.parse(url);

        return "https".equalsIgnoreCase(uri.getScheme())
                && uri.getHost() != null
                && !uri.getHost().isEmpty()
                && uri.getUserInfo() == null;
    }
}
