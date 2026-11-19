package com.siit.recipeapp.data.repository.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.siit.recipeapp.data.repository.AuthRepository;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
// Firebase Authentication - Cuva naloge, imejl, info da li je imejl potvrdjen...
// Cloud Firestorage - Cuva stvari vezane za nalog: ime, region, bio, recepte, favorite...
// Firebase auth ce da napravi nalog i dodeli mu UID, a store ce da cuva njegove podatke

// auth - koristim za registraciju, prijavu, odjavu i upravljanje lozinke.
// firestoe - za citanje i pisanje dokumenata o korisniku
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

}
