package org.mirgar;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.mirgar.util.LocationListener;
import org.mirgar.util.Logger;
import org.mirgar.util.PrefManager;
import org.mirgar.util.exceptions.NoPermissionException;

public class GeneralActivity extends Activity {
    private static final int LOC_SETTINGS_ACTIVITY_REQUEST = 2;
    private static final int LOCATION_PERMISSION_REQUEST = 1;
    public boolean isPermissionChecking = false;
    private boolean isLocPermission = false;

    public boolean isLocPermission() {
        return isLocPermission;
    }
    private final String IS_LOC_PERMISSION_BUNDLE_ID = "isLocPermission";

    private View mainView;
    private View progressView;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent parentActivityIntent = NavUtils.getParentActivityIntent(this);
            parentActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            NavUtils.navigateUpTo(this, parentActivityIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    long lastKey = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            event.startTracking();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.isTracking() && !event.isCanceled()) {
            if (event.getEventTime() - lastKey > 3000) {
                lastKey = event.getEventTime();
                Toast.makeText(this, "Для выхода нажмите НАЗАД ещё раз", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                finishAffinity();
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    static public class LocMessageHandler extends Handler {
        public static final int MSG_CHECK_PERMISSION = 0;
        public static final int MSG_NO_GPS = 1;

        private GeneralActivity context;

        private LocMessageHandler(GeneralActivity context) {
            this.context = context;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CHECK_PERMISSION:
                    context.getLocationPermission();
                    break;
                case MSG_NO_GPS:
                    context.showMessageNoGps();
                    break;
            }
        }
    }

    public boolean checkLocationWorks() {
        LocMessageHandler handler = new LocMessageHandler(this);

        try {
            LocationListener.init(this, handler);
            do {
                Thread.sleep(1000);
            } while (LocationListener.isIniting());
        } catch (NoPermissionException ex) {
            Logger.e(ex);
            showMessageNoGpsPermission();
        } catch (Exception e) {
            Logger.e(e);
            showMessageUnknownFail();
        }
        return LocationListener.getInstance() != null;
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) return false;

        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public Location getLocation() {
        Location location = LocationListener.getLastKnownLocation();
        if(location!=null)
            return location;
        else {
            LocMessageHandler handler = new LocMessageHandler(this);

            try {
                LocationListener.init(this, handler);
                do {
                    Thread.sleep(1000);
                } while (LocationListener.isIniting());
            } catch (NoPermissionException ex) {
                Logger.e(ex);
                showMessageNoGpsPermission();
            } catch (Exception ex) {
                Logger.e(ex);
                showMessageUnknownFail();
            }
        }
        return LocationListener.getLastKnownLocation();

    }

    public boolean getLocationPermission() {
        isPermissionChecking = true;
        try {
            if (isLocationPermission()) {
                return true;
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                                                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    AlertDialog.Builder explanationDialogBuilder = new AlertDialog.Builder(this);
                    explanationDialogBuilder
                            .setTitle("Внимание!")
                            .setMessage("Для корректной работы приложения необходим доступ к геоданным.")
                            .setIcon(android.R.drawable.stat_sys_warning)
                            .setCancelable(false)
                            .setNegativeButton("Ок", (DialogInterface dialog, int id) -> dialog.cancel());
                    new Thread(explanationDialogBuilder::show, "explDialog").run();
                }
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                                                  new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                                  LOCATION_PERMISSION_REQUEST
                );
                return false;
                // PERMISSIONS_READ_CONTACTS_REQUEST is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
            }
        } catch (Throwable tr) {
            isPermissionChecking = false;
            LocationListener locationListener = LocationListener.getInstance();
            locationListener.inHandler.sendEmptyMessage(LocationListener.ItsHandler.MSG_PERMISSION_GETTING_END);
            throw tr;
        }
    }

    public boolean isLocationPermission() {
        return
                ActivityCompat.checkSelfPermission(this,
                                                   Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this,
                                                           Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST:
                // If request is cancelled, the result arrays are empty.
                boolean isLocPermission = false;

                for (int i = 0; i < permissions.length; i++) {
                    if(permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) ||
                            permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION))
                        isLocPermission = isLocPermission || grantResults[i] == PackageManager.PERMISSION_GRANTED;
                }
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // permission was granted, yay! Do the
//                    // contacts-related task you need to do.
//                    isLocPermission = true;
//                } else {
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                    isLocPermission = false;
//                }
                isPermissionChecking = false;
                LocationListener locationListener = LocationListener.getInstance();
                locationListener.inHandler.sendEmptyMessage(LocationListener.ItsHandler.MSG_PERMISSION_GETTING_END);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case LOC_SETTINGS_ACTIVITY_REQUEST:
                LocationListener.getInstance().inHandler.sendEmptyMessage(LocationListener.ItsHandler.MSG_GPS_SWITCHING_END);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null)
            isLocPermission = savedInstanceState.getBoolean(IS_LOC_PERMISSION_BUNDLE_ID);
        else {
            PrefManager.init(getApplicationContext());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_LOC_PERMISSION_BUNDLE_ID, isLocPermission());
    }

    public void showMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Пожалуйста, включите GPS. Это необходимо для работы приложения.")
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, (dialog, id) -> startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), LOC_SETTINGS_ACTIVITY_REQUEST))
                .setNegativeButton(android.R.string.no, (final DialogInterface dialog, @SuppressWarnings("unused") final int id) -> dialog.cancel());
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public void showMessage(@NonNull String msg) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setMessage(msg)
                .setCancelable(false)
                .setNegativeButton(android.R.string.ok, (final DialogInterface dialog, @SuppressWarnings("unused") final int id) -> dialog.cancel());
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public void showMessageNoGpsPermission() {
        showMessage("Для корректной работы приложения необходим доступ к геоданным.");
    }

    public void showMessageUnknownFail() {
        showMessage("Неизвестная ошибка.");
    }

    public void swapVisibility(View view1, View view2) {
        if (view1.getVisibility() == View.GONE && view2.getVisibility() == View.VISIBLE) {
            view1.setVisibility(View.VISIBLE);
            view2.setVisibility(View.GONE);
        } else {
            view1.setVisibility(View.GONE);
            view2.setVisibility(View.VISIBLE);
        }
    }

    @SuppressWarnings("unused")
    public void swapVisibility(int idView1, int idView2) {
        swapVisibility(findViewById(idView1), findViewById(idView2));
    }
}
