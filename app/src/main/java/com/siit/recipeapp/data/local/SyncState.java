package com.siit.recipeapp.data.local;

// defines the synchronization status of the local data with Firestore
public enum SyncState {
    SYNCED,
    PENDING,
    FAILED
}
