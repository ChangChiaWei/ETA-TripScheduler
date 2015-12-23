package com.nyu.cs9033.eta.controllers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.nyu.cs9033.eta.R;
import com.nyu.cs9033.eta.TripDatabaseHelper;
import com.nyu.cs9033.eta.models.Person;
import com.nyu.cs9033.eta.models.Trip;

import java.text.DateFormat;
import java.util.ArrayList;

public class ViewTripActivity extends Activity {

	private static final String TAG = "ViewTripActivity";
	EditText tripName, tripDescription, tripDate, tripTime, tripLocation, tripFriend;
	Button back, start, stop;
	Trip trip = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// TODO - fill in here
		setContentView(R.layout.activity_view_trip);
		tripName = (EditText)findViewById(R.id.view_name);
		tripDescription = (EditText)findViewById(R.id.view_description);
		tripDate = (EditText)findViewById(R.id.view_date);
		tripTime = (EditText)findViewById(R.id.view_time);
		tripLocation = (EditText)findViewById(R.id.view_location);
		tripFriend = (EditText)findViewById(R.id.view_friend);
		back = (Button)findViewById(R.id.back);
		start = (Button)findViewById(R.id.startTrip);
		stop = (Button)findViewById(R.id.stopTrip);
		trip = getTrip(getIntent());
		viewTrip(trip);
		back.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				goBack();
			}
		});

		start.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view){
				//Check for network connectivity
				ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
				if(networkInfo == null || !(networkInfo.isConnected())){
					new AlertDialog.Builder(ViewTripActivity.this)
							.setTitle("INTERNET CONNECTION FAILURE")
							.setMessage("Check your Internet connectivity please")
							.setCancelable(false)
							.setPositiveButton("OK", new DialogInterface.OnClickListener(){
								@Override public void onClick(DialogInterface dialog, int which){
									dialog.cancel();
									finish();
								}
							}).show();
				}

				//Check for GPS status
				LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
				if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
					TripDatabaseHelper dbHelper = new TripDatabaseHelper(ViewTripActivity.this);
					SQLiteDatabase db = dbHelper.getReadableDatabase();
					Cursor cursorForStatus = db.rawQuery("select t_status from trip where _id=?", new String[]{String.valueOf(trip.getTripId())});
					if(cursorForStatus.moveToFirst() && cursorForStatus.getString(0).equals("active")){
						Toast toast = Toast.makeText(getApplicationContext(), "Already Active", Toast.LENGTH_LONG);
						toast.show();
						cursorForStatus.close();
					}
					Cursor cursorForAnotherActiveTrip = db.rawQuery("select * from trip where t_status=?", new String[]{"active"});
					if(cursorForAnotherActiveTrip.moveToFirst() && cursorForAnotherActiveTrip.getCount() > 0){
						Toast toast = Toast.makeText(getApplicationContext(), "You have already an active trip", Toast.LENGTH_LONG);
						toast.show();
						cursorForAnotherActiveTrip.close();
					}else{
						dbHelper.updateTripStatus(trip, "active");
						PollTripService.setServiceAlarm(getApplicationContext(), true);
						Toast toast = Toast.makeText(getApplicationContext(), "Active Trip", Toast.LENGTH_LONG);
						toast.show();
					}
				}else{
					new AlertDialog.Builder(ViewTripActivity.this)
							.setTitle("GPS NOT ENABLED")
							.setCancelable(false)
							.setPositiveButton("OK", new DialogInterface.OnClickListener(){
								@Override
								public void onClick(DialogInterface dialog, int which){
									dialog.cancel();
								}
							}).show();
				}
			}
		});

		stop.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view){
				TripDatabaseHelper dbHelper = new TripDatabaseHelper(ViewTripActivity.this);
				SQLiteDatabase db = dbHelper.getReadableDatabase();
				Cursor cursor = db.rawQuery("select t_status from trip where _id=?", new String[]{String.valueOf(trip.getTripId())});
				if(cursor.moveToFirst() && cursor.getString(0).equals("inactive")){
					Toast toast = Toast.makeText(getApplicationContext(), "The trip isn't activated yet", Toast.LENGTH_LONG);
					toast.show();
				}else{
					dbHelper.updateTripStatus(trip, "inactive");
					PollTripService.setServiceAlarm(getApplicationContext(), false);
					Toast toast = Toast.makeText(getApplicationContext(), "The trip is stopped", Toast.LENGTH_LONG);
					toast.show();
				}
			}
		});


	}
	
	/**
	 * Create a Trip object via the recent trip that
	 * was passed to TripViewer via an Intent.
	 * 
	 * @param i The Intent that contains
	 * the most recent trip data.
	 * 
	 * @return The Trip that was most recently
	 * passed to TripViewer, or null if there
	 * is none.
	 */
	public Trip getTrip(Intent i) {
		
		// TODO - fill in here
		return (Trip)i.getParcelableExtra("tripDetails");

	}

	/**
	 * Populate the View using a Trip model.
	 * 
	 * @param trip The Trip model used to
	 * populate the View.
	 */
	public void viewTrip(Trip trip) {
		
		// TODO - fill in here
		tripName.setText(trip.getName());
		tripDescription.setText(trip.getDescriptionOfTrip());
		DateFormat dateFormat = DateFormat.getDateInstance();
		tripDate.setText(dateFormat.format(trip.getDate().getTime()));
		tripTime.setText(trip.getTime());
		tripLocation.setText(trip.getLocation());
		ArrayList<Person> people = trip.getPeople();
		StringBuilder sb = new StringBuilder();
		for(Person person: people){
			sb.append(person.getName());
			sb.append(" ");
		}
		tripFriend.setText(sb.toString());
	}

	public void goBack(){
		setResult(RESULT_OK);
		finish();
	}
}
