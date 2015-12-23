package com.nyu.cs9033.eta.controllers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nyu.cs9033.eta.R;
import com.nyu.cs9033.eta.TripDatabaseHelper;
import com.nyu.cs9033.eta.models.Trip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	private static final String SERVER_URL = "http://cs9033-homework.appspot.com";
	Button createBtn, viewBtn, activateBtn;
	TextView statusView;
	private Trip trip = new Trip();

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// TODO - fill in here
		createBtn = (Button)findViewById(R.id.createButton);
		viewBtn = (Button)findViewById(R.id.viewButton);
		activateBtn = (Button)findViewById(R.id.activateBtn);
		statusView = (TextView)findViewById(R.id.statusTrip);

		TripDatabaseHelper dbHelper = new TripDatabaseHelper(this);
		long tripId = dbHelper.getActiveTripIDFromDB();
		if(tripId!=-1){
			trip = dbHelper.getTripObjectFromDB(tripId);
			statusView.setText("CURRENT ACTIVE TRIP: " + trip.getName());
		}else{
			trip = null;
			statusView.setText("NO ACTIVE TRIP");
		}

		createBtn.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				startCreateTripActivity();
			}
		});

		viewBtn.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				startTripHistoryActivity();
			}
		});

		activateBtn.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
				if(networkInfo == null || !(networkInfo.isConnected())){
					new AlertDialog.Builder(MainActivity.this)
							.setTitle("NETWORK IS UNCONNECTED")
							.setMessage("Please CONNECT TO INTERNET")
							.setCancelable(false)
							.setPositiveButton("OK", new DialogInterface.OnClickListener(){
								@Override
								public void onClick(DialogInterface dialog, int which){
									dialog.cancel();
								}
							}).show();
				}else{
					if(trip!=null){
						new StatusAsyncTask(trip).execute(SERVER_URL);
					}else{
						Toast toast = Toast.makeText(getApplicationContext(), "No active trip" , Toast.LENGTH_LONG);
						toast.show();
					}
				}

			}
		});
	}


	@Override
	protected void onRestart(){
		super.onRestart();

		/**
		 * get current active trip from SQLite database and display it on MainActivity View
		 * when the activity is resumed/restarted after starting a trip (or marking it as "current active trip")
		 * from ViewTripActivity
		 */

		TripDatabaseHelper dbHelper = new TripDatabaseHelper(this);
		long activeTripId = dbHelper.getActiveTripIDFromDB();
		if (activeTripId != -1) {
			trip = dbHelper.getTripObjectFromDB(activeTripId);
			statusView.setText("CURRENT ACTIVE TRIP: " + trip.getName());
		} else {
			trip = null;
			statusView.setText("NO ACTIVE TRIP !");
		}
	}

	/**
	 * The AsyncTask sends the "TRIP_STATUS" JSON request to the Web Server to get the information of each person
	 * related to the current active trip
	 */
	private class StatusAsyncTask extends AsyncTask<String, Void, String>{

		private Trip activeTrip = new Trip();

		protected StatusAsyncTask(Trip activeTrip){
			this.activeTrip = activeTrip;
		}

		@Override
		protected String doInBackground(String...urls){
			try{
				return getResponseString(urls[0]);
			}catch(IOException error){
				System.out.printf("Exception - %s%n", error.getMessage());
				return "Fail";
			}
		}

		@Override
		protected void onPostExecute(String result){
			try{
				JSONObject response = new JSONObject(result);
				JSONArray peopleList = response.getJSONArray("people");
				JSONArray distanceLeftList = response.getJSONArray("distance_left");
				JSONArray timeLeftList = response.getJSONArray("time_left");
				StringBuilder statusInformation = new StringBuilder();
				for(int i = 0;i<peopleList.length();i++){
					statusInformation.append(peopleList.getString(i) + " "
							+ distanceLeftList.getDouble(i) + " miles away (" + timeLeftList.getInt(i)/60 + "mins) ; ");
				}
				Toast toast = Toast.makeText(getApplicationContext(), statusInformation, Toast.LENGTH_LONG);
				toast.show();
			}catch(JSONException error){
				System.out.printf("Exception - %s%n", error.getMessage());
			}
		}

		public String getResponseString(String inUrl) throws IOException{

			String statusRequest = "";
			URL url = new URL(inUrl);
			HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setRequestProperty("Content-Type", "application/json");
			try{
				JSONObject request = new JSONObject();
				request.put("command", "TRIP_STATUS");
				request.put("trip_id", activeTrip.getTripId());
				statusRequest = request.toString();
			}catch(JSONException error){
				System.out.printf("Exception - %s%n", error.getMessage());
			}
			try{
				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpURLConnection.getOutputStream());
				outputStreamWriter.write(statusRequest);
				outputStreamWriter.close();
				if(httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK){
					return null;
				}
				InputStream inputStream = httpURLConnection.getInputStream();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				String serverData;
				StringBuilder responseString = new StringBuilder();
				while((serverData = bufferedReader.readLine())!=null){
					responseString.append(serverData);
				}
				inputStream.close();
				return responseString.toString();
			}finally{
				httpURLConnection.disconnect();
			}
		}
	}



	/**
	 * This method should start the
	 * Activity responsible for creating
	 * a Trip.
	 */
	public void startCreateTripActivity() {
		
		// TODO - fill in here
		Intent intent = new Intent(this, CreateTripActivity.class);
		startActivityForResult(intent, 1);
	}
	
	/**
	 * This method should start the
	 * Activity responsible for viewing
	 * tripHistory
	 */
	public void startTripHistoryActivity() {
		
		// TODO - fill in here
		TripDatabaseHelper tripDatabaseHelper = new TripDatabaseHelper(this);
		SQLiteDatabase db = tripDatabaseHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from trip", null);
		if(cursor.getCount()!=0){
			Intent intent = new Intent(getBaseContext(), TripHistoryActivity.class);
			startActivity(intent);
		}else{
			cursor.close();
			db.close();
			Toast.makeText(this, "There is no trip!", Toast.LENGTH_SHORT).show();
		}
	}
}
