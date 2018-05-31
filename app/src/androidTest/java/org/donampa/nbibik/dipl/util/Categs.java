package org.donampa.nbibik.dipl.util;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.ArrayMap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class Categs {
    private ArrayMap<Integer, ArrayMap<String, Integer>> catCild = new ArrayMap<>();
    private ArrayMap<Integer, String> catParent = new ArrayMap<>();
    private ArrayMap<Integer, Bitmap> catBmp = new ArrayMap<>();
    private fetchCatsTask task;

    public Categs() {
        task = new fetchCatsTask();
        task.execute((Void) null);
    }

    private class CatContainer {
        public ArrayMap<Integer, ArrayMap<String, Integer>> catCild = new ArrayMap<>();
        public ArrayMap<Integer, String> catParent = new ArrayMap<>();
        public ArrayMap<Integer, Bitmap> catBmp = new ArrayMap<>();
    }

    private class fetchCatsTask extends AsyncTask<Void, Void, CatContainer> {
        @Override
        protected CatContainer doInBackground(Void... voids) {
            String linkToCatlist = "https://mirgar.ga/getCat.php";
            CatContainer catContainer = new CatContainer();
            try {
                URLConnection conn = (new URL(linkToCatlist)).openConnection();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                conn.getInputStream()
                        )
                );

                String line;

                Integer catId = null;
                String catName = null;
                int parentId;

                int i = 1;
                while ((line = reader.readLine()) != null) {
                    switch (3 % i) {
                        case 1:
                            catId = Integer.parseInt(line);
                            break;
                        case 2:
                            catName = line;
                            break;
                        case 0:
                            parentId = Integer.parseInt(line);
                            if (parentId != 0) {
                                if (catContainer.catCild.containsKey(parentId)) {
                                    (catContainer.catCild.get(parentId)).put(catName, catId);
                                } else {
                                    catContainer.catCild.put(parentId, new ArrayMap<>());
                                    (catContainer.catCild.get(parentId)).put(catName, catId);
                                }
                            } else {
                                if (!catContainer.catCild.containsKey(parentId)) {
                                    catContainer.catCild.put(catId, new ArrayMap<>());
                                }
                                catContainer.catParent.put(catId, catName);
                            }
                    }
                    i++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

//            String pictureAddr = "";
//            try {
//                for (int i : catContainer.catParent.keySet()) {
//                    InputStream inpStream = new URL(pictureAddr + i + ".swg").openStream();
//                    catContainer.catBmp.put(i, BitmapFactory.decodeStream(inpStream));
//                }
//            } catch (Exception e) {
//                Logger.e(getClass(), e);
//            }

            return catContainer;
        }

        @Override
        protected void onPostExecute(CatContainer container) {
            catCild = container.catCild;
            catParent = container.catParent;
            catBmp = container.catBmp;
            task = null;
        }
    }
}
