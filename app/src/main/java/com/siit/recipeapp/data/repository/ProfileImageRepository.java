package com.siit.recipeapp.data.repository;

public interface ProfileImageRepository {

    // ===== Rezultati operacija =====

    interface LoadCallback {
        void onSuccess(String imageUrl);
        void onError(String message);
    }

    interface SaveCallback {
        void onSuccess();
        void onError(String message);
    }

    // ===== Funkcionalnosti =====

    // Citanje URL-a profilne slike
    void loadImageUrl(String userId, LoadCallback callback);

    // Cuvanje URL-a profilne slike
    void saveImageUrl(String imageUrl, SaveCallback callback);

}
