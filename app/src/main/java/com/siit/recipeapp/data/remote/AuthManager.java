package com.siit.recipeapp.data.remote;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public final class AuthManager {
    public interface Callback<T> {
        void onSuccess(T value);
        void onError(@NonNull Exception exception);
    }

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    public AuthManager() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public void login(String identifier, String password, Callback<FirebaseUser> callback) {
        resolveEmail(identifier, new Callback<String>() {
            @Override
            public void onSuccess(String email) {
                auth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener(result -> {
                            FirebaseUser user = result.getUser();
                            if (user == null) {
                                callback.onError(new IllegalStateException("User is missing"));
                            } else if (!user.isEmailVerified()) {
                                auth.signOut();
                                callback.onError(new IllegalStateException(
                                        "Verify your email address before signing in"));
                            } else {
                                callback.onSuccess(user);
                            }
                        })
                        .addOnFailureListener(callback::onError);
            }

            @Override
            public void onError(@NonNull Exception exception) {
                callback.onError(exception);
            }
        });
    }

    public void register(String usernameInput, String emailInput, String password,
                         Callback<FirebaseUser> callback) {
        final String username = usernameInput == null ? "" : usernameInput.trim();
        final String email = emailInput == null ? "" : emailInput.trim().toLowerCase(java.util.Locale.ROOT);
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        callback.onError(new IllegalStateException("User is missing"));
                        return;
                    }
                    Map<String, Object> profile = new HashMap<>();
                    profile.put("uid", user.getUid());
                    profile.put("username", username);
                    profile.put("email", email);
                    profile.put("role", "USER");
                    profile.put("emailVerified", false);
                    profile.put("createdAt", System.currentTimeMillis());

                    firestore.runTransaction(transaction -> {
                                if (transaction.get(firestore.collection("usernames")
                                        .document(username)).exists()) {
                                    throw new IllegalStateException("Username is already taken");
                                }
                                transaction.set(firestore.collection("usernames").document(username),
                                        mapOf("uid", user.getUid(), "email", email));
                                transaction.set(firestore.collection("users").document(user.getUid()),
                                        profile);
                                return null;
                            }).addOnSuccessListener(ignored ->
                                    user.sendEmailVerification()
                                            .addOnSuccessListener(ignoredEmail ->
                                                    callback.onSuccess(user))
                                            .addOnFailureListener(callback::onError))
                            .addOnFailureListener(error -> {
                                user.delete();
                                callback.onError(error);
                            });
                })
                .addOnFailureListener(callback::onError);
    }

    public void sendPasswordReset(String email, Callback<Void> callback) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(ignored -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    public void changePassword(String currentPassword, String newPassword,
                               Callback<Void> callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            callback.onError(new IllegalStateException("You must be signed in"));
            return;
        }
        com.google.firebase.auth.AuthCredential credential =
                com.google.firebase.auth.EmailAuthProvider.getCredential(
                        user.getEmail(), currentPassword);
        user.reauthenticate(credential)
                .continueWithTask(ignored -> user.updatePassword(newPassword))
                .addOnSuccessListener(ignored -> callback.onSuccess(null))
                .addOnFailureListener(callback::onError);
    }

    public void logout() {
        auth.signOut();
    }

    private void resolveEmail(String identifier, Callback<String> callback) {
        if (identifier.contains("@")) {
            callback.onSuccess(identifier.trim());
            return;
        }
        firestore.collection("usernames").document(identifier.trim()).get()
                .addOnSuccessListener(document -> {
                    String email = document.getString("email");
                    if (email == null || email.trim().isEmpty()) {
                        callback.onError(new IllegalArgumentException("Username was not found"));
                    } else {
                        callback.onSuccess(email);
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    private Map<String, Object> mapOf(String key, Object value, String secondKey,
                                      Object secondValue) {
        Map<String, Object> values = new HashMap<>();
        values.put(key, value);
        values.put(secondKey, secondValue);
        return values;
    }
}