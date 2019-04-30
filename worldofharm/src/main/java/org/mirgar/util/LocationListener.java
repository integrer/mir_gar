package org.mirgar.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;

import com.activeandroid.Cache;

import org.jetbrains.annotations.Contract;
import org.mirgar.BuildConfig;
import org.mirgar.GeneralActivity;
import org.mirgar.util.exceptions.ExceptionWrapper;
import org.mirgar.util.exceptions.GetLocationException;
import org.mirgar.util.exceptions.NoPermissionException;

import java.util.concurrent.atomic.AtomicBoolean;


public class LocationListener implements android.location.LocationListener {
    private static final String ITS_THREAD_NAME = "LocationListener";

    private static Thread itsThread;

    @Contract(pure = true)
    public static LocationListener getInstance() {
        return instance;
    }

    private static LocationListener instance = null;

    private static GeneralActivity context;

    private Location location;

    @Contract(pure = true)
    public static Boolean isIniting() {
        return isIniting;
    }

    private static Boolean isIniting = null;

    private static boolean isGpsSwitching;

    private static boolean isPermissionGetting;

    public ItsHandler inHandler;

    private static Exception exception;

    static public class ItsHandler extends Handler {
        public static final int MSG_PERMISSION_GETTING_END = 0;
        public static final int MSG_GPS_SWITCHING_END = 1;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_PERMISSION_GETTING_END:
                    isPermissionGetting = false;
                    break;
                case MSG_GPS_SWITCHING_END:
                    isGpsSwitching = false;
                    break;
            }
        }
    }

    private LocationListener() {
        inHandler = new ItsHandler();
    }

    public static void init(GeneralActivity context, GeneralActivity.LocMessageHandler outHandler)
            throws Exception {
        isIniting = true;
        if(itsThread != null && itsThread.isAlive() && LocationListener.context != context) {
            itsThread.interrupt();
            instance = null;
        }

        ExceptionWrapper exceptionWrapper = new ExceptionWrapper();
        AtomicBoolean startUpFinished = new AtomicBoolean(false);

        itsThread = new Thread(Thread.currentThread().getThreadGroup(), () -> {
            try {
                Looper.prepare();
                startUp(context, outHandler);
                startUpFinished.set(true);
            } catch (Exception e) {
                Logger.e(e);
                exceptionWrapper.exception = e;
                startUpFinished.set(true);
                isIniting = null;
                Thread.currentThread().interrupt();
            }
        }, ITS_THREAD_NAME);

        itsThread.setPriority(Thread.MIN_PRIORITY);
        itsThread.start();

        do Thread.sleep(1000); while (!startUpFinished.get());
        Exception exception = exceptionWrapper.exception;
        if(exception != null)
            if (exception instanceof GetLocationException) throw exception;
            else if (exception instanceof NoPermissionException)
                throw exception;
            else throw exception;
    }

    private static void startUp(GeneralActivity context, GeneralActivity.LocMessageHandler outHandler)
            throws GetLocationException, NoPermissionException, InterruptedException {
        if (instance == null) {
            LocationManager locationManager =
                    (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            instance = new LocationListener();

            if (!context.isLocationPermission()) {
                outHandler.sendEmptyMessage(GeneralActivity.LocMessageHandler.MSG_CHECK_PERMISSION);
                isPermissionGetting = true;

                do Thread.sleep(1000);
                while (isPermissionGetting);

                if(!context.isLocationPermission())
                    throw new NoPermissionException();
            }

            if (locationManager != null) {
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    outHandler.sendEmptyMessage(GeneralActivity.LocMessageHandler.MSG_NO_GPS);
                    isGpsSwitching = true;

                    do Thread.sleep(1000);
                    while (isGpsSwitching);

                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                        throw new GetLocationException();
                }

                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            5000,
                            10,
                            instance
                    );

                    instance.location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    isIniting = false;
                }
            }
        } else isIniting = false;
    }

    @Contract(pure = true)
    public static Location getLastKnownLocation() {
        return instance.location;
    }

    @Override
    public void onLocationChanged(Location location) { this.location = location; }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}
}
