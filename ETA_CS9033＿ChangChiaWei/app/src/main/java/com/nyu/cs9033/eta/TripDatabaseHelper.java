package com.nyu.cs9033.eta;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nyu.cs9033.eta.models.Person;
import com.nyu.cs9033.eta.models.Trip;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by chia-weichang on 10/20/15.
 */
public class TripDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "TripDatabaseHelper";
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "trips";

    //trip table
    private static final String TABLE_TRIP = "trip";
    private static final String COLUMN_TRIP_ID = "_id";
    private static final String COLUMN_TRIP_NAME = "t_name";
    private static final String COLUMN_TRIP_DESCRIPTION = "t_description";
    private static final String COLUMN_TRIP_DATE = "t_date";
    private static final String COLUMN_TRIP_TIME = "t_time";
    private static final String COLUMN_TRIP_STATUS = "t_status";

    //locationInformation table
    private static final String TABLE_LOCATION = "locDetails";
    private static final String COLUMN_LOC_TRIPID = "loc_trip_id";
    private static final String COLUMN_LOC_NAME = "loc_name";
    private static final String COLUMN_LOC_ADDRESS = "address";
    private static final String COLUMN_LOC_LAT = "latitude";
    private static final String COLUMN_LOC_LONG = "longitude";
    private static final String COLUMN_LOC_PROVIDER = "provider";

    //person table
    private static final String TABLE_PERSON = "person";
    private static final String COLUMN_PERSON_TRIPID = "trip_id";
    private static final String COLUMN_PERSON_NAME = "p_name";

    public TripDatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        //create trip table
        db.execSQL("CREATE TABLE " + TABLE_TRIP + "("
                + COLUMN_TRIP_ID + " INTEGER PRIMARY KEY, "
                + COLUMN_TRIP_NAME + " text, "
                + COLUMN_TRIP_DESCRIPTION + " text, "
                + COLUMN_TRIP_DATE + " integer, "
                + COLUMN_TRIP_TIME + " text, "
                + COLUMN_TRIP_STATUS + " text)");

        //create location table
        db.execSQL("CREATE TABLE " + TABLE_LOCATION + "("
                + COLUMN_LOC_TRIPID + " integer references trip(_id), "
                + COLUMN_LOC_NAME + " text, "
                + COLUMN_LOC_ADDRESS + " text, "
                + COLUMN_LOC_LAT + " text, "
                + COLUMN_LOC_LONG + " text, "
                + COLUMN_LOC_PROVIDER + " text)");

        //create person table
        db.execSQL("create table " + TABLE_PERSON + "("
                + COLUMN_PERSON_TRIPID + " integer references trip(_id), "
                + COLUMN_PERSON_NAME + " text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        //drop older table if exists
        db.execSQL(" DROP TABLE IF EXISTS " + TABLE_TRIP);
        db.execSQL(" DROP TABLE IF EXISTS " + TABLE_LOCATION);
        db.execSQL(" DROP TABLE IF EXISTS " + TABLE_PERSON);

        //create tables again
        onCreate(db);
    }

    //insert the trip into trip table and get the tripId of the inserted row
    public long insertTrip(Trip trip){
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TRIP_ID, trip.getTripId());
        cv.put(COLUMN_TRIP_NAME, trip.getName());
        cv.put(COLUMN_TRIP_DESCRIPTION, trip.getDescriptionOfTrip());
        cv.put(COLUMN_TRIP_DATE, trip.getDate().getTime());
        cv.put(COLUMN_TRIP_TIME, trip.getTime());
        cv.put(COLUMN_TRIP_STATUS, trip.getStatus());
        return getWritableDatabase().insert(TABLE_TRIP, null, cv);
    }

    //insert the locationInformation into locDetails table based on the related tripId
    public void insertLocationDetails(long tripId, Trip trip) {

        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("insert into " + TABLE_LOCATION +
                "(" + COLUMN_LOC_TRIPID + ", "
                + COLUMN_LOC_NAME + ", "
                + COLUMN_LOC_ADDRESS + ", "
                + COLUMN_LOC_LAT + ", "
                + COLUMN_LOC_LONG + ", "
                + COLUMN_LOC_PROVIDER + ")"
                + " values(" + tripId + ", '"
                + trip.getLocation() + "' , '"
                + trip.getLocationAddress() + "' , '"
                + trip.getLocationLatitude() + "' , '"
                + trip.getLocationLongitude() + "' , 'HW3API')");
    }

    //insert the person into person table based on the related tripId
    public void insertPerson(long tripId, Person person) {

        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("insert into " + TABLE_PERSON +
                "(" + COLUMN_PERSON_TRIPID + ", "
                + COLUMN_PERSON_NAME + ")"
                + " values(" + tripId + ", '" + person.getName() + "')");
    }

    //get Trip Object based on the tripID stored in the database
    public Trip getTripObjectFromDB(long tripID){
        Trip trip = new Trip();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try{
            cursor = db.rawQuery("select * from " + TABLE_TRIP, null);
            if(cursor.getCount()!=0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    if ((cursor.getLong(0) == tripID)) {
                        ArrayList<String> tripLocationInformation = this.getLocationInformationFromDB(cursor.getLong(0));
                        ArrayList<Person> people = this.getPeopleFromDB(cursor.getLong(0));
                        trip.setTripId(cursor.getLong(0));
                        trip.setName(cursor.getString(1));
                        trip.setDescriptionOfTrip((cursor.getString(2)));
                        trip.setLocationInformation(tripLocationInformation);
                        trip.setDate(new Date(cursor.getLong(3)));
                        trip.setTime(cursor.getString(4));
                        trip.setPeople(people);
                        trip.setStatus(cursor.getString(5));
                    }
                }
            }
        }finally{
            if(cursor != null) {
                cursor.close();
            }
        }
        return trip;
    }

    //get locationInformation based on the related tripID stored in the database
    public ArrayList<String> getLocationInformationFromDB(long tripID){
        ArrayList<String> locationInformation = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try{
            cursor = db.rawQuery("select * from " + TABLE_LOCATION, null);
            if(cursor.getCount()!=0){
                for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
                    if(cursor.getLong(0) == tripID){
                        locationInformation.add(cursor.getString(1));
                        locationInformation.add(cursor.getString(2));
                        locationInformation.add(cursor.getString(3));
                        locationInformation.add(cursor.getString(4));
                        locationInformation.add(cursor.getString(5));
                    }
                }
            }
        }finally {
            if(cursor!=null){
                cursor.close();
            }
        }
        return locationInformation;
    }

    //get person based on the related tripID stored in the database
    public ArrayList<Person> getPeopleFromDB(long tripID){
        ArrayList<Person> people = new ArrayList<Person>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try{
            cursor = db.rawQuery("Select * from " + TABLE_PERSON, null);
            if(cursor.getCount()!=0){
                for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
                    if(cursor.getLong(0) == tripID){
                        Person person = new Person();
                        person.setName(cursor.getString(1));
                        people.add(person);
                    }
                }
            }
        }finally{
            if(cursor!=null){
                cursor.close();
            }
        }
        return people;
    }

    public int updateTripStatus(Trip trip, String tripStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TRIP_STATUS, tripStatus);
        return db.update("trip", cv, COLUMN_TRIP_ID + "=?",
                new String[]{String.valueOf(trip.getTripId())});
    }

    public long getActiveTripIDFromDB() {
        long activeTripID = -1;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select _id from trip where t_status=?", new String[]{"active"});
        if (cursor.getCount() != 0 && cursor.moveToFirst()) {
            activeTripID = cursor.getLong(0);
            cursor.close();
            db.close();
        }
        return activeTripID;
    }

}
