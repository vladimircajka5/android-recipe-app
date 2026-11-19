package com.siit.recipeapp.data.repository.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.siit.recipeapp.data.repository.AuthRepository;
import com.siit.recipeapp.domain.validation.AuthValidator;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// Firebase Authentication upravlja nalozima, lozinkama i potvrdom emaila.
// Svaki nalog dobija jedinstveni UID.
// Cloud Firestore čuva podatke aplikacije u dokumentima.
// Dokument korisnika povezujemo sa nalogom preko UID-a.

// auth - registracija, prijava, odjava i upravljanje lozinkom.
// firestore - citanje i upis dokumenata u Cloud Firestore.

public class FirebaseAuthRepository implements AuthRepository {

    // Veze
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    // Constructor
    public FirebaseAuthRepository() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    // Registracija korisnika
    @Override
    public void register(String username, String email, String password,
                         RegisterCallback callback) {

        String usernameKey = username.trim().toLowerCase(Locale.ROOT);

        if (usernameKey.isEmpty() || usernameKey.contains("/")
                || usernameKey.equals(".") || usernameKey.equals("..")) {
            callback.onError("Enter a valid username.");
            return;
        }

        // Salje zahtec auth servisu da napravi nalog
        // On vraca FirebaseUser - objekat koji predstavlja nalog korisnika u aplikaciji
        // ima .getUid(), .getEmail(), .isEmailVerified()
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {

                    FirebaseUser user = result.getUser();

                    if (user == null) {
                        callback.onError("Unable to create user.");
                        return;
                    }

                    DocumentReference usernameRef = firestore
                            .collection("usernames")
                            .document(usernameKey);

                    DocumentReference userRef = firestore
                            .collection("users")
                            .document(user.getUid());

                    // Korisnicko ime
                    Map<String, Object> usernameData = new HashMap<>();
                    usernameData.put("uid", user.getUid());

                    // Korisnicki profil
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("uid", user.getUid());
                    userData.put("username", username.trim());
                    userData.put("usernameKey", usernameKey);
                    userData.put("email", email);
                    userData.put("biography", "");
                    userData.put("region", "");
                    userData.put("profileImageUrl", "");
                    userData.put("createdAt", FieldValue.serverTimestamp());

                    // Atomska rezervacija imena i kreiranje profila
                    firestore.runTransaction(transaction -> {

                        DocumentSnapshot existing = transaction.get(usernameRef);

                        if (existing.exists()) {
                            throw new FirebaseFirestoreException(
                                    "Username is already taken.",
                                    FirebaseFirestoreException.Code.ALREADY_EXISTS
                            );
                        }

                        transaction.set(usernameRef, usernameData);
                        transaction.set(userRef, userData);

                        return null;

                    }).addOnSuccessListener(unused -> {

                        // Slanje aktivacionog mejla
                        user.sendEmailVerification()
                                .addOnCompleteListener(emailTask -> {

                                    auth.signOut();

                                    callback.onSuccess(emailTask.isSuccessful());
                                });

                    }).addOnFailureListener(error -> {

                        // Ako Firestore upis ne uspe, ukloni novi Auth nalog
                        user.delete().addOnCompleteListener(deleteTask -> {

                            auth.signOut();

                            if (deleteTask.isSuccessful()) {
                                callback.onError(error.getMessage());
                            } else {
                                callback.onError(
                                        "Registration failed. The account could not "
                                                + "be cleaned up automatically."
                                );
                            }
                        });
                    });
                })
                .addOnFailureListener(error -> callback.onError(error.getMessage()));
    }


    @Override
    public void login(String email, String password,
                      AuthRepository.LoginCallback callback) {

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {

                    FirebaseUser user = result.getUser();

                    if (user == null) {
                        auth.signOut();
                        callback.onError("LOGIN_FAILED");
                        return;
                    }

                    // Osvezi podatak o potvrdi email adrese
                    user.reload().addOnCompleteListener(task -> {

                        if (!task.isSuccessful()) {
                            auth.signOut();
                            callback.onError("LOGIN_FAILED");
                        } else if (!user.isEmailVerified()) {
                            auth.signOut();
                            callback.onError("EMAIL_NOT_VERIFIED");
                        } else {
                            callback.onSuccess();
                        }
                    });
                })
                .addOnFailureListener(error -> {
                    callback.onError("LOGIN_FAILED");
                });
    }

    @Override
    public void sendPasswordReset(String email, OperationCallback callback) {
        if (!AuthValidator.isValidEmail(email)) {
            callback.onError("Enter a valid email address.");
            return;
        }

        auth.sendPasswordResetEmail(email.trim())
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(error ->
                        callback.onError("Unable to send password reset email."));
    }

    // Odjava korisnika
    @Override
    public void logout() {
        auth.signOut();
    }

}
