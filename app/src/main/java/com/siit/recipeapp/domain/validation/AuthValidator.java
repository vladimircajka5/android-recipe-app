package com.siit.recipeapp.domain.validation;

import android.util.Patterns;

public final class AuthValidator {

    // Svojstva
    private static final int MIN_PASSWORD_LENGTH = 6;

    // Constructor
    private AuthValidator() {
    }

    public static boolean isValidUsername(String username) {

        if (username == null) {
            return false;
        }

        String value = username.trim();

        return !value.isEmpty()
                && !value.contains("/")
                && !value.contains("@")
                && !value.equals(".")
                && !value.equals("..");
    }

    public static boolean isValidEmail(String email) {
        return email != null
                && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null
                && password.length() >= MIN_PASSWORD_LENGTH;
    }

    public static boolean passwordsMatch(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }

    public static boolean isValidLoginIdentifier(String identifier) {
        return isValidUsername(identifier) || isValidEmail(identifier);
    }

    public static boolean isValidRegistration(
            String username,
            String email,
            String password,
            String confirmPassword) {

        return isValidUsername(username)
                && isValidEmail(email)
                && isValidPassword(password)
                && passwordsMatch(password, confirmPassword);
    }
}
