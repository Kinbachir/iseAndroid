package com.example.myapplication;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.IBinder;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;

public class GPS_Service extends Service {

    private LocationListener listener;
    private LocationManager locationManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        Log.e("Providers",locationManager.getProviders(true).toString());

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Intent i = new Intent("location_update");
                List<Address> addresses ;
                Geocoder geocoder = new Geocoder(GPS_Service.this, Locale.getDefault());
                try {
                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses == null || addresses.size()  == 0) {
                        Log.e("", "no address found");
                    }
                    else if (addresses != null && addresses.size() > 0) {
                        Address address = addresses.get(0);
                        StringBuffer addressDetails = new StringBuffer();
                        addressDetails.append(address.getAddressLine(0));
                        i.putExtra("adresse", addressDetails.toString());
                        i.putExtra("latitude", location.getLatitude());
                        i.putExtra("longitude", location.getLongitude());
                        i.putExtra("altitude", location.getAltitude());
                        i.putExtra("accuracy", location.getAccuracy());
                        i.putExtra("provider", location.getProvider());
                        i.putExtra("bearing", location.getBearing());
                        i.putExtra("speed", location.getSpeed());

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            i.putExtra("elapsedRealtimeNanos", location.getElapsedRealtimeNanos());
                        }

                        sendBroadcast(i);
                    }
                }
                catch (IOException ioException) {
                    Log.e("", "Error in getting address for the location");
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, listener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            locationManager.removeUpdates(listener);
        }
    }

}