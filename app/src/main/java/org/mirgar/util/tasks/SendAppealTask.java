package org.mirgar.util.tasks;

import android.content.Context;
import android.os.AsyncTask;

import org.mirgar.util.BitmapManufacture;
import org.mirgar.util.Logger;
import org.mirgar.util.exceptions.JSONParsingException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by n.bibik on 20.06.2018.
 */

public class SendAppealTask extends AsyncTask<String, Void, String> {
    private static final String LINE_END = "\r\n";
    private static final String TWO_HYPHENS = "--";
    private final String BOUNDARY;
    private final File CACHE_DIR;
    public Throwable exception;

    public SendAppealTask(Context context) {
        BOUNDARY = Long.toHexString(System.currentTimeMillis());
        CACHE_DIR = context.getCacheDir();
    }

    @Override
    protected String doInBackground(String... args) {
        String endl = "\r\n";
        String twoHyphens = "--";
        String boundary = "******";
        String result = "Ein Fehler ist aufgetreten";
        String upLoadServerUri;

        String charset = "UTF-8";
        String insertedReportID;

        DataOutputStream dos = null;

        try {
            //insertedReportID = args[0];

            //<editor-fold defaultstate="collapsed" desc="ADD <s>LOGIN CREDENTIALS</s> JSON TO THE QUERY">
            final JSONObject itsJson;
            try {
                itsJson = new JSONObject(args[0]);
            } catch (JSONException ex) {
                throw new JSONParsingException("Firs argument in method must be an JSONObject!", ex);
            }
//            final String authKeyMail = "authMail";
//            final String authKeyPW = "authPW";
//            Context mContext = getContext();
//            SharedPreferences mPrefs = mContext.getSharedPreferences("privPrefs", Context.MODE_PRIVATE);
//
//            String email = mPrefs.getString(authKeyMail, "default");
//            String pw = mPrefs.getString(authKeyPW, "default");

            Map<String, String> params = new HashMap<>();
//            params.put("emailadr", email);
//            params.put("passwrt", pw);
//            params.put("json", itsJson.toString());
            upLoadServerUri = "http://mirgar.ga/modules/mod_mobile_app_support/sendAppeal.php?json=" + itsJson.toString();

//            StringBuilder sbParams = new StringBuilder();
//            boolean firstTime = true;
//            for (String key : params.keySet()) {
//                try {
//                    if (!firstTime) {
//                        sbParams.append("&");
//                    } else firstTime = false;
//                    sbParams.append(key).append("=")
//                            .append(URLEncoder.encode(params.get(key), charset));
//
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
//            }
            // </editor-fold>

            Set<File> filesToUpload = new HashSet<>();


            for(byte i = 1; i < args.length; i++) {
                File newFile = new File(args[i]);
                if (newFile.exists() && newFile.isFile())
                    filesToUpload.add(newFile);
            }

            URL url = new URL(upLoadServerUri);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url
                    .openConnection();
            // allow  input and output
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            // use POST way

            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.setRequestProperty("Content-Type",
                                                 "multipart/form-data;boundary=" + boundary
            );

            dos = new DataOutputStream(
                    httpURLConnection.getOutputStream());

            //<editor-fold defaultstate="collapsed" desc="Putting credentials to upload query">
//            //BEGIN - ADD LOGIN CREDENTIALS TO THE UPLOADER
//            dos.writeBytes(twoHyphens + boundary + endl);
//            //"emailadr" MUST BE THE EXACT SAME NAME AS IN PHP $_POST['emailadr']
//            dos.writeBytes("Content-Disposition: form-data; name=\"emailadr\"" + endl);
//            dos.writeBytes(endl);
//
//            dos.writeBytes(email);
//            dos.writeBytes(endl);
//            dos.writeBytes(twoHyphens + boundary + endl);
//
//            //"passwrt" MUST BE THE EXACT SAME NAME AS IN PHP $_POST['passwrt']
//            dos.writeBytes("Content-Disposition: form-data; name=\"passwrt\"" + endl);
//            dos.writeBytes(endl);
//            dos.writeBytes(pw);
//            dos.writeBytes(endl);
//            dos.writeBytes(twoHyphens + boundary + endl);
//            //END - ADD LOGIN CREDENTIALS TO THE UPLOADER
//
//            //"playgroundID" MUST BE THE EXACT SAME NAME AS IN PHP $_POST['equipmentID']
//            dos.writeBytes("Content-Disposition: form-data; name=\"reportID\"" + endl);
//            dos.writeBytes(endl);
//            dos.writeBytes(insertedReportID);
//            dos.writeBytes(endl);
//            dos.writeBytes(twoHyphens + boundary + endl);
//            //END - ADD LOGIN CREDENTIALS TO THE UPLOADER
            //BEGIN - ADD JSON TO THE UPLOADER
            for(String key: params.keySet()) {
                dos.writeBytes(twoHyphens + boundary + endl);
                //"emailadr" MUST BE THE EXACT SAME NAME AS IN PHP $_POST['json']
                dos.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + endl);
                dos.writeBytes(endl);
                dos.writeBytes("Content-Type: application/x-www-form-urlencoded");
                dos.writeBytes(endl);

                dos.writeBytes(params.get(key));
                dos.writeBytes(endl);
            }
            dos.writeBytes(twoHyphens + boundary + endl);
            //END - ADD JSON TO THE UPLOADER
//          </editor-fold>

            final int kByte = 1024;
            final int bufSize = 8 * kByte;

            for (File file: filesToUpload) {
                //"image_file" MUST BE THE EXACT SAME NAME AS IN PHP $_FILES['image_file']
                dos.writeBytes("Content-Disposition: form-data; name=\"photo[]\"; filename=\""
                                       + file.getName()
                                       + "\""
                                       + endl);
                dos.writeBytes(endl);

                InputStream is = new FileInputStream(file);
                byte[] buffer = new byte[bufSize];
                int count;

                while ((count = is.read(buffer)) != -1) {
                    dos.write(buffer, 0, count);
                }
                is.close();
                dos.writeBytes(endl);
                dos.writeBytes(twoHyphens + boundary + endl);


                //"image_file" MUST BE THE EXACT SAME NAME AS IN PHP $_FILES['image_file']
                String fileName = file.getName();
                        if(fileName.endsWith(".jpg"))
                    fileName = fileName.substring(0, fileName.length() - 4);
                else if(fileName.endsWith(".jpeg"))
                    fileName = fileName.substring(0, fileName.length() - 5);


                for(short i = 1; i < 4; i++) {
                    File tmp_file = new File(CACHE_DIR, fileName + (i == 1 ? "_thb" : i == 2 ? "_thm" : "_ths") + ".jpg");
                    if (!tmp_file.exists())
                        tmp_file.createNewFile();
                    dos.writeBytes("Content-Disposition: form-data; name=\"photo[]\"; filename=\""
                                           + tmp_file.getName()
                                           + "\""
                                           + endl
                    );
                    dos.writeBytes(endl);

                    BitmapManufacture.prepareImage(file, tmp_file, i);
                    is = new FileInputStream(tmp_file);

                    while ((count = is.read(buffer)) != -1) {
                        dos.write(buffer, 0, count);
                    }
                    is.close();
                    dos.writeBytes(endl);
                    dos.writeBytes(twoHyphens + boundary + endl);
                }
            }

            dos.close();

            InputStream is = httpURLConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String buf;
            StringBuilder builder = new StringBuilder();
            while ((buf = br.readLine()) != null)
                builder.append(buf).append('\n');

            result = builder.toString();

            is.close();
            httpURLConnection.disconnect();
        } catch (JSONParsingException ex) {
            exception = ex;
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(dos != null)
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        Logger.i(getClass(), result);
        return result;
    }
}
