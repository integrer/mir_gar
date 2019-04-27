package org.mirgar;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import org.mirgar.util.Logger;

//import java.io.File;

public class TextViewActivity extends GeneralActivity {

    public static final String FIELD_FILE_PATH = "File path";
    public static final String FIELD_TITLE = "wndTitle";
    public static final String FIELD_URL = "url";

    //private File itsFile;
    private String wndTitle;
    private String url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            String filePath = null;

            Window itsWindow = getWindow();
            itsWindow.requestFeature(Window.FEATURE_PROGRESS);

            setContentView(R.layout.activity_text_view);

            if(savedInstanceState == null) {
                Intent itsIntent = getIntent();
                url = itsIntent.getStringExtra(FIELD_URL);
//                itsFile = new File(itsIntent.getStringExtra(FIELD_FILE_PATH));
                wndTitle = itsIntent.getStringExtra(FIELD_TITLE);
                getWindow().setTitle(wndTitle);
            } else {
                url = savedInstanceState.getString(FIELD_URL);
                filePath = savedInstanceState.getString(FIELD_FILE_PATH);

                wndTitle = savedInstanceState.getString(FIELD_FILE_PATH);
            }

            itsWindow.setTitle(wndTitle != null ? wndTitle : "Мир Гармонии");
            WebView wv = findViewById(R.id.webView);

            ActionBar actionBar = getActionBar();
            if (actionBar != null)
                actionBar.setDisplayHomeAsUpEnabled(true);

            Activity context = this;

            wv.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged (WebView webView, int progress) {
                    super.onProgressChanged(webView, progress);

                    context.setProgress(progress * 1000);
                }
            });

            if (url != null && isOnline()) {
                wv.loadUrl(url);
            } else if (filePath != null) {
                wv.loadUrl(filePath);
            } else
                throw new NullPointerException();

            if(wndTitle != null)
                getWindow().setTitle(wndTitle);

        } catch (NullPointerException ex) {
            showMessageUnknownFail();
            Logger.e(ex);
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(FIELD_URL, url);
        outState.putString(FIELD_TITLE, wndTitle);
    }
}
