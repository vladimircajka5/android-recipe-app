package com.siit.recipeapp.data.local;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public final class LocalMediaStore {
    private final Context context;

    public LocalMediaStore(Context context) {
        this.context = context.getApplicationContext();
    }

    public Uri copyToInternalStorage(Uri source, String extension) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        String safeExtension = extension == null || extension.trim().isEmpty()
                ? ".bin" : (extension.startsWith(".") ? extension : "." + extension);
        File directory = new File(context.getFilesDir(), "media");
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Unable to create local media directory");
        }
        File destination = new File(directory, UUID.randomUUID() + safeExtension);
        try (InputStream input = resolver.openInputStream(source);
             OutputStream output = new FileOutputStream(destination)) {
            if (input == null) {
                throw new IOException("Unable to open selected media");
            }
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
        }
        return Uri.fromFile(destination);
    }

    public boolean delete(Uri uri) {
        if (uri == null) return false;
        if ("file".equals(uri.getScheme())) {
            return new File(uri.getPath()).delete();
        }
        return false;
    }
}