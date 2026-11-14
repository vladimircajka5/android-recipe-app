package com.siit.recipeapp;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.siit.recipeapp.data.local.RecipeDatabase;
import com.siit.recipeapp.data.remote.FirestoreSyncManager;

public final class RecipeApplication extends Application {
    private FirestoreSyncManager syncManager;

    @Override
    public void onCreate() {
        super.onCreate();
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            connectivityManager.registerDefaultNetworkCallback(
                    new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(@NonNull Network network) {
                            syncSignedInUser();
                        }
                    });
        }
    }

    public void syncSignedInUser() {
        if (FirebaseApp.getApps(this).isEmpty()) return;
        if (syncManager == null) {
            syncManager = new FirestoreSyncManager(
                    this, RecipeDatabase.getInstance(this));
        }
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        syncManager.sync(userId, new FirestoreSyncManager.Callback() {
            @Override
            public void onComplete() {
                // Local Room data remains the source shown by the UI.
            }

            @Override
            public void onError(@NonNull Exception exception) {
                // A failed sync keeps PENDING rows for the next attempt.
            }
        });
    }
}