package org.mirgar.util.tasks;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ArrayAdapter;

import org.mirgar.util.Logger;
import org.mirgar.util.exceptions.UnsolvableJSONException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Created by n.bibik on 21.06.2018.
 */

public final class GetAddressesTask extends AsyncTask<Double, Void, Set<String>> {
    private final Runnable postRunnable;
    private ArrayAdapter<String> adapter;
    private Map<String, String> outerMap;

    public GetAddressesTask(@NonNull ArrayAdapter<String> adapter, @NonNull Map<String, String> outerMap, @NonNull Runnable postRunnable) {
        this.adapter = adapter;
        this.postRunnable = postRunnable;
        this.outerMap = outerMap;
    }

    @Nullable
    @Override
    protected Set<String> doInBackground(Double... latlng) {
        URL urlUserNative;
        URL urlSystem;

        HttpURLConnection userNativeConnection = null;
        HttpURLConnection systemLangConnection = null;

        final String key = "AIzaSyB1wG0erDrxkxVUNxIkQteJbZkh6g0xVeg";

        JSONObject userNativeResponseData;
        JSONObject systemResponseData;

        final short maxTryes = 32;

        for (int counter = 0; counter < maxTryes; counter++) {
            try {
                urlUserNative = new URL("http://maps.googleapis.com/maps/api/geocode/json?latlng=" + String.valueOf(latlng[0]) + "," + String.valueOf(latlng[1]) + "&sensor=false&language=" + Locale.getDefault().getLanguage());
                final boolean russian = Locale.getDefault().getLanguage().equals("ru");


                userNativeConnection = (HttpURLConnection) urlUserNative.openConnection();

                try (InputStream in = userNativeConnection.getInputStream()) {
                    try (Scanner s = new Scanner(in).useDelimiter("\\A")) {
                        String text = s.next();
                        userNativeResponseData = new JSONObject(text);
                    }
                }

                String errStr;
                if (!(errStr = userNativeResponseData.optString("error_message")).equals(""))
                    throw new UnsolvableJSONException(errStr);


                JSONArray nativeLangAddressArr = userNativeResponseData.getJSONArray("results");

                final int nativeLangAddressArrLength = nativeLangAddressArr.length();


                if(!russian) {
                    urlSystem = new URL("http://maps.googleapis.com/maps/api/geocode/json?latlng=" + String.valueOf(latlng[0]) + "," + String.valueOf(latlng[1]) + "&sensor=false&language=ru-ru");
                    systemLangConnection = (HttpURLConnection) urlSystem.openConnection();
                    try (InputStream in = systemLangConnection.getInputStream()) {
                        try (Scanner s = new Scanner(in).useDelimiter("\\A")) {
                            String text = s.next();
                            systemResponseData = new JSONObject(text);
                        }
                    }
                    JSONArray systemLangAddressArr = systemResponseData.getJSONArray("results");
                    final int systemLangAddressArrLength = systemLangAddressArr.length();
                    Map<String, String> nativeItems = new HashMap<>(nativeLangAddressArrLength);
                    Map<String, String> systemItems = new HashMap<>(systemLangAddressArrLength);

                    for (int i = 0; i < nativeLangAddressArrLength; i++) {
                        JSONObject item = nativeLangAddressArr.getJSONObject(i);
                        try {
                            nativeItems.put(item.getString("place_id"), item.getString("formatted_address"));
                        } catch (JSONException ex) {
                            Logger.wtf(getClass(), "Unexpected error on json data parsing. Json code: \n" + item.toString(2), ex);
                        }
                    }

                    for (int i = 0; i < systemLangAddressArrLength; i++) {
                        JSONObject item = systemLangAddressArr.getJSONObject(i);
                        try {
                            systemItems.put(item.getString("place_id"), item.getString("formatted_address"));
                        } catch (JSONException ex) {
                            Logger.wtf(getClass(), "Unexpected error on json data parsing. Json code: \n" + item.toString(2), ex);
                        }
                    }

                    Set<String> overlapKeySet;

                    (overlapKeySet = nativeItems.keySet()).retainAll(systemItems.keySet());
                    for (String olpKey : overlapKeySet) {
                        outerMap.put(nativeItems.get(olpKey), systemItems.get(olpKey));
                    }

                    Set<String> diffKeySet;

                    (diffKeySet = nativeItems.keySet()).removeAll(overlapKeySet);
                    for (String diffKey : diffKeySet)
                        outerMap.put(nativeItems.get(diffKey), nativeItems.get(diffKey));

                    (diffKeySet = systemItems.keySet()).removeAll(overlapKeySet);
                    for (String diffKey : diffKeySet) {
                        outerMap.put(systemItems.get(diffKey), systemItems.get(diffKey));
                    }
                } else
                    for (int i = 0; i < nativeLangAddressArrLength; i++) {
                        String addr = nativeLangAddressArr.getJSONObject(i).getString("formatted_address");
                        outerMap.put(addr, addr);
                    }
                return outerMap.keySet();
            } catch (UnsolvableJSONException ex) {
                Logger.wtf(getClass(), ex.getMessage(), ex);
                userNativeConnection.disconnect();
                return null;
            } catch (IOException | JSONException ex) {
                ex.printStackTrace();
                if(userNativeConnection != null) userNativeConnection.disconnect();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Set<String> addressSet) {
        if(addressSet != null) {
            adapter.clear();
            adapter.addAll(addressSet);
            adapter.notifyDataSetChanged();
            postRunnable.run();
        }
        super.onPostExecute(addressSet);
    }
}
