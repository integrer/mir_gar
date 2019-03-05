package org.mirgar.util;

import android.os.AsyncTask;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.mirgar.MainActivity;
import org.jetbrains.annotations.Contract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Set;

public class Cats {
    private BidiMap<Integer, BidiMap<String, Integer>> mapCat;// = new DualHashBidiMap<>();
    private BidiMap<String, Integer> mapRootCat;// = new DualHashBidiMap<>();
    private loadCatsTask task;
    private MainActivity context;

    private final class CatsContainer {
        public BidiMap<Integer, BidiMap<String, Integer>> mapCat;
        public BidiMap<String, Integer> mapRootCat;

        public CatsContainer() {
            mapCat = new DualHashBidiMap<>();
            mapRootCat = new DualHashBidiMap<>();
        }
    }

    private static Cats instance;

    public static void init(MainActivity context) {
        if(instance == null)
            instance = new Cats(context);
    }

    @Contract(pure = true)
    public static Cats getInstance() {
        return instance;
    }

    public String getCatName(int rootCatId) {
        if (mapRootCat.containsValue(rootCatId))
            return mapRootCat.getKey(rootCatId);
        return null;
    }

    public String getCatName(String rootCatName, int catId) {
        BidiMap<String, Integer> mapRootCat = mapCat.get(getRootCatId(rootCatName));
        if(mapRootCat.containsValue(catId))
            return mapRootCat.getKey(catId);
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

    private Cats(boolean startLoadTask, MainActivity context) {
        this.context = context;
        if (startLoadTask) {
            task = new loadCatsTask(this);
            task.execute((Void) null);
        }
    }

    private Cats(MainActivity context) { this(true, context); }

    public Set<String> getCats(int idParentCat) {
        if (idParentCat == 0)
            return mapRootCat.keySet();
        else {
            try {
                return mapCat.get(idParentCat)
                        .keySet();
            } catch(NullPointerException ex) {
                return null;
            }
        }
    }

    public int getCatId(int keyPar, String key) {
        if (mapCat.containsKey(keyPar)) {
            BidiMap<String, Integer> map = mapCat.get(keyPar);
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

    private class loadCatsTask extends AsyncTask<Void, Void, CatsContainer> {
        Cats context;
        ArrayList<OnFinishLoadCategsListener> listenersQueue = new ArrayList<>();
        boolean successfully = false;

        public loadCatsTask(Cats context) {
            super();
            this.context = context;
        }

        @Override
        protected CatsContainer doInBackground(Void... voids) {
            Logger.i(getClass(), "loadCatsTask started successful.");
            CatsContainer loadedCats = new CatsContainer();
            String linkToCatlist = "https://mirgar.ga/getCat.php";
            try {
                URLConnection conn = new URL(linkToCatlist).openConnection();

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
                        if (!loadedCats.mapCat.containsKey(parentId)) {
                            loadedCats.mapCat.put(parentId, new DualHashBidiMap<>());
                        }
                        loadedCats.mapCat.get(parentId).put(catName, catId);
                    } else {
                        loadedCats.mapRootCat.put(catName, catId);
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

            return loadedCats;
        }

        @Override
        protected void onPostExecute(CatsContainer cats) {
            Logger.i(getClass(), "PostExecuting...");
            context.mapCat = cats.mapCat;
            context.mapRootCat = cats.mapRootCat;
            super.onPostExecute(cats);
            context.ready = true;
            for (OnFinishLoadCategsListener listener : listenersQueue)
                listener.onFinishLoadCategs(successfully);
            Logger.i(getClass(), "Finished " + (successfully ? "" : "un") + "successfully.");
            context.task = null;
        }
    }
}
