package com.siit.recipeapp;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.siit.recipeapp.data.local.RecipeDatabase;
import com.siit.recipeapp.data.remote.FirestoreSyncManager;
import com.siit.recipeapp.data.remote.UserDataSession;

public final class RecipeApplication extends Application {

    private final Handler syncHandler =
            new Handler(Looper.getMainLooper());

    private FirestoreSyncManager syncManager;

    // Ova polja koristi samo glavna nit
    private boolean syncRunning;
    private boolean syncRequested;

    @Override
    public void onCreate() {
        super.onCreate();

        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(
                        Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            connectivityManager.registerDefaultNetworkCallback(
                    new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(@NonNull Network network) {
                            syncSignedInUser();
                        }
                    });
        }

        if (!FirebaseApp.getApps(this).isEmpty()) {
            FirebaseAuth.getInstance().addAuthStateListener(
                    auth -> syncSignedInUser());
        }
    }

    public void syncSignedInUser() {
        syncHandler.post(() -> {
            syncRequested = true;
            startSyncIfNeeded();
        });
    }

    private void startSyncIfNeeded() {
        if (syncRunning || !syncRequested) {
            return;
        }

        syncRequested = false;

        String userId = UserDataSession.currentOwnerId(this);

        if (userId == null) {
            return;
        }

        if (syncManager == null) {
            syncManager = new FirestoreSyncManager(
                    this, RecipeDatabase.getInstance(this));
        }

        syncRunning = true;

        try {
            syncManager.sync(userId, new FirestoreSyncManager.Callback() {
                @Override
                public void onComplete() {
                    finishSync(null);
                }

                @Override
                public void onError(@NonNull Exception exception) {
                    finishSync(exception);
                }
            });
        } catch (RuntimeException exception) {
            finishSync(exception);
        }
    }

    private void finishSync(Exception error) {
        syncHandler.post(() -> {
            syncRunning = false;

            if (error != null) {
                Log.e("RecipeSync", "Synchronization failed", error);
            }

            if (syncRequested) {
                startSyncIfNeeded();
            }
        });
    }
}