package com.example.enigeerdesignflower;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {


    private TextView AddressText;
    private TextView AddressText1;

    private Button LocationButton;
    private LocationRequest locationRequest;
    String rain;
    double lon =  0.0;
    double lat = 0.0;
    double amountofrain = 0;
    private final String url = "https://api.openweathermap.org/data/2.5/forecast?";
    private final String appid = "e53301e27efa0b66d05045d91b2742d3";
    DecimalFormat df = new DecimalFormat("#.##");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //text which will switch to the longtitude and latitude
        AddressText = findViewById(R.id.addressText);
        AddressText1 = findViewById(R.id.addressText1);
        //only for testing to check if we get the right long and lat
        LocationButton = findViewById(R.id.locationButton);


        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //wait a few seconds to make sure the location is precise
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        LocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent  = new Intent( MainActivity.this, tuin1.class);
                startActivity(intent);

                //get the current location
                getCurrentLocation();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        lat = 51.4231;
                        lon = 5.4623;
                        getWeatherDetails(v , lon, lat);
                    }
                }, 5000);

            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if (isGPSEnabled()) {
                    //get location if gps is enabled
                    getCurrentLocation();
                }else {
                    //otherwise get gps enabled
                    turnOnGPS();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {

                //get current location
                getCurrentLocation();
            }
        }
    }

    public void getWeatherDetails(View view, double lon, double lat) {

        amountofrain = 0;
        String tempUrl = "https://api.openweathermap.org/data/2.5/forecast?lat=" + lat + "&lon="+ lon +"&appid=e53301e27efa0b66d05045d91b2742d3";




        StringRequest stringRequest = new StringRequest(Request.Method.POST, tempUrl, new Response.Listener<String>() {            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray jsonArray = jsonResponse.getJSONArray("list");




                    int i = -1;
                    while( i < 8){
                        i++;
                        JSONObject jsonObjectWeather = jsonArray.getJSONObject(i);
                        String weather = jsonObjectWeather.getString("weather");
                        //String description = jsonObjectWeather.getString("weather");
                        if(weather.contains("rain")){
                            JSONObject jsonobjectrain = jsonObjectWeather.getJSONObject("rain");
                            Double rain = jsonobjectrain.getDouble("3h");
                            amountofrain = amountofrain +rain;
                        }
                        AddressText.setText("In the coming 24 hours there will fall " + amountofrain + " millimeters of rain. So we would advise you not to give water to you plants");
                    }







                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString().trim(), Toast.LENGTH_SHORT).show();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    private void getCurrentLocation() {
        //if the version of the phone is high enough to get the location
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //if the app has already permission to get the location
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //if the gps is already enabled
                if (isGPSEnabled()) {
                    //will try to get the location
                    LocationServices.getFusedLocationProviderClient(MainActivity.this)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);

                                    LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                            .removeLocationUpdates(this);
                                    //if it gets an result it will get latitude and longtitude
                                    if (locationResult != null && locationResult.getLocations().size() >0){

                                        int index = locationResult.getLocations().size() - 1;
                                        lat = locationResult.getLocations().get(index).getLatitude();
                                        lon = locationResult.getLocations().get(index).getLongitude();
                                        //will set the adress text to latitude and longtitude we found
                                    }
                                }
                            }, Looper.getMainLooper());

                } else {
                    //if the gps isn't turned on
                    turnOnGPS();
                }

            } else {
                //ask permission from the user for the location of the phone
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    private void turnOnGPS() {
        //
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                //make sure that the gps isn't turned on
                try {

                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(MainActivity.this, "GPS is already tured on", Toast.LENGTH_SHORT).show();

                //otherwise try to get it
                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(MainActivity.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });

    }

    private boolean isGPSEnabled() {

        LocationManager locationManager = null;
        boolean isEnabled = false;

        if (locationManager == null) {
            //if we don't have the location yet try to get them
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        //check if we can get the location
        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        //return if we got the location
        return isEnabled;

    }
}