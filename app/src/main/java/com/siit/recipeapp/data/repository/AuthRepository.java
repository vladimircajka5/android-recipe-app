
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

    // Rezultat prijave
    interface LoginCallback {
        void onSuccess();
        void onError(String message);
    }

    // Prijava korisnika
    void login(String email, String password, LoginCallback callback);

    // rezultat operacije koja ne vraca podatke
    // za reset i promenu lozinke
    interface OperationCallback {
        void onSuccess();
        void onError(String message);
    }

    // Slanje emaila za reset lozinke
    void sendPasswordReset(String email, OperationCallback callback);

    // Odjava korisnika
    void logout();
}
