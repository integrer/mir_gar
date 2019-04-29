package org.mirgar.util;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import org.jetbrains.annotations.Nullable;
import org.mirgar.util.exceptions.ContextUnreachableException;

import java.io.File;
import java.io.IOException;

import static android.os.Build.VERSION.SDK_INT;
import static org.mirgar.BuildConfig.APPLICATION_ID;

public class Files {
    public static Uri fromFile(File file) throws ContextUnreachableException {
        if (SDK_INT >= 24) {
            @Nullable
            Context ctx = com.activeandroid.Cache.getContext();
            if (ctx != null) {
                return FileProvider.getUriForFile(ctx,
                        APPLICATION_ID + ".file_provider", file);
            } else throw new ContextUnreachableException();
        } else
            return Uri.fromFile(file);
    }

    public static boolean inDataDirectory(File file) throws IOException, ContextUnreachableException {
        @Nullable
        Context ctx = com.activeandroid.Cache.getContext();
        if (ctx != null) {
            String dataDirPath = ctx.getFilesDir().getCanonicalPath() + "/data/";
            return file.getCanonicalPath().startsWith(dataDirPath);
        } else throw new ContextUnreachableException();
    }

}
