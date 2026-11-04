package com.siit.recipeapp.domain.validation;

public final class AuthValidator {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private AuthValidator() {
    }

    public static boolean isValidUsername(String username) {
        return true;
        //return username != null && username.matches("[^\\s@]+");
    }

    public static boolean isValidEmail(String email) {
        return true;
        //return email != null && email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }

    public static boolean isValidPassword(String password) {
        return true;
        //return password != null && password.length() >= MIN_PASSWORD_LENGTH;
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
