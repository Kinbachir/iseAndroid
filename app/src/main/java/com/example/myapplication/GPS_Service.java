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

public class GPS_Service extends Service {

    private LocationListener listener;
    private LocationManager locationManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
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
                        i.putExtra("accuracy", location.getAccuracy());
                        i.putExtra("altitude", location.getAltitude());
                        i.putExtra("bearing", location.getBearing());
                        i.putExtra("provider", location.getProvider());
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
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        /*
        // getting GPS status
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        // First get location from Network Provider
        if (isNetworkEnabled) {
            locationManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER,  5000,  0, listener);
            Log.d("Network", "Network");
            if (locationManager != null) {
                Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {
                    lat = location.getLatitude();
                    lng = location.getLongitude();
                }
            }
        }
        //get the location by gps
        if (isGPSEnabled) {
            if (location == null) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,0, listener);
                Log.d("GPS Enabled", "GPS Enabled");
                if (mLocationManager != null) {location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        lat = location.getLatitude();
                        lng = location.getLongitude();
                    }
                }
            }
        }
        */
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, listener); // GPS_PROVIDER
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            locationManager.removeUpdates(listener);
        }
    }

}