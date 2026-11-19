package com.siit.recipeapp.data.repository;

import com.siit.recipeapp.data.model.account.UserRole;

public interface AuthRepository {

    // ===== Registracija =====

    // Rezultat registracije
    interface RegisterCallback {
        void onSuccess(boolean verificationEmailSent);
        void onError(String message);
    }

    // Registracija korisnika
    void register(String username, String email, String password,
                  RegisterCallback callback);


    // ===== Prijava =====

    // Rezultat prijave
    interface LoginCallback {
        void onSuccess();
        void onError(String message);
    }

    // Prijava preko emaila ili korisnickog imena
    void login(String identifier, String password, LoginCallback callback);


    // ===== Odjava =====

    // Odjava korisnika
    void logout();


    // ===== Upravljanje lozinkama =====

    // Rezultat operacije nad lozinkom
    interface PasswordCallback {
        void onSuccess();
        void onError(String message);
    }

    // Slanje emaila za resetovanje lozinke
    void sendPasswordResetEmail(String email, PasswordCallback callback);

    // Promena lozinke prijavljenog korisnika
    void changePassword(String currentPassword, String newPassword,
                        PasswordCallback callback);

    // ===== Sesija =====

    // Provera da li postoji prijavljen i verifikovan korisnik
    boolean hasVerifiedSession();

    // ===== Administratorske uloge =====

    // Rezultat citanja uloge
    interface RoleCallback {
        void onSuccess(UserRole role);
        void onError(String message);
    }

    // Rezultat dodele administratorske uloge
    interface AdminCallback {
        void onSuccess();
        void onError(String message);
    }

    // Citanje uloge trenutno prijavljenog korisnika
    void getCurrentRole(RoleCallback callback);

    // Dodela ADMIN uloge korisniku na osnovu username-a
    void grantAdmin(String username, AdminCallback callback);
}
