package org.donampa.nbibik.dipl.util;

/**
 * Created by n.bibik on 08.05.2018.
 */

public class StringTable {
    private static final StringTable ourInstance = new StringTable();

    public static final String IntentDataCuriosity = "curiosity";

    public static StringTable getInstance() {
        return ourInstance;
    }

    private StringTable() {
    }

}
