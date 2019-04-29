package org.mirgar.util.tasks;

import android.app.Application;
import android.os.AsyncTask;

import org.apache.commons.collections4.list.TreeList;
import org.mirgar.util.Logger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by n.bibik on 17.06.2018.
 */

public class LoadFileTask extends AsyncTask<String, Void, Void> {
    private Application context;

    private List<File> filesToLoad = new TreeList<>();
    private List<URL> urls = new TreeList<>();

    LoadFileTask(Application context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        File root = context.getFilesDir();
        context = null;
        File dst;
    }

    @Override
    protected Void doInBackground(String... urls) {

        for (String strUrl: urls) {
            try {
                URL url = new URL(strUrl);

            } catch (MalformedURLException e) {
                Logger.wtf(e);
            }
        }
        return null;
    }
}
