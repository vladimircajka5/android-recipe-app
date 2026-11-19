package com.siit.recipeapp.data.remote;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserDataSession {
    private UserDataSession() {
    }

    public static String currentOwnerId(Context context) {
        if (FirebaseApp.getApps(context.getApplicationContext()).isEmpty()) {
            return null;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null || !user.isEmailVerified()) {
            return null;
        }

        return user.getUid();
    }
}
