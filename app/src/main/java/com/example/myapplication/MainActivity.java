package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.StrictMode;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private WebView webView;
    private BroadcastReceiver broadcastReceiver;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;

    String driver ="com.mysql.jdbc.Driver";
    String url = "jdbc:mysql://ms1003976-001.dbaas.ovh.net:35365/udashboard_prod1";
    String user = "cp-admin-1";
    String password = "JNHY678nhbg87zsef836sdfkoi34SD";

    public static final String MyPREFERENCES = "MyPrefs" ;
    SharedPreferences sharedpreferences;

    int id_inserted;
    String url_selected;
    String location_updated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        new SelectMySql().execute();

        if(!runtimePermissions()) {
            startLocation();
        }
    }

    private Boolean runtimePermissions() {
        //permissions non accordées
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            // si on refuse la permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)&& ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Permission de location requise")
                        .setMessage("Vous devez donner cette permission pour accéder à cette application")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},100);
                            }
                        })
                        .setCancelable(false)
                        .create().show();
            }
            //sinon on demande la permission
            else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},100);
            }
            return true;
        }
        //permissions accordées
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            // permissions accordées
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                new InsertMySql().execute();
                startLocation();
            }
            // Permissions non accordées (refuser l'autorisation)
            else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                runtimePermissions();
            }
            // Permissions non accordées (ne pas demander)
            else {
                new AlertDialog.Builder(this)
                        .setTitle("Permission de location requise")
                        .setMessage("Vous devez donner cette permission pour accéder à cette application")
                        .setPositiveButton("Autoriser manuellement", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent();
                                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .setCancelable(false)
                        .create().show();
            }
        }
    }

    private Timestamp getCurrentDate() {
        try {
            Timestamp date = new Timestamp(System.currentTimeMillis());
            return date;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getMacAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : interfaces) {
                if (!networkInterface.getName().equalsIgnoreCase("wlan0"))
                    continue;

                byte[] macBytes = networkInterface.getHardwareAddress();
                if (macBytes == null)
                    return "";

                StringBuilder mac = new StringBuilder();
                for (byte b : macBytes) {
                    mac.append(String.format("%02X:",b));
                }

                if (mac.length() > 0)
                    mac.deleteCharAt(mac.length() - 1);

                return mac.toString();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getIpLanAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : interfaces) {
                List<InetAddress> adresses = Collections.list(networkInterface.getInetAddresses());
                for (InetAddress adresse : adresses) {
                    if (!adresse.isLoopbackAddress()) {
                        String ip = adresse.getHostAddress();
                        boolean isIPv4 = ip.indexOf(':')<0;
                        if (isIPv4)
                            return ip;
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getIpWanAddress() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            URL url_name = new URL("http://bot.whatismyipaddress.com");
            BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream()));
            String ip = sc.readLine();
            return ip;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        }
        else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    private void enableGps() {
        final LocationManager manager = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
            googleApiClient.connect();

            locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30000);
            locationRequest.setFastestInterval(5000);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    //GPS actif
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    //GPS inactif
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(MainActivity.this, 1000);
                        }
                        catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    private void startLocation() {
        Intent i = new Intent(getApplicationContext(),GPS_Service.class);
        startService(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            enableGps();

            if(webView == null) {
                loadApplication();
            }

            if(webView != null) {
                webView.onResume();
            }

            if(broadcastReceiver == null) {
                broadcastReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Toast.makeText(MainActivity.this, intent.getExtras().get("adresse").toString(), Toast.LENGTH_LONG).show();
                        location_updated = intent.getExtras().get("adresse").toString();
                        new UpdateMySql().execute();
                    }
                };
            }
            registerReceiver(broadcastReceiver,new IntentFilter("location_update"));
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private void loadApplication() {
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        url_selected = sharedpreferences.getString("url_app","");
        if(isNetworkAvailable() && url_selected != "") {
            webView = findViewById(R.id.webView);
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webView.setWebViewClient(new WebViewClient()); //Chargez l'URL dans WebView et non pas dans le navigateur web
            webView.loadUrl(url_selected);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(webView != null) {
            webView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    public class InsertMySql extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... params) {
            try {
                Class.forName(driver);
                Connection con = DriverManager.getConnection(url, user, password);
                if (con == null) {
                    Log.e("", "Vérifiez la connexion internet");
                }
                else {
                    Log.e("", "Connexion à la BDD avec succès, insertion en cours ...");
                    insertData(con);
                    con.close();
                }
            }
            catch (Exception e) {
                Log.e("error ", e.getMessage());
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }

    public class UpdateMySql extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... params) {
            try {
                Class.forName(driver);
                Connection con = DriverManager.getConnection(url, user, password);
                if (con == null) {
                    Log.e("", "Vérifiez la connexion Internet");
                }
                else {
                    Log.e("", "Connexion à la BDD avec succès, mise à jour en cours ...");
                    updateData(con,location_updated);
                    con.close();
                }
            }
            catch (Exception e) {
                Log.e("error ", e.getMessage());
            }

            return "";
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }

    public class SelectMySql extends AsyncTask<String, Void, String> {

        String res;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... params) {
            try {
                Class.forName(driver);
                Connection con = DriverManager.getConnection(url, user, password);
                if (con == null) {
                    Log.e("", "Vérifiez la connexion Internet");
                }
                else {
                    Log.e("", "Connexion à la BDD avec succès, selection en cours ...");
                    res = selectUrl(con);
                    con.close();
                }
            }
            catch (Exception e) {
                Log.e("error ", e.getMessage());
            }
            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString("url_app", result);
            editor.apply();
        }
    }

    private void insertData(Connection con) {
        String query = "insert into tb_app_android_install (ip_wan, ip_mac, nom_machine, ip_lan, date)"+ " values (?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement ;
        ResultSet rs = null;
        try {
            preparedStatement = con.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, getIpWanAddress());
            preparedStatement.setString(2, getMacAddress());
            preparedStatement.setString(3, getDeviceName());
            preparedStatement.setString(4, getIpLanAddress());
            preparedStatement.setTimestamp(5, getCurrentDate());

            int rowAffected = preparedStatement.executeUpdate();

            if(rowAffected == 1) {
                rs = preparedStatement.getGeneratedKeys();
                if(rs.next()) {
                    int id_android = rs.getInt(1);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putInt("id_android", id_android);
                    editor.apply();
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if(rs != null)
                    rs.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateData(Connection con,String loc) {
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        id_inserted = sharedpreferences.getInt("id_android",0);
        if(id_inserted != 0) {
            try {
                String query = "update tb_app_android_install set localisation = ? where id_android = ?";
                PreparedStatement preparedStmt = con.prepareStatement(query);
                preparedStmt.setString(1, loc);
                preparedStmt.setInt(2, id_inserted);
                preparedStmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else {
            Log.e("", "id_android null");
        }
    }

    private String selectUrl(Connection con) {

        String url_app = "";
        try {
            String query = "select url from tb_url where etat = 1";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(query);

            while (rs.next()) {
                url_app = rs.getString(1);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return url_app;
    }

}