package com.siit.recipeapp.data.repository;

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

    // Prijava korisnika
    void login(String email, String password, LoginCallback callback);


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
}
