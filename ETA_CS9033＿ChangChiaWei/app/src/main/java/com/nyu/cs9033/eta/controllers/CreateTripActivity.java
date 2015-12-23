package com.nyu.cs9033.eta.controllers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.nyu.cs9033.eta.R;
import com.nyu.cs9033.eta.TripDatabaseHelper;
import com.nyu.cs9033.eta.models.Person;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class CreateTripActivity extends Activity implements DatePickerDialog.OnDateSetListener{
	
	private static final String TAG = "CreateTripActivity";
	private static final int REQUEST_CONTACT = 1;
	private static final int REQUEST_LOCATION = 2;
	private static final String SERVER_URL = "http://cs9033-homework.appspot.com";
	private Date storeDate;

	private ArrayList<String> selectedContactList = new ArrayList<String>();
	private ArrayList<String> selectedLocationInformation = new ArrayList<String>();
	EditText tripNameView, tripDescriptionView, tripTimeView, tripFriendView, searchAreaView, searchItemView;
	Button commitBtn, dropBtn, addFriendBtn, searchBtn, selectDateBtn;


	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Check for network connectivity
		ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if(networkInfo == null || !(networkInfo.isConnected())){
			new AlertDialog.Builder(CreateTripActivity.this)
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

		setContentView(R.layout.activity_create_trip);
		tripNameView = (EditText)findViewById(R.id.tripName);
		tripDescriptionView = (EditText)findViewById(R.id.tripDescription);
		tripTimeView = (EditText)findViewById(R.id.tripTime);
		//tripFriendView = (EditText)findViewById(R.id.tripFriend);
		searchAreaView = (EditText)findViewById(R.id.searchArea);
		searchItemView = (EditText)findViewById(R.id.searchItem);
		commitBtn = (Button)findViewById(R.id.commit);
		dropBtn	= (Button)findViewById(R.id.drop);
		addFriendBtn = (Button)findViewById(R.id.addFriendButton);
		searchBtn = (Button)findViewById(R.id.searchButton);
		selectDateBtn = (Button)findViewById(R.id.selectDateButton);

		commitBtn.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				Trip trip = createTrip();
				if(trip!=null){
					new TripAsyncTask(trip).execute(SERVER_URL);
				}else{
					cancelTrip();
				}
			}
		});

		dropBtn.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				cancelTrip();
			}
		});

		addFriendBtn.setOnClickListener(new View.OnClickListener(){
			public void onClick(View view){
				pickContact();
			}
		});

		searchBtn.setOnClickListener(new View.OnClickListener(){
			public void onClick(View view){
				searchLocation();
			}
		});

		selectDateBtn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view){
				showDatePickerDialog();
			}
		});


		// searchBtn is touchable only when searchAreaView and searchItemView are filled.
		searchAreaView.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				if (!searchAreaView.getText().toString().trim().isEmpty() && !searchItemView.getText().toString().trim().isEmpty()) {
					searchBtn.setEnabled(true);
				}
			}

			@Override
			public void afterTextChanged(Editable editable) {
				if (searchAreaView.getText().toString().trim().isEmpty() || searchItemView.getText().toString().trim().isEmpty()) {
					searchBtn.setEnabled(false);
				}
			}
		});

		// searchBtn is touchable only when searchAreaView and searchItemView are filled.
		searchItemView.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				if(!searchItemView.getText().toString().trim().isEmpty() && !searchAreaView.getText().toString().trim().isEmpty()){
					searchBtn.setEnabled(true);
				}
			}

			@Override
			public void afterTextChanged(Editable editable) {
				if(searchItemView.getText().toString().trim().isEmpty() || searchAreaView.getText().toString().trim().isEmpty()){
					searchBtn.setEnabled(false);
				}
			}
		});

	}

	/**
	 * The AsyncTask sends the "CREATE_TRIP" JSON request to the Web Server to get a TripID of the newly created trip
	 * and saves the trip into the database
	 */
	private class TripAsyncTask extends AsyncTask<String, Void, String>{

		private Trip trip = new Trip();

		protected TripAsyncTask(Trip trip){
			this.trip = trip;
		}

		@Override
		protected String doInBackground(String...urls){
			try{
				return getJsonTripId(urls[0]);
			}catch(IOException e){
				System.out.printf("Exception - %s%n", e.getMessage());
				return "Unable to Retrieve getTripId from the server";
			}
		}

		@Override
		protected void onPostExecute(String result){
			try{
				JSONObject createTripResponse = new JSONObject(result);
				trip.setTripId(createTripResponse.getLong("trip_id"));
				if(CreateTripActivity.this.saveTrip(trip)){
					Toast toast = Toast.makeText(getApplicationContext(), "Successfully", Toast.LENGTH_LONG);
					toast.show();
				}else{
					Toast toast = Toast.makeText(getApplicationContext(), "Failure", Toast.LENGTH_LONG);
					toast.show();
				}
				finish();
			}catch(JSONException e){
				System.out.printf("Exception - %s%n", e.getMessage());
			}
		}

		public String getJsonTripId(String url) throws IOException{

			URL	connectionUrl = new URL(url);
			String RequestString = null;
			HttpURLConnection connection = (HttpURLConnection)connectionUrl.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");

			try {
				JSONObject createTripRequest = new JSONObject();
				createTripRequest.put("command", "CREATE_TRIP");
				createTripRequest.put("location", new JSONArray(trip.getLocationInformation()));
				createTripRequest.put("datetime", trip.getDate().getTime());
				createTripRequest.put("people", new JSONArray(trip.getPeopleName(trip.getPeople())));
				RequestString = createTripRequest.toString();
			}catch(JSONException e){
				connection.disconnect();
				System.out.printf("Exception - %s%n", e.getMessage());
			}

			try{
				OutputStreamWriter output = new OutputStreamWriter(connection.getOutputStream());
				output.write(RequestString);
				output.close();
				if(connection.getResponseCode()!=HttpURLConnection.HTTP_OK){
					return null;
				}
				InputStream input = connection.getInputStream();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input));
				String dataFromServer;
				StringBuilder responseString = new StringBuilder();
				while((dataFromServer = bufferedReader.readLine())!=null){
					responseString.append(dataFromServer);
				}
				input.close();
				return responseString.toString();
			}finally{
				connection.disconnect();
			}
		}

	}


	
	/**
	 * This method should be used to
	 * instantiate a Trip model object.
	 * 
	 * @return The Trip as represented
	 * by the View.
	 */
	public Trip createTrip() {
		// TODO - fill in here
		String tripName = tripNameView.getText().toString();
		String tripDescription = tripDescriptionView.getText().toString();
		Date tripDate = storeDate;
		String tripTime = tripTimeView.getText().toString();
		ArrayList<String> location = new ArrayList<String>();
		ArrayList<Person> people = new ArrayList<Person>();
		if(selectedLocationInformation!=null){
			for(String information : selectedLocationInformation){
				location.add(information);
			}
		}
		if(selectedContactList!=null){
			for(String name : selectedContactList){
				Person person = new Person(name);
				people.add(person);
			}
		}
		if(!tripName.equals("") && !tripDescription.equals("") && tripDate!=null && !tripTime.equals("") && location!=null && people!=null){
			Trip trip = new Trip();
			trip.setName(tripName);
			trip.setDescriptionOfTrip(tripDescription);
			trip.setDate(tripDate);
			trip.setTime(tripTime);
			trip.setLocation(location);
			trip.setLocationInformation(location);
			trip.setPeople(people);
			trip.setStatus("inactive");
			return trip;
		}
		return null;
	}

	/**
	 * For HW2 you should treat this method as a 
	 * way of sending the Trip data back to the
	 * main Activity.
	 * 
	 * Note: If you call finish() here the Activity 
	 * will end and pass an Intent back to the
	 * previous Activity using setResult().
	 * 
	 * @return whether the Trip was successfully 
	 * saved.
	 */
	public boolean saveTrip(Trip trip) {
	
		// TODO - fill in here
		if(trip!=null){
			TripDatabaseHelper tripDatabaseHelper = new TripDatabaseHelper(CreateTripActivity.this);
			long tripID = tripDatabaseHelper.insertTrip(trip);
			tripDatabaseHelper.insertLocationDetails(tripID, trip);
			ArrayList<Person> people = trip.getPeople();
			for(Person person : people){
				tripDatabaseHelper.insertPerson(tripID, person);
			}
			tripDatabaseHelper.close();
			return true;
		}
		return false;
	}

	/**
	 * This method should be used when a
	 * user wants to cancel the creation of
	 * a Trip.
	 * 
	 * Note: You most likely want to call this
	 * if your activity dies during the process
	 * of a trip creation or if a cancel/back
	 * button event occurs. Should return to
	 * the previous activity without a result
	 * using finish() and setResult().
	 */
	public void cancelTrip() {
	
		// TODO - fill in here
		setResult(RESULT_CANCELED);
		Toast toast = Toast.makeText(getApplicationContext(), "Fail to create a trip !", Toast.LENGTH_LONG);
		toast.show();
		finish();
	}

	//send an implicit intent to pick a contact name from the Phone's 'Contact Book'
	public void pickContact(){
		Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
		startActivityForResult(pickContactIntent, REQUEST_CONTACT);
	}

	//send an implicit intent to search location from the 'HW3API'
	public void searchLocation(){
		Uri uri = Uri.parse("location://com.example.nyu.hw3api");
		Intent searchLocationIntent = new Intent(Intent.ACTION_VIEW, uri);
		searchLocationIntent.putExtra("searchVal", searchAreaView.getText() + "::" + searchItemView.getText());
		startActivityForResult(searchLocationIntent, REQUEST_LOCATION);
	}

	//display a Date Picker Dialog Fragment to select a date
	public void showDatePickerDialog(){
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		DialogFragment newFragment = DatePickerDialogFragment.newInstance(CreateTripActivity.this);
		newFragment.show(ft, "Date Fragment");
	}

	public static class DatePickerDialogFragment extends DialogFragment {

		protected DatePickerDialog.OnDateSetListener mDateSetListener;

		//Empty default constructor to prevent our app from crashing when the device is rotated.
		public DatePickerDialogFragment() {
		}

		//static method to create an instance of the DatePickerDialogFragment in the outer class
		public static DialogFragment newInstance(DatePickerDialog.OnDateSetListener callback){
			DatePickerDialogFragment dFragment = new DatePickerDialogFragment();
			dFragment.mDateSetListener = callback;
			return dFragment;
		}

		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Calendar cal = Calendar.getInstance();

			return new DatePickerDialog(getActivity(),mDateSetListener, cal.get(Calendar.YEAR),
					cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
		}
	}

	//set the valid trip date selected by the user
	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

		Calendar cal = new GregorianCalendar(year, monthOfYear, dayOfMonth);
		final Calendar now = new GregorianCalendar();

		//check if the user selects a past trip date and show an error
		if ((cal.get(Calendar.YEAR) < now.get(Calendar.YEAR))
				|| ((cal.get(Calendar.YEAR) == now.get(Calendar.YEAR))
				&&(cal.get(Calendar.MONTH) < now.get(Calendar.MONTH))
				|| ((cal.get(Calendar.YEAR) == now.get(Calendar.YEAR))
				&&(cal.get(Calendar.MONTH) == now.get(Calendar.MONTH))
				&&(cal.get(Calendar.DAY_OF_MONTH) < now.get(Calendar.DAY_OF_MONTH))))){
			Toast.makeText(this, "Cannot go back to the past! ", Toast.LENGTH_SHORT).show();
		}
		else {
			storeDate = cal.getTime();
		}
	}

	//get the result intents back from Phone's 'Contact Book' & 'HW3API' and set the person & location
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(data!=null){
			if (requestCode == REQUEST_CONTACT && resultCode == Activity.RESULT_OK){
				Uri contactUri = data.getData();
				String[] queryFields = new String[]{
						ContactsContract.Contacts.DISPLAY_NAME
				};
				Cursor c = getContentResolver().query(contactUri, queryFields, null, null, null);
				try{
					if(c.getCount() == 0){
						return;
					}
					c.moveToFirst();
					String name = c.getString(0);
					if (name != null) {
						//check to verify if the user enters a duplicate friend and show an error via a Toast widget
						if (selectedContactList.contains(name)){
							Toast toast = Toast.makeText(getApplicationContext(), "Duplicated name " , Toast.LENGTH_LONG);
							toast.show();
						}
						else {
							selectedContactList.add(name);
							Toast toast = Toast.makeText(getApplicationContext(), "Friend: " +  name + " added successfully", Toast.LENGTH_SHORT);
							toast.show();
						}
					}
				} finally{
					c.close();
				}
			}
			if(requestCode == REQUEST_LOCATION){
				Toast toast = Toast.makeText(getApplicationContext(), "Location added successfully" , Toast.LENGTH_LONG);
				toast.show();
				selectedLocationInformation = data.getStringArrayListExtra("retVal");
			}
		}
	}
}
