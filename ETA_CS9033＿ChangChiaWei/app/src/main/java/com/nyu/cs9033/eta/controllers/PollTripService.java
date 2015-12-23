package com.nyu.cs9033.eta.controllers;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by chia-weichang on 11/7/15.
 */
public class PollTripService extends IntentService {

    private static final String TAG = "PollTripService";
    private static final int POLL_INTERVAL = 1000*60;
    private static final String SERVER_URL ="http://cs9033-homework.appspot.com";


    boolean isGPSEnabled = false;
    LocationManager locationManager;
    Location location = null;

    public PollTripService(){
        super(TAG);
    }

    //method to trigger the PollTripService every 1 minute
    public static void setServiceAlarm(Context context, boolean isOn){
        Intent intent = new Intent(context, PollTripService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if(isOn){
            alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), POLL_INTERVAL, pendingIntent);
        }else{
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    //method to get the current location of the user
    public Location getCurrentUserLocation(){
        try{
            locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(isGPSEnabled){
               if(location == null && locationManager!=null){
                   LocationListener locationListener = new LocationListener() {
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
                   };
                   locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, POLL_INTERVAL, 10, locationListener);
                   location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                   locationManager.removeUpdates(locationListener);
                   Log.d("latitude", location.getLatitude() + "");
                   Log.d("longitude", location.getLongitude() + "");
               }
            }
        }catch(Exception error){
            System.out.printf("Exception - %s%n", error.getMessage());
        }
        return location;
    }

    //method to handle the incoming intents and send "UPDATE_LOCATION" JSON Request to the Web Server and getting response back
    @Override
    protected void onHandleIntent(Intent intent){
        Location currentUserLocation = getCurrentUserLocation();
        if(currentUserLocation != null){
            double locationLatitude = currentUserLocation.getLatitude();
            double locationLongitude = currentUserLocation.getLongitude();
            HttpURLConnection connection = null;
            try{
                URL url = new URL(SERVER_URL);
                connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                JSONObject updateLocationRequest = new JSONObject();
                updateLocationRequest.put("command", "UPDATE_LOCATION");
                updateLocationRequest.put("latitude", locationLatitude);
                updateLocationRequest.put("longitude", locationLongitude);
                updateLocationRequest.put("dateTime", System.currentTimeMillis());

                String request = updateLocationRequest.toString();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
                outputStreamWriter.write(request);
                outputStreamWriter.close();

                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String serverData;
                StringBuilder response = new StringBuilder();
                while((serverData = bufferedReader.readLine())!=null){
                    response.append(serverData);
                }
                inputStream.close();
            }catch(Exception error){
                System.out.printf("Exception - %s%n", error.getMessage());
            }finally{
                if(connection != null){
                    connection.disconnect();
                }
            }
        }
    }
}
