package com.siit.recipeapp.data.repository.firebase;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Source;
import com.siit.recipeapp.data.model.account.UserRole;
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
                    usernameData.put("email", user.getEmail());

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



// ===== Prijava =====

    @Override
    public void login(String identifier, String password,
                      LoginCallback callback) {

        String value = identifier.trim();

        // Prijava preko emaila
        if (value.contains("@")) {
            loginWithEmail(value, password, callback);
            return;
        }

        // Prijava preko korisnickog imena
        String usernameKey = value.toLowerCase(Locale.ROOT);

        firestore.collection("usernames")
                .document(usernameKey)
                .get()
                .addOnSuccessListener(document -> {

                    String email = document.getString("email");

                    if (email == null || email.isEmpty()) {
                        callback.onError("LOGIN_FAILED");
                        return;
                    }

                    loginWithEmail(email, password, callback);
                })
                .addOnFailureListener(error -> {
                    callback.onError("LOGIN_FAILED");
                });
    }


    // Prijava u Firebase pomocu emaila
    private void loginWithEmail(String email, String password,
                                LoginCallback callback) {

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



    // Slanje emaila za resetovanje lozinke
    @Override
    public void sendPasswordResetEmail(String email, PasswordCallback callback) {
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



// ===== Promena lozinke =====

    @Override
    public void changePassword(String currentPassword, String newPassword,
                               PasswordCallback callback) {

        FirebaseUser user = auth.getCurrentUser();

        // Provera da li je korisnik prijavljen
        if (user == null || user.getEmail() == null) {
            callback.onError("NO_USER");
            return;
        }

        // Priprema podataka za ponovnu autentifikaciju
        AuthCredential credential = EmailAuthProvider.getCredential(
                user.getEmail(), currentPassword
        );

        // Provera trenutne lozinke
        user.reauthenticate(credential)
                .addOnSuccessListener(unused -> {

                    // Postavljanje nove lozinke
                    user.updatePassword(newPassword)
                            .addOnSuccessListener(result -> {
                                callback.onSuccess();
                            })
                            .addOnFailureListener(error -> {
                                callback.onError("CHANGE_FAILED");
                            });
                })
                .addOnFailureListener(error -> {

                    // Neuspesna provera trenutne lozinke
                    if (error instanceof FirebaseAuthInvalidCredentialsException) {
                        callback.onError("WRONG_PASSWORD");
                    } else {
                        callback.onError("CHANGE_FAILED");
                    }
                });
    }

    // ===== Sesija =====

    // Provera postojece prijave
    @Override
    public boolean hasVerifiedSession() {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            return false;
        }

        return user.isEmailVerified();
    }




// ===== Administratorske uloge =====

    // Citanje uloge trenutno prijavljenog korisnika
    @Override
    public void getCurrentRole(RoleCallback callback) {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null || !user.isEmailVerified()) {
            callback.onError("NO_USER");
            return;
        }

        firestore.collection("roles")
                .document(user.getUid())
                .get(Source.SERVER)
                .addOnSuccessListener(document -> {

                    String role = document.getString("role");

                    if ("ADMIN".equals(role)) {
                        callback.onSuccess(UserRole.ADMIN);
                    } else {
                        callback.onSuccess(UserRole.USER);
                    }
                })
                .addOnFailureListener(error -> {
                    callback.onError("ROLE_FAILED");
                });
    }


    // Dodela administratorske uloge drugom korisniku
    @Override
    public void grantAdmin(String username, AdminCallback callback) {

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null || !currentUser.isEmailVerified()) {
            callback.onError("NO_USER");
            return;
        }

        String usernameKey = username.trim().toLowerCase(Locale.ROOT);

        if (usernameKey.isEmpty() || usernameKey.contains("/")
                || usernameKey.equals(".") || usernameKey.equals("..")) {
            callback.onError("INVALID_USERNAME");
            return;
        }

        // Osvezi Firebase token nakon potvrde emaila
        currentUser.getIdToken(true)
                .addOnSuccessListener(token -> {

                    // Pronalazenje korisnika preko username-a
                    firestore.collection("usernames")
                            .document(usernameKey)
                            .get(Source.SERVER)
                            .addOnSuccessListener(document -> {

                                String targetUid = document.getString("uid");

                                if (targetUid == null || targetUid.isEmpty()) {
                                    callback.onError("USER_NOT_FOUND");
                                    return;
                                }

                                if (targetUid.equals(currentUser.getUid())) {
                                    callback.onError("SELF_ASSIGNMENT");
                                    return;
                                }

                                // Provera da li korisnik vec ima ADMIN ulogu
                                DocumentReference roleRef = firestore
                                        .collection("roles")
                                        .document(targetUid);

                                roleRef.get(Source.SERVER)
                                        .addOnSuccessListener(roleDocument -> {

                                            if (roleDocument.exists()) {
                                                callback.onError("ALREADY_ADMIN");
                                                return;
                                            }

                                            Map<String, Object> roleData = new HashMap<>();
                                            roleData.put("role", "ADMIN");

                                            // Firestore pravila proveravaju pravo dodele
                                            roleRef.set(roleData)
                                                    .addOnSuccessListener(unused -> {
                                                        callback.onSuccess();
                                                    })
                                                    .addOnFailureListener(error -> {
                                                        callback.onError("GRANT_FAILED");
                                                    });
                                        })
                                        .addOnFailureListener(error -> {
                                            callback.onError("GRANT_FAILED");
                                        });
                            })
                            .addOnFailureListener(error -> {
                                callback.onError("GRANT_FAILED");
                            });
                })
                .addOnFailureListener(error -> {
                    callback.onError("GRANT_FAILED");
                });
    }




}
