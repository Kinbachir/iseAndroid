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
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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

    private BroadcastReceiver receiver;
    private boolean isConnected = false;

    LinearLayout linearLayout,linearLayout1,linearLayout2;
    ImageView imageView;
    TextView textView1,textView2;

    String driver = "com.mysql.jdbc.Driver";
    String url = "jdbc:mysql://ms1003976-001.dbaas.ovh.net:35365/udashboard_prod1";
    String user = "cp-admin-1";
    String password = "JNHY678nhbg87zsef836sdfkoi34SD";

    public static final String MyPREFERENCES = "MyPrefs" ;
    SharedPreferences sharedpreferences;

    int id_inserted;
    Timestamp date_install;
    String url_selected,email_user;
    Boolean isExecuted1 = false, isExecuted2 = false;

    Long elapsedRealtimeNanos_updated;
    String location_updated,provider_updated;
    Float accuracy_updated,bearing_updated,speed_updated;
    Double latitude_updated,longitude_updated,altitude_updated;

    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        linearLayout = findViewById(R.id.linearLayout);
        linearLayout1 = findViewById(R.id.linearLayout1);
        linearLayout2 = findViewById(R.id.linearLayout2);

        imageView = findViewById(R.id.imageView);
        textView1 = findViewById(R.id.textView1);
        textView2 = findViewById(R.id.textView2);

        date_install = getCurrentDate();

        if(isNetworkAvailable()) {
            isExecuted1 = true;
            Log.e("url","selected in onCreate");
            new SelectMySqlUrl().execute();
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        runtimePermissions();

    }

    private void runtimePermissions() {
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
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            // permissions accordées
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                new InsertMySql().execute();
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

    protected void createLocationRequest() {
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!isGPS) {
            Log.e("Location Services","disabled");
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();

            locationRequest = new LocationRequest();
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(5000);
            locationRequest.setMaxWaitTime(5000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
        else {
            Log.e("Location Services","enabled");
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(isNetworkAvailable()) {
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    //GPS inactif
                    if (status.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        try {
                            status.startResolutionForResult(MainActivity.this, 1000);
                        }
                        catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    private void startLocation() {
        if(isNetworkAvailable()){
            Intent i = new Intent(getApplicationContext(),GPS_Service.class);
            startService(i);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            if(!isExecuted1) {
                Log.e("url","not selected in onCreate");
                new SelectMySqlUrl().execute();
            }

            if (webView != null) {
                createLocationRequest();
                startLocation();
                Log.e("webview","resuming");
                webView.onResume();
                webView.resumeTimers();
            }
            else {
                createLocationRequest();
                startLocation();
                Log.e("webview","loading");
                loadApplication();
            }

            if (broadcastReceiver == null) {
                broadcastReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        location_updated = intent.getExtras().get("adresse").toString();
                        latitude_updated = intent.getDoubleExtra("latitude",0);
                        longitude_updated = intent.getDoubleExtra("longitude",0);
                        //Toast.makeText(getApplicationContext(),location_updated,Toast.LENGTH_LONG).show();
                        Log.e("adresse",location_updated);
                        Log.e("latitude",""+latitude_updated);
                        Log.e("longitude",""+longitude_updated);

                        /*
                        accuracy_updated = intent.getFloatExtra("accuracy",0);
                        altitude_updated = intent.getDoubleExtra("altitude",0);
                        bearing_updated = intent.getFloatExtra("bearing",0);
                        provider_updated = intent.getStringExtra("provider");
                        speed_updated = intent.getFloatExtra("speed",0);
                        elapsedRealtimeNanos_updated = intent.getLongExtra("elapsedRealtimeNanos",0);

                        Log.e("accuracy",""+accuracy_updated);
                        Log.e("altitude",""+altitude_updated);
                        Log.e("bearing",""+bearing_updated);
                        Log.e("provider",""+provider_updated);
                        Log.e("speed",""+speed_updated);
                        Log.e("elapsedRealtimeNanos",""+elapsedRealtimeNanos_updated);
                        */

                        new UpdateMySqlLocation().execute();
                        new InsertMySqlHistorique().execute();
                    }
                };
            }
            registerReceiver(broadcastReceiver, new IntentFilter("location_update"));

            if (receiver == null) {
                receiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        RefreshNetworkAvailable();
                    }
                };
            }
            registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return (activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting());
    }

    private void loadApplication() {
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        url_selected = sharedpreferences.getString("url_app","");

        linearLayout1.setVisibility(LinearLayout.VISIBLE);
        linearLayout2.setVisibility(LinearLayout.GONE);

        webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(true);

        /*
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT); //LOAD_CACHE_ONLY, LOAD_DEFAULT, LOAD_NO_CACHE and LOAD_CACHE_ELSE_NETWORK.
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        webSettings.setAppCachePath(appCachePath);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(true);
        */

        webView.addJavascriptInterface(new MyJavaScriptInterface(),"android");
        WebViewClient webViewClient = new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                view.loadUrl("javascript:window.android.onUrlChange(window.location.href);");
            }
        };
        webView.loadUrl(url_selected);
        webView.setWebViewClient(webViewClient); //Chargez l'URL dans WebView et non pas dans le navigateur web
    }

    class MyJavaScriptInterface {
        @JavascriptInterface
        public void onUrlChange(String url) {
            if(url.contains("auth")) {
                Log.e("auth", url);
                email_user = null;
                isExecuted2 = false;
            }
            else if(!url.contains("auth") && url.contains("idashboard") && !isExecuted2) {
                Log.e("app", url);
                if(email_user == null) {
                    Log.e("email", "getEmail en cours ...");
                    getEmail();
                    new UpdateMySqlEmail().execute();
                }
                isExecuted2 = true;
            }
        }
    }

    private void getEmail() {
        webView.post(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript("(function(){return window.document.getElementsByClassName('email')[0].innerHTML})()", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String res) {
                            if(res != null)
                                email_user = res.substring(1,res.length()-1);
                        }
                    });
                }
            }
        });
    }

    //
    private void PageConnectionFailed() {
        linearLayout1.setVisibility(LinearLayout.GONE);
        linearLayout2.setVisibility(LinearLayout.VISIBLE);
        imageView.setImageResource(R.drawable.wifi4);
        textView1.setVisibility(TextView.GONE);
        textView2.setVisibility(TextView.GONE);
        //textView1.setText("Pas de connexion internet");
        //textView2.setText("Veuillez vérifier votre connexion internet \n et réessayez");
    }

    //
    private boolean RefreshNetworkAvailable() {
        ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        //network on
        if (connectivity != null) {
            NetworkInfo activeNetworkInfo = connectivity.getActiveNetworkInfo();
            if (activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting()) {
                if (!isConnected) {
                    //createLocationRequest();
                    //loadApplication();
                    isConnected = true;
                }
                return true;
            }
        }
        //network off
        PageConnectionFailed();
        isConnected = false;
        return false;
    }

    //
    private void PageSiteInactif() {
        linearLayout1.setVisibility(LinearLayout.GONE);
        linearLayout2.setVisibility(LinearLayout.VISIBLE);
        imageView.setImageResource(R.drawable.warn3);
        textView1.setText("Site inactif");
        textView2.setText("site non disponible pour le moment \n réessayez plus tard");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(webView != null) {
            webView.onPause();
            webView.pauseTimers();
        }
    }

    //
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }

        if(receiver != null) {
            unregisterReceiver(receiver);
        }
        /*
        if (webView != null) {
            webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            webView.clearHistory();

            ((ViewGroup) webView.getParent()).removeView(webView);
            /*
            webView.clearCache(true);
            webView.onPause();
            webView.removeAllViews();
            webView.destroyDrawingCache();
            webView.pauseTimers();
            //
            webView.destroy();
            webView = null;
        }
        */
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        }
        else {
            super.onBackPressed();
        }
    }

    public class SelectMySqlUrl extends AsyncTask<String, Void, String> {
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
                    Log.e("connexion 1", "Vérifiez la connexion Internet");
                }
                else {
                    Log.e("", "Connexion à la BDD avec succès, selection en cours ...");
                    res = selectUrl(con);
                }
                con.close();
            }
            catch (Exception e) {
                Log.e("error 1", e.getMessage());
            }
            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            if(result == "") {
                PageSiteInactif();
            }
        }
    }

    public class InsertMySql extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            new SelectMySqlUrl().execute();
        }

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... params) {
            try {
                Class.forName(driver);
                Connection con = DriverManager.getConnection(url, user, password);
                if (con == null) {
                    Log.e("connexion 2", "Vérifiez la connexion internet");
                }
                else {
                    Log.e("", "Connexion à la BDD avec succès, insertion en cours ...");
                    insertData(con);
                }
                con.close();
            }
            catch (Exception e) {
                Log.e("error 2", e.getMessage());
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }

    public class UpdateMySqlLocation extends AsyncTask<String, Void, String> {

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
                    Log.e("connexion 3", "Vérifiez la connexion Internet");
                }
                else {
                    Log.e("", "Connexion à la BDD avec succès, mise à jour de l'adresse en cours ...");
                    updateLocation(con,location_updated);
                }
                con.close();
            }
            catch (Exception e) {
                e.printStackTrace();
                Log.e("error 3", e.getMessage());
            }

            return "";
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }

    public class UpdateMySqlEmail extends AsyncTask<String, Void, String> {
        String old_email;

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
                    Log.e("connexion 4", "Vérifiez la connexion Internet");
                }
                else {
                    old_email = selectEmail(con);
                    if(old_email == null || old_email != email_user) { // || old_email != email_user
                        Log.e("", "Connexion à la BDD avec succès, mise à jour de l'email en cours ...");
                        updateEmail(con,email_user);
                    }
                    else if(old_email != null && old_email != email_user) {
                        Log.e("", "Connexion à la BDD avec succès, nuv user avec mm appareil, mise à jour de l'email en cours ...");
                        updateEmail(con,email_user);
                    }
                    else {
                        Log.e("", "pas de mise à jour, mm user connecté");
                    }
                }
                con.close();
            }
            catch (Exception e) {
                Log.e("error 4", e.getMessage());
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }

    public class InsertMySqlHistorique extends AsyncTask<String, Void, String> {

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
                    Log.e("connexion 5", "Vérifiez la connexion internet");

                }
                else {
                    Log.e("", "Connexion à la BDD avec succès, insertion de l'historique en cours ...");
                    insertHistorique(con,location_updated,latitude_updated,longitude_updated,email_user);
                }
                con.close();
            }
            catch (Exception e) {
                Log.e("error 5", e.getMessage());
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
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
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString("url_app", url_app);
                editor.apply();
            }
            Log.e("", "La selection de l'url est effectuée url = "+url_app);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return url_app;
    }

    private void insertData(Connection con) {
        String query = "insert into tb_app_android_install (ip_wan, ip_mac, nom_machine, ip_lan, date_install) values (?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement ;
        ResultSet rs;
        try {
            preparedStatement = con.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, getIpWanAddress());
            preparedStatement.setString(2, getMacAddress());
            preparedStatement.setString(3, getDeviceName());
            preparedStatement.setString(4, getIpLanAddress());
            preparedStatement.setTimestamp(5, date_install);

            int rowAffected = preparedStatement.executeUpdate();

            if(rowAffected == 1) {
                rs = preparedStatement.getGeneratedKeys();
                if(rs.next()) {
                    int id_android = rs.getInt(1);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putInt("id_android", id_android);
                    editor.apply();
                }
                Log.e("", "L'insertion est effectuée");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateLocation(Connection con,String loc) {
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        id_inserted = sharedpreferences.getInt("id_android",0);
        if(id_inserted != 0) {
            try {
                String query = "update tb_app_android_install set localisation = ? where id_android = ?";
                PreparedStatement preparedStmt = con.prepareStatement(query);
                preparedStmt.setString(1, loc);
                preparedStmt.setInt(2, id_inserted);
                preparedStmt.executeUpdate();
                Log.e("", "La mise à jour de l'adresse est effectuée");
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else {
            Log.e("", "id_android null");
        }
    }

    private String selectEmail(Connection con) {
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        id_inserted = sharedpreferences.getInt("id_android", 0);
        String email_selected;
        if (id_inserted != 0) {
            try {
                String query = "select nom_user from tb_app_android_install where id_android = " + id_inserted;
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(query);

                while (rs.next()) {
                    if(rs.getString(1) != null) {
                        email_selected = rs.getString(1);
                        return email_selected;
                    }
                }
                Log.e("", "La selection de l'email est effectuée");
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else {
            Log.e("", "id_android null");
        }
        return null;
    }

    private void updateEmail(Connection con,String email) {
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        id_inserted = sharedpreferences.getInt("id_android",0);
        if(id_inserted != 0) {
            try {
                String query = "update tb_app_android_install set nom_user = ? where id_android = ?";
                PreparedStatement preparedStmt = con.prepareStatement(query);
                preparedStmt.setString(1, email);
                preparedStmt.setInt(2, id_inserted);
                preparedStmt.executeUpdate();
                Log.e("", "La mise à jour de l'email est effectuée");
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else {
            Log.e("", "id_android null");
        }
    }

    private void insertHistorique(Connection con,String location,Double latitude,Double longitude,String email) {
        if(email != null) {
            String query = "insert into tb_historique_gps (ip_wan, ip_mac, nom_user, nom_machine, localisation, ip_lan, date_install, latitude_gps, longitude_gps) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement;
            try {
                preparedStatement = con.prepareStatement(query);
                preparedStatement.setString(1, getIpWanAddress());
                preparedStatement.setString(2, getMacAddress());
                preparedStatement.setString(3, email);
                preparedStatement.setString(4, getDeviceName());
                preparedStatement.setString(5, location);
                preparedStatement.setString(6, getIpLanAddress());
                preparedStatement.setTimestamp(7, date_install);
                preparedStatement.setDouble(8,latitude);
                preparedStatement.setDouble(9,longitude);

                int rowAffected = preparedStatement.executeUpdate();

                if(rowAffected == 1) {
                    Log.e("", "L'insertion avec email est effectuée");
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else {
            String query = "insert into tb_historique_gps (ip_wan, ip_mac, nom_machine, localisation, ip_lan, date_install,latitude_gps,longitude_gps) values (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement;
            try {
                preparedStatement = con.prepareStatement(query);
                preparedStatement.setString(1, getIpWanAddress());
                preparedStatement.setString(2, getMacAddress());
                preparedStatement.setString(3, getDeviceName());
                preparedStatement.setString(4, location);
                preparedStatement.setString(5, getIpLanAddress());
                preparedStatement.setTimestamp(6, date_install);
                preparedStatement.setDouble(7,latitude);
                preparedStatement.setDouble(8,longitude);

                int rowAffected = preparedStatement.executeUpdate();

                if(rowAffected == 1) {
                    Log.e("", "L'insertion sans email est effectuée");
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}