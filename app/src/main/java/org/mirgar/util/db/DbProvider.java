package org.mirgar.util.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.mirgar.Appeal;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by n.bibik on 07.06.2018.
 */

public class DbProvider {
    @Contract(pure = true)
    @Nullable
    public static DbProvider getInstance() {
        return instance;
    }

    private static DbProvider instance;

    private SQLiteOpenHelper itsHelper;

    private DbProvider(Context context) {
        itsHelper = new ItsDbHelper(context);
    }

    public static void init(Context context) {
        if(instance == null)
            instance = new DbProvider(context);
    }

    public long insertAppeal(ContentValues values) {
        SQLiteDatabase db = itsHelper.getWritableDatabase();
        db.beginTransaction();
        final long id = db.insert(DBContract.T_APPEALS.NAME, null, values);
        if (id != -1) db.setTransactionSuccessful();
        db.endTransaction();
        return id;
    }

    public long insertAppeal(Appeal appeal, boolean asDraft) {
        ContentValues values = makeContentValues(appeal, asDraft);
        final long id = insertAppeal(values);
        if(id == 0) throw new SQLException("Can not insert to " + DBContract.T_APPEALS.NAME);
        for (Appeal.Photo photo:
                appeal.photos) {
            insertAppealPhoto(id, photo);
        }
        return id;
    }

    public long insertAppeal(Appeal appeal) {
        return insertAppeal(appeal, false);
    }

    public void updateAppeal(long id, ContentValues values) {
        SQLiteDatabase db = itsHelper.getWritableDatabase();
        db.beginTransaction();
        boolean fail = db.update(DBContract.T_APPEALS.NAME, values, DBContract.T_APPEALS._ID + " = ?", new String[]{String.valueOf(id)}) == -1;
        if(!fail) db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void updateAppeal(Appeal appeal) {
        updateAppeal(appeal, false);
    }

    public void updateAppeal(Appeal appeal, boolean asDraft) {
        ContentValues values = makeContentValues(appeal, asDraft);
        updateAppeal(appeal.getLocalId(), values);
    }

    public void updateAppealPhotos(long id, Set<Appeal.Photo> photos) {
        SQLiteDatabase db = itsHelper.getReadableDatabase();
        Cursor photoNames = db.query(DBContract.T_APPEAL_PHOTOS.NAME,
                                     new String[]{DBContract.T_APPEAL_PHOTOS.COL_FILENAME},
                                     DBContract.T_APPEAL_PHOTOS.COL_APPEAL_ID + " = ?",
                                     new String[] {String.valueOf(id)},
                                     null,
                                     null,
                                     null);

        Set<String> dst = new HashSet<>(photoNames.getCount());
        while (photoNames.moveToNext())
            dst.add(photoNames.getString(0));
        Set<String> src = new HashSet<>(photos.size());
        for (Appeal.Photo photo: photos)
            src.add(photo.file.getAbsolutePath());

        for (String strDst: dst)
            if (!src.contains(strDst)) deleteAppealPhoto(strDst);
        for (Appeal.Photo photo: photos)
            if (!dst.contains(photo.file.getAbsolutePath())) insertAppealPhoto(id, photo);
        photoNames.close();
    }

    private void deleteAppealPhoto(String path) {
        SQLiteDatabase db = itsHelper.getWritableDatabase();
        db.beginTransaction();
        boolean fail = db.delete(DBContract.T_APPEAL_PHOTOS.NAME, DBContract.T_APPEAL_PHOTOS.COL_FILENAME + " = ?", new String[]{path}) == -1;
        if(!fail) db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void insertAppealPhoto(ContentValues values) {
        SQLiteDatabase db = itsHelper.getWritableDatabase();
        db.beginTransaction();
        boolean fail = db.insert(DBContract.T_APPEAL_PHOTOS.NAME, null, values) == -1;
        if(!fail) db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void insertAppealPhoto(final long id, Appeal.Photo photo) {
        ContentValues values = new ContentValues();
        values.put(DBContract.T_APPEAL_PHOTOS.COL_APPEAL_ID, id);
        values.put(DBContract.T_APPEAL_PHOTOS.COL_FILENAME, photo.file.getAbsolutePath());
        values.put(DBContract.T_APPEAL_PHOTOS.COL_LAT, String.valueOf(photo.latitude));
        values.put(DBContract.T_APPEAL_PHOTOS.COL_LONG, String.valueOf(photo.longitude));
        insertAppealPhoto(values);
    }

    private ContentValues makeContentValues(Appeal appeal, boolean asDraft) {
        ContentValues values = new ContentValues();
        values.put(DBContract.T_APPEALS.COL_DRAFT, asDraft);
        values.put(DBContract.T_APPEALS.COL_TITLE, appeal.title);
        values.put(DBContract.T_APPEALS.COL_DESCR, appeal.descr);
        values.put(DBContract.T_APPEALS.COL_CAT_ID, appeal.catId);
        values.put(DBContract.T_APPEALS.COL_ROOT_CAT_ID, appeal.rootCatId);
        return values;
    }

    private Appeal getAppeal(long id) {
        Appeal appeal = new Appeal();

        appeal.localId = id;

        SQLiteDatabase db = itsHelper.getReadableDatabase();
        Cursor query = db.query(
                DBContract.T_APPEALS.NAME,
                new String[]{
                        DBContract.T_APPEALS.COL_GLOBAL_ID,
                        DBContract.T_APPEALS.COL_ROOT_CAT_ID,
                        DBContract.T_APPEALS.COL_CAT_ID,
                        DBContract.T_APPEALS.COL_TITLE,
                        DBContract.T_APPEALS.COL_DESCR
                },
                DBContract.T_APPEALS._ID + " = ?",
                new String[] {String.valueOf(id)},
                null,
                null,
                null,
                "1"
        );

        query.moveToFirst();
        appeal.globalId = query.getInt(0);
        appeal.rootCatId = query.getInt(1);
        appeal.catId = query.getInt(2);
        appeal.title = query.getString(3);
        appeal.descr = query.getString(4);
        query.close();

        query = db.query(
                DBContract.T_APPEAL_PHOTOS.NAME,
                new String[] {
                        DBContract.T_APPEAL_PHOTOS.COL_FILENAME,
                        DBContract.T_APPEAL_PHOTOS.COL_LAT,
                        DBContract.T_APPEAL_PHOTOS.COL_LONG
                },
                DBContract.T_APPEAL_PHOTOS.COL_APPEAL_ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                DBContract.T_APPEAL_PHOTOS.COL_FILENAME
        );

        while (query.moveToNext()) {
            Appeal.Photo photo = new Appeal.Photo();
            photo.file = new File(query.getString(0));
            photo.latitude = Double.parseDouble(query.getString(1));
            photo.longitude = Double.parseDouble(query.getString(2));
            appeal.photos.add(photo);
        }

        return appeal;
    }
}
