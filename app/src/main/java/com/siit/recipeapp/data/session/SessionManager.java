package com.siit.recipeapp.data.session;

import android.content.Context;
import android.content.SharedPreferences;
import com.siit.recipeapp.data.model.account.UserRole;
import com.siit.recipeapp.data.model.account.User;

public final class SessionManager {
    private static final String PREFS = "recipe_session";
    private static final String KEY_USER = "user_id";
    private static final String KEY_ROLE = "role";
    private SessionManager() { }
    public static void signIn(Context context, String userId, UserRole role) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(KEY_USER, userId).putString(KEY_ROLE, role.name()).apply();
    }
    public static void signIn(Context context, User user) {
        if (user == null) throw new IllegalArgumentException("User is required");
        signIn(context, user.getUsername(), user.getRole());
    }
    public static void signOut(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply();
    }
    public static String getUserId(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_USER, null);
    }
    public static UserRole getRole(Context context) {
        String role = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_ROLE, UserRole.USER.name());
        try { return UserRole.valueOf(role); } catch (IllegalArgumentException exception) { return UserRole.USER; }
    }
    public static boolean isAdmin(Context context) { return getRole(context) == UserRole.ADMIN; }
}
