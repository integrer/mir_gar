package org.donampa.nbibik.dipl;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

class Util {
    private static final String PREF_ID = "org.donampa.nbibik.dipl.prefs";
    static final String FULLUSERNAME_FIELD = "username_field";
    private static SharedPreferences prefs = null;

    public Util() {
        super();
//        for (Errs entry:Errs.values()) {
//            if (!strErrors.containsKey(entry) && entry != Errs.NoErr)
                // TODO: Здесь должно выбрасываться исключение
//        }
//        for (Prefs entry:Prefs.values()) {
//            if (!prefNames.containsKey(entry))
            // TODO: Здесь должно выбрасываться исключение
//        }
    }

    static void Init(Context appContext) {
        if (prefs == null)
            prefs = appContext.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE);
    }

    enum Errs {
        EmailNExist,
        PwdIncorrect,
        IO,
        MalformedUrl,
        NoErr
    }

    private static Map<String, Errs> strErrors = new HashMap<>();
    static {
        strErrors.put("[EMAIL_DOES_NOT_EXISTS]",    Errs.EmailNExist);
        strErrors.put("[PASSWORD_INCORRECT]",       Errs.PwdIncorrect);
        strErrors.put("[IO_ERROR]",                 Errs.IO);
        strErrors.put("[MALFORMED_URL]",            Errs.MalformedUrl);
    }

    static Errs GetErr(String strErr) {
        if(!strErrors.containsKey(strErr))
            return Errs.NoErr;
        return strErrors.get(strErr);
    }

    enum Prefs {
        FULLNAME,
        LOGIN,
        EMAIL,
        COOKIE
    }

    private static Map<Prefs, String> prefNames = new HashMap<>();
    static {
        prefNames.put(Prefs.FULLNAME, "fullname");
        prefNames.put(Prefs.LOGIN, "login");
        prefNames.put(Prefs.EMAIL, "email");
        prefNames.put(Prefs.COOKIE, "cookie");
    }

    static String GetPref(Prefs id, String val) { return prefs.getString(prefNames.get(id), val); }

    static void SetPref(Prefs id, String val) {
        SharedPreferences.Editor e = prefs.edit();
        e.putString(prefNames.get(id), val);
        e.apply();
    }

    static class Logger {
        static final String TAG = "org.donampa.nbibik.dipl";

        static void v (String msg) { Log.v(TAG, msg); }

        static void i (String msg) { Log.i(TAG, msg); }

        static void w (String msg) { Log.w(TAG, msg); }

        static void e (String msg) { Log.e(TAG, msg); }
    }
}