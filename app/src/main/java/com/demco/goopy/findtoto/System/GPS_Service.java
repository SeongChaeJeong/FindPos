package com.demco.goopy.findtoto.System;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import static com.demco.goopy.findtoto.PositionMangerActivity.LATITUDE_POS;
import static com.demco.goopy.findtoto.PositionMangerActivity.LONGITUDE_POS;

/**
 * Created by filipp on 6/16/2016.
 */
public class GPS_Service extends Service {

    private LocationListener listener;
    private LocationManager locationManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
            listener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.d("GPS", "onLocationChanged");
                    Intent i = new Intent("location_update");
                    i.putExtra(LATITUDE_POS, location.getLatitude());
                    i.putExtra(LONGITUDE_POS, location.getLongitude());
                    i.putExtra("coordinates",location.getLongitude()+" "+location.getLatitude());
                    sendBroadcast(i);
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {
                    Log.d("GPS", "onStatusChanged");
                }

                @Override
                public void onProviderEnabled(String s) {
                    Log.d("GPS", "onProviderEnabled");
                }

                @Override
                public void onProviderDisabled(String s) {
                    Log.d("GPS", "onProviderDisabled");
                    Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
            };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        //noinspection MissingPermission
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000,0,listener);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            //noinspection MissingPermission
            locationManager.removeUpdates(listener);
        }
    }
}
