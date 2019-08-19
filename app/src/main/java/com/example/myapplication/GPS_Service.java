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

    private LocationListener listener,locationListenerGps,locationListenerNetwork;
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
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Log.e("Providers",locationManager.getProviders(true).toString());
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, listener);

        /*
        locationListenerGps = new LocationListener() {
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

                        Log.e("adresse_gps_src",addressDetails.toString());

                        i.putExtra("adresse_gps", addressDetails.toString());
                        i.putExtra("latitude_gps", location.getLatitude());
                        i.putExtra("longitude_gps", location.getLongitude());
                        i.putExtra("altitude_gps", location.getAltitude());
                        i.putExtra("accuracy_gps", location.getAccuracy());
                        i.putExtra("provider_gps", location.getProvider());
                        i.putExtra("bearing_gps", location.getBearing());
                        i.putExtra("speed_gps", location.getSpeed());

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            i.putExtra("elapsedRealtimeNanos_gps", location.getElapsedRealtimeNanos());
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
        locationListenerNetwork = new LocationListener() {
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

                        Log.e("adresse_network_src",addressDetails.toString());

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

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Log.e("providers",providers.toString());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0,locationListenerGps);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0,locationListenerNetwork);
        */

        /*
        boolean gps_enabled;
        boolean network_enabled;
        gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (gps_enabled) {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0,locationListenerGps);
        }
        if (network_enabled) {
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0,locationListenerNetwork);
        }
        */
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            locationManager.removeUpdates(listener);
        }
    }

}