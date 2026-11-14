package com.siit.recipeapp.data.local;

import android.net.Uri;

public final class YouTubeLinks {
    private YouTubeLinks() {}

    public static boolean isValid(String value) {
        if (value == null || value.trim().isEmpty()) return false;
        Uri uri = Uri.parse(value.trim());
        String host = uri.getHost();
        return host != null && (host.equalsIgnoreCase("youtube.com")
                || host.equalsIgnoreCase("www.youtube.com")
                || host.equalsIgnoreCase("youtu.be")
                || host.equalsIgnoreCase("www.youtu.be"));
    }
}