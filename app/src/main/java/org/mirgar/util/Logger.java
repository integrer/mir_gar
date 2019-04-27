package org.mirgar.util;

import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.mirgar.BuildConfig;

public class Logger {
    private static final String TAG = "org.mirgar";

    private static StackTraceElement getCallerSTE() {
        return Thread.currentThread().getStackTrace()[4];
    }

    // Verbose - 2
    public static void v(String msg) {
        if (BuildConfig.USE_LOG) {
            StackTraceElement ste = getCallerSTE();
            String tag = makeTag(ste.getClassName());
            Log.v(tag, msg + "\nat " + ste.toString());
        }
    }

    // Debug   - 3
    public static void d(String msg) {
        if (BuildConfig.USE_LOG) {
            StackTraceElement ste = getCallerSTE();
            String tag = makeTag(ste.getClassName());
            Log.d(tag, msg + "\nat " + ste.toString());
        }
    }

    // Info    - 4
    public static void i(String msg) {
        if (BuildConfig.USE_LOG) {
            StackTraceElement ste = getCallerSTE();
            String tag = makeTag(ste.getClassName());
            Log.i(tag, msg + "\nat " + ste.toString());
        }
    }

    // Warning - 5
    public static void w(String msg) {
        if (BuildConfig.USE_LOG) {
            StackTraceElement ste = getCallerSTE();
            String tag = makeTag(ste.getClassName());
            Log.w(tag, msg + "\nat " + ste.toString());
        }
    }

    // Error   - 6
    public static void e(String msg, Throwable tr) {
        if (BuildConfig.USE_LOG) {
            String tag = makeTag(getCallerSTE().getClassName());
            if (msg != null) {
                if (tr == null)
                    Log.e(tag, msg + "\nat " + getCallerSTE().toString());
                else Log.e(tag, msg, tr);
            } else if (tr != null) Log.e(tag, tr.getMessage(), tr);
            else Log.e(tag, "at " + getCallerSTE().toString());
        }
    }

    public static void e(String msg) {
        if (BuildConfig.USE_LOG)
            e(msg + "\nat " + getCallerSTE().toString(), null);
    }

    public static void e(Throwable tr) {
        if (BuildConfig.USE_LOG) e(null, tr);
    }

    // Assert  - 7
    public static void wtf(String msg, Throwable tr) {
        if (BuildConfig.USE_LOG) {
            String tag = makeTag(getCallerSTE().getClassName());
            if (msg != null) {
                if (tr == null)
                    Log.wtf(tag, msg + "\nat " + getCallerSTE().toString());
                else Log.wtf(tag, msg, tr);
            } else if (tr != null) Log.wtf(tag, tr);
            else Log.wtf(tag, "at " + getCallerSTE().toString());
        }
    }

    public static void wtf(String msg) {
        if (BuildConfig.USE_LOG) wtf(msg + "\nat " + getCallerSTE().toString(), null);
    }

    public static void wtf(Throwable tr) {
        if (BuildConfig.USE_LOG) wtf(null, tr);
    }

    private static String makeTag(@NotNull String className) {
        if (className.contains(TAG)) {
            String[] splited = className.split(TAG, 2);
            className = "";
            for (String subStr : splited) {
                className = className.concat(subStr);
            }
        }
        return className;
    }
}
