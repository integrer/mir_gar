package org.donampa.nbibik.dipl.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Created by n.bibik on 08.05.2018.
 */

public class PrefManager {
    private static final String PREF_ID = "org.donampa.nbibik.dipl.prefs";
    private SharedPreferences prefs;

    public enum Prefs {
        FULLNAME,
        LOGIN,
        EMAIL,
        COOKIE,
        Curiosity
    }

    public PrefManager(Context context) {
        prefs = context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE);
    }

    public boolean Get(Prefs id, boolean val) {
        return prefs.getBoolean(id.name(), val);
    }

    public float Get(Prefs id, float val) {
        return prefs.getFloat(id.name(), val);
    }

    public int Get(Prefs id, int val) {
        return prefs.getInt(id.name(), val);
    }

    public long Get(Prefs id, long val) {
        return prefs.getLong(id.name(), val);
    }

    public String Get(Prefs id, @Nullable String val) {
        return prefs.getString(id.name(), val);
    }

    public Set<String> Get(Prefs id, @Nullable Set<String> val) {
        return prefs.getStringSet(id.name(), val);
    }

    public boolean Set(Prefs id, boolean val) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(id.name(), val);
        return editor.commit();
    }

    public boolean Set(Prefs id, float val) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(id.name(), val);
        return editor.commit();
    }

    public boolean Set(Prefs id, int val) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(id.name(), val);
        return editor.commit();
    }

    public boolean Set(Prefs id, long val) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(id.name(), val);
        return editor.commit();
    }

    public boolean Set(Prefs id, String val) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(id.name(), val);
        return editor.commit();
    }

    public boolean Set(Prefs id, Set<String> val) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(id.name(), val);
        return editor.commit();
    }
}
