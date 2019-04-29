package org.mirgar.util.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by n.bibik on 07.06.2018.
 */
class ItsDbHelper extends SQLiteOpenHelper {
    ItsDbHelper(Context context) {
        super(context, DBContract.FileName, null, DBContract.Version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        db.execSQL(DBContract.T_APPEALS.SQL_CREATE);
        db.execSQL(DBContract.T_APPEAL_PHOTOS.SQL_CREATE);
        db.execSQL(DBContract.T_CATS.SQL_CREATE);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
