package org.mirgar;

import org.mirgar.util.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by n.bibik on 18.06.2018.
 */

public class Appeal {
    private static final String LOCAL_ID_JSON = "local_id";
    private static final String GLOBAL_ID_JSON = "global_id";
    private static final String ROOT_CAT_ID_JSON = "root_cat_id";
    private static final String CAT_ID_JSON = "cat_id";
    private static final String TITLE_JSON = "title";
    private static final String DESCR_JSON = "descr";
    private static final String ADDRESS_JSON = "address";
    private static final String PHOTOS_JSON = "photos";

    public long localId;
    public int globalId;
    public int rootCatId;
    public int catId;
    public String title;
    public String descr;
    public String address;
    public Set<Photo> photos;

    public Appeal() {
        localId = 0;
        globalId = 0;
        rootCatId = 0;
        catId = 0;
        title = "";
        descr = "";
        address = "";
        photos = new LinkedHashSet<>();
    }

    public Appeal(String jsonStr) {
        this();
        if (!jsonStr.equals("")) {
            try {
                JSONObject json = new JSONObject(jsonStr);
                localId = json.getLong(LOCAL_ID_JSON);
                globalId = json.getInt(GLOBAL_ID_JSON);
                rootCatId = json.getInt(ROOT_CAT_ID_JSON);
                catId = json.getInt(CAT_ID_JSON);
                title = json.getString(TITLE_JSON);
                descr = json.getString(DESCR_JSON);
                address = json.getString(ADDRESS_JSON);
                JSONArray jsonArray = json.getJSONArray(PHOTOS_JSON);
                for (int i = 0; i < jsonArray.length(); i++) {
                    Photo photo = new Photo();
                    JSONObject photoJson = jsonArray.getJSONObject(i);
                    photo.file = new File(photoJson.getString(Photo.FILE_PATH_JSON));
                    photo.longitude = photoJson.getDouble(Photo.LONGITUDE_JSON);
                    photo.latitude = photoJson.getDouble(Photo.LATITUDE_JSON);
                    photos.add(photo);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Photo {
        private static final String FILE_PATH_JSON = "file_path";
        private static final String LATITUDE_JSON = "latitude";
        private static final String LONGITUDE_JSON = "longitude";

        public File file;
        public double latitude;
        public double longitude;
    }

    public JSONObject toLocalJson() {
        JSONObject result = new JSONObject();
        try {
            JSONArray photoJsonArray = new JSONArray();
            for(Photo photo : photos) {
                JSONObject photoJson = new JSONObject();
                photoJson
                        .put(Photo.FILE_PATH_JSON, photo.file.getAbsolutePath())
                        .put(Photo.LATITUDE_JSON, photo.latitude)
                        .put(Photo.LONGITUDE_JSON, photo.longitude);
                photoJsonArray.put(photoJson);
            }
            result
                    .put(LOCAL_ID_JSON, localId)
                    .put(GLOBAL_ID_JSON, globalId)
                    .put(ROOT_CAT_ID_JSON, rootCatId)
                    .put(CAT_ID_JSON, catId)
                    .put(TITLE_JSON, title)
                    .put(DESCR_JSON, descr)
                    .put(ADDRESS_JSON, address)
                    .put(PHOTOS_JSON, photoJsonArray);
        } catch (JSONException ex) {
            Logger.wtf(getClass(), ex);
        }
        return result;
    }

    public void printJson() {
        try {
            Logger.v(getClass(), toLocalJson().toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String toLocalJsonStr() {
        return toLocalJson().toString();
    }
}
