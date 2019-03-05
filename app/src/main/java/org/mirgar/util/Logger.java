package org.mirgar.util;

import android.util.Log;

import org.donampa.nbibik.dipl.BuildConfig;

public class Logger {
    private static final String TAG = "org.donampa.nbibik.dipl";

    // Verbose - 2
    public static void v(Class senderClass, String msg) {
        if (BuildConfig.USE_LOG) {
            String tag = makeTag(senderClass);
            Log.v(tag, msg);
        }
    }

    // Debug   - 3
    public static void d(Class senderClass, String msg) {
        if (BuildConfig.USE_LOG) {
            String tag = makeTag(senderClass);
            Log.d(tag, msg);
        }
    }

    // Info    - 4
    public static void i(Class senderClass, String msg) {
        if (BuildConfig.USE_LOG) {
            String tag = makeTag(senderClass);
            Log.i(tag, msg);
        }
    }

    // Warning - 5
    public static void w(Class senderClass, String msg) {
        if (BuildConfig.USE_LOG) {
            String tag = makeTag(senderClass);
            Log.w(tag, msg);
        }
    }

    // Error   - 6
    public static void e(Class senderClass, String msg, Throwable tr) {
        if (BuildConfig.USE_LOG) {
            String tag = makeTag(senderClass);
            if (msg != null) {
                if (tr == null)
                    Log.e(tag, msg);
                else Log.e(tag, msg, tr);
            } else if (tr != null) Log.e(tag, tr.getMessage(), tr);
            else Log.e(tag, "");
        }
    }

    public static void e(Class senderClass, String msg) {
        if (BuildConfig.USE_LOG) e(senderClass, msg, null);
    }

    public static void e(Class senderClass, Throwable tr) {
        if (BuildConfig.USE_LOG) e(senderClass, null, tr);
    }

    // Assert  - 7
    public static void wtf(Class senderClass, String msg, Throwable tr) {
        if (BuildConfig.USE_LOG) {
            String tag = makeTag(senderClass);
            if (msg != null) {
                if (tr == null)
                    Log.wtf(tag, msg);
                else Log.wtf(tag, msg, tr);
            } else if (tr != null) Log.wtf(tag, tr);
            else Log.wtf(tag, "");
        }
    }

    public static void wtf(Class senderClass, String msg) {
        if (BuildConfig.USE_LOG) wtf(senderClass, msg, null);
    }

    public static void wtf(Class senderClass, Throwable tr) {
        if (BuildConfig.USE_LOG) wtf(senderClass, null, tr);
    }

    private static String makeTag(Class cl) {
        String tag = cl.getName();
        if (tag.contains(TAG)) {
            String[] splited = tag.split(TAG, 2);
            tag = "";
            for (String subStr : splited) {
                tag = tag.concat(subStr);
            }
        }
        return tag;
    }
}
