package org.donampa.nbibik.dipl.util;

import android.os.AsyncTask;
import android.util.ArrayMap;

import org.donampa.nbibik.dipl.MainActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class Categs {
    private ArrayMap<Integer, ArrayMap<String, Integer>> mapCat = new ArrayMap<>();
    private ArrayMap<String, Integer> mapRootCat = new ArrayMap<>();
    //    private ArrayMap<Integer, Bitmap> mapRootCatPics = new ArrayMap<>();
    private loadCatsTask task;
    private MainActivity context;

    public String getRootCatName(int rootCatId) {
        Set<Map.Entry<String, Integer>> entrys = mapRootCat.entrySet();
        for (Map.Entry<String, Integer> entry : entrys)
            if (entry.getValue() == rootCatId)
                return entry.getKey();
        return null;
    }

    public String getCatName(String rootCatName, int catId) {
        ArrayMap<String, Integer> mapRootCat = mapCat.get(getRootCatId(rootCatName));
        Set<Map.Entry<String, Integer>> entrys = mapRootCat.entrySet();
        for (Map.Entry<String, Integer> entry : entrys)
            if (entry.getValue() == catId)
                return entry.getKey();
        return null;
    }

    public interface OnFinishLoadCategsListener {
        void onFinishLoadCategs(boolean successfully);
    }

    public void addListener(OnFinishLoadCategsListener listener) {
        if (task != null) {
            task.listenersQueue.add(listener);
        }
    }

    private Categs(boolean startLoadTask, MainActivity context) {
        if (startLoadTask) {
            task = new loadCatsTask(this);
            task.execute((Void) null);
        }
        this.context = context;
    }

    public Categs(MainActivity context) {
        this(true, context);
    }

    public Categs() {
        this(false, null);
    }

    public String[] getCats(int idParentCat) {
        if (idParentCat == 0)
            return mapRootCat.keySet().toArray(new String[0]);
        else
            return mapCat.get(idParentCat).keySet().toArray(new String[0]);
    }

    public int getCatId(int keyPar, String key) {
        if (mapCat.containsKey(keyPar)) {
            ArrayMap<String, Integer> map = mapCat.get(keyPar);
            if (map.containsKey(key))
                return map.get(key);
        }
        return 0;
    }

    public int getRootCatId(String key) {
        return mapRootCat.get(key);
    }

//    public Bitmap getBmp(String key) {
//        return mapRootCatPics.get(getRootCatId(key));
//    }

    public boolean isTaskRunning() {
        return task != null;
    }

    public boolean ready = false;

    public int getIconId(int categoryId) {
        return context.getResources().getIdentifier("cat_icon" + categoryId, "drawable", context.getPackageName());
    }

    public int getIconId(String categoryName) {
        if (mapRootCat.containsKey(categoryName))
            return getIconId(getRootCatId(categoryName));
        else return 0;
    }

    private class loadCatsTask extends AsyncTask<Void, Void, Categs> {
        Categs context;
        ArrayList<OnFinishLoadCategsListener> listenersQueue = new ArrayList<>();
        boolean successfully = false;

        public loadCatsTask(Categs context) {
            super();
            this.context = context;
        }

        @Override
        protected Categs doInBackground(Void... voids) {
            Logger.i(getClass(), "loadCatsTask started successful.");
            Categs loadedCategs = new Categs();
            String linkToCatlist = "https://mirgar.ga/getCat.php";
            try {
                URLConnection conn = (new URL(linkToCatlist)).openConnection();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                conn.getInputStream()
                        )
                );

                String line;

                Integer catId;
                String catName;
                int parentId;

                while ((line = reader.readLine()) != null) {
                    catId = Integer.parseInt(line);
                    if ((line = reader.readLine()) != null)
                        catName = line;
                    else throw new IOException("Unexpected end of file.");
                    if ((line = reader.readLine()) != null)
                        parentId = Integer.parseInt(line);
                    else throw new IOException("Unexpected end of file.");
                    if (parentId != 0) {
                        if (loadedCategs.mapCat.containsKey(parentId)) {
                            (loadedCategs.mapCat.get(parentId)).put(catName, catId);
                        } else {
                            loadedCategs.mapCat.put(parentId, new ArrayMap<String, Integer>());
                        }
                    } else {
                        if (!loadedCategs.mapCat.containsKey(parentId)) {
                            loadedCategs.mapCat.put(catId, new ArrayMap<String, Integer>());
                        }
                        loadedCategs.mapRootCat.put(catName, catId);
                    }
                }

                successfully = true;
            } catch (Exception e) {
                e.printStackTrace();
            }

//            String pictureAddr="https://mirgar.ga/images/imgcat/1/";
//            try {
//                for(int i : mapRootCat.values()){
//                    String currentPictureAddr = pictureAddr + i + '_' + i + ".png";
//                    Logger.i("Picture address: " + currentPictureAddr);
//                    InputStream inpStream = new java.net.URL(currentPictureAddr).openStream();
//
//                    //ImageLoader.init().
//                    mapRootCatPics.put(i, BitmapFactory.decodeStream(inpStream));
//                    if(mapRootCatPics.get(i) == null)
//                        Logger.e("Can not find picture '" + pictureAddr + i + '_' + i + ".png'!");
//                }
//            } catch (Exception e){
//                Logger.e(e.getMessage());
//                e.printStackTrace();
//            }

            return loadedCategs;
        }

        @Override
        protected void onPostExecute(Categs categs) {
            Logger.i(getClass(), "PostExecuting...");
            context.mapCat = categs.mapCat;
            context.mapRootCat = categs.mapRootCat;
//            context.mapRootCatPics = categs.mapRootCatPics;
            super.onPostExecute(categs);
            context.ready = true;
            for (OnFinishLoadCategsListener listener : listenersQueue)
                listener.onFinishLoadCategs(successfully);
            Logger.i(getClass(), "Finished " + (successfully ? "" : "un") + "successfully.");
            context.task = null;
        }
    }
}
