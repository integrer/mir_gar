package org.mirgar.util.db;

import android.provider.BaseColumns;

/**
 * Created by n.bibik on 01.06.2018.
 */

final class DBContract {
    static final String FileName = "data.db";
    static final int Version = 1;

    static final class T_APPEALS implements BaseColumns {
        static final String NAME = "APPEALS";

        static final String COL_DRAFT = "DRAFT";
        static final String COL_GLOBAL_ID = "GLOBAL_ID";
        static final String COL_TITLE = "TITLE";
        static final String COL_DESCR = "DESCRIPTION";
        static final String COL_ROOT_CAT_ID = "ROOT_CAT_ID";
        static final String COL_CAT_ID = "CAT_ID";

        static final String SQL_CREATE =
                "CREATE TABLE " + NAME + " (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DRAFT + " INTEGER DEFAULT 0, " +
                COL_GLOBAL_ID + " INTEGER, " +
                COL_TITLE + " TEXT, " +
                COL_DESCR + " TEXT, " +
                COL_ROOT_CAT_ID + " INTEGER, " +
                COL_CAT_ID + " INTEGER, " +
                "UNIQUE (" + _ID + ", " + COL_DRAFT + ") ON CONFLICT FAIL);";

//        static final String UPD_2 =
//                        "BEGIN TRANSACTION;" +
//                        "CREATE TEMPORARY TABLE " +
//                        "CREATE TABLE IF NOT EXISTS _" + NAME + " (" +
//                                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                                COL_GLOBAL_ID + " INTEGER, " +
//                                COL_TITLE + " TEXT, " +
//                                COL_DESCR + " TEXT, " +
//                                COL_ROOT_CAT_ID + " INTEGER, " +
//                                COL_CAT_ID + " INTEGER);" +
//                        "INSERT INTO _" + NAME + " SELECT * FROM " + NAME + ";" +
//                        "DROP TABLE " + NAME + ";" +
//                        "CREATE TABLE " + NAME + " (" +
//                                _ID + " INTEGER AUTOINCREMENT, " +
//                                COL_DRAFT + " INTEGER 0, " +
//                                COL_GLOBAL_ID + " INTEGER, " +
//                                COL_TITLE + " TEXT, " +
//                                COL_DESCR + " TEXT, " +
//                                COL_ROOT_CAT_ID + " INTEGER, " +
//                                COL_CAT_ID + " INTEGER" +
//                                "PRIMARY KEY(" + _ID + ", " + COL_DRAFT + ") ON CONFLICT FAIL);" +
//                        "INSERT INTO " + NAME + " (" +
//                                _ID + ", " +
//                                COL_GLOBAL_ID + ", " +
//                                COL_TITLE + ", " +
//                                COL_DESCR + ", " +
//                                COL_ROOT_CAT_ID + ", " +
//                                COL_CAT_ID + ")" +
//                        "SELECT * FROM _" + NAME + ";" +
//                        "DROP TABLE _" + NAME + ";";
    }

    static final class T_APPEAL_PHOTOS implements BaseColumns {
        static final String NAME = "APPEAL_PHOTOS";

        static final String COL_APPEAL_ID = "APPEAL_ID";
        static final String COL_FILENAME = "FILENAME";
        static final String COL_LAT = "LATITUDE";
        static final String COL_LONG = "LONGITUDE";

        static final String SQL_CREATE =
                String.format("CREATE TABLE IF NOT EXISTS %s (" +
                                      "%s INTEGER NOT NULL, " +
                                      "%s TEXT NOT NULL, " +
                                      "%s TEXT NOT NULL, " +
                                      "%s TEXT NOT NULL, " +
                                      "PRIMARY KEY (%s, %s) ON CONFLICT FAIL);",
                        NAME, COL_APPEAL_ID, COL_FILENAME, COL_LAT, COL_LONG, COL_APPEAL_ID, COL_FILENAME);
    }

    static final class T_CATS implements BaseColumns {
        static final String NAME = "CATS";

        static final String COL_PARENT_ID = "PARENT_ID";
        static final String COL_NAME = "NAME";
        static final String COL_ID = "ID";

        static final String SQL_CREATE =
                String.format("CREATE TABLE IF NOT EXISTS %s (" +
                                "%s INTEGER NOT NULL, " +
                                "%s TEXT NOT NULL, " +
                                "%s INTEGER NOT NULL);",
                        NAME, COL_PARENT_ID, COL_NAME, COL_ID);
    }
}
