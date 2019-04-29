package org.mirgar.util;

/**
 * Created by n.bibik on 18.06.2018.
 */

public class FileManager {
    private FileManager() {

    }

    private static FileManager instance;

    public static FileManager getInstance() {
        if(instance == null) {
            instance = new FileManager();
        }
        return instance;
    }
}
