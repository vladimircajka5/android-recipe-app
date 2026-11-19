
package com.siit.recipeapp.data.repository;

public interface AuthRepository {

    // Rezultat registracije
    // Sve radi asinhrono pa ne moze samo return vrednost,
    // nego moraju callbackovi
    interface RegisterCallback {
        void onSuccess(boolean verificationEmailSent);
        void onError(String message);
    }

    // Registracija korisnika
    void register(String username, String email, String password,
                  RegisterCallback callback);
}
