package org.donampa.nbibik.dipl.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import org.donampa.nbibik.dipl.MainActivity;


public class LocationListener implements android.location.LocationListener {
    private static boolean started = false;

    public static void startUp(MainActivity itsContext) {
        if (!started) {
            LocationManager locationManager =
                    (LocationManager) itsContext.getSystemService(Context.LOCATION_SERVICE);

            LocationListener locationListenerner = new LocationListener();

            if (itsContext.getLocationPermission() &&
                    ActivityCompat.checkSelfPermission(itsContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    locationManager != null) {

                locationManager.requestLocationUpdates(
                        locationManager.GPS_PROVIDER,
                        5000,
                        10,
                        locationListenerner
                );
                started = true;
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
