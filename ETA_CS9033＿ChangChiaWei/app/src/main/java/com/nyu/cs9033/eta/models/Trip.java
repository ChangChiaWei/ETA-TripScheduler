package com.nyu.cs9033.eta.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;

public class Trip implements Parcelable {
	
	// Member fields should exist here, what else do you need for a trip?
	// Please add additional fields
	private long tripId;
	private String name;
	private String descriptionOfTrip;
	private Date date;
	private String time;
	private String locationName;
	private String locationAddress;
	private String locationLatitude;
	private String locationLongitude;
	private ArrayList<Person> people;
	private ArrayList<String> locationInformation = new ArrayList<String>();
	private String status;

	
	/**
	 * Parcelable creator. Do not modify this function.
	 */
	public static final Parcelable.Creator<Trip> CREATOR = new Parcelable.Creator<Trip>() {
		public Trip createFromParcel(Parcel p) {
			return new Trip(p);
		}

		public Trip[] newArray(int size) {
			return new Trip[size];
		}
	};
	
	/**
	 * Create a Trip model object from a Parcel. This
	 * function is called via the Parcelable creator.
	 * 
	 * @param p The Parcel used to populate the
	 * Model fields.
	 */
	public Trip(Parcel p) {
		
		// TODO - fill in here
		this.tripId = p.readLong();
		name = p.readString();
		descriptionOfTrip = p.readString();
		date = new Date(p.readLong());
		time = p.readString();
		p.readStringList(locationInformation);
		locationName = p.readString();
		locationAddress = p.readString();
		locationLatitude = p.readString();
		locationLongitude = p.readString();
		people = new ArrayList<Person>();
		p.readTypedList(people, Person.CREATOR);
		status = p.readString();
	}
	
	/**
	 * Create a Trip model object from arguments
	 * 
	 * @param name  Add arbitrary number of arguments to
	 * instantiate Trip class based on member variables.
	 */
	public Trip(String name, String descriptionOfTrip, Date date, String time, ArrayList<String> locationInformation, ArrayList<Person> people, String status) {
		
		// TODO - fill in here, please note you must have more arguments here
		this.name = name;
		this.descriptionOfTrip = descriptionOfTrip;
		this.date = date;
		this.time = time;
		this.locationInformation = locationInformation;
		this.people = people;
		this.status = status;
	}

	public Trip(){

	}

	/**
	 * Serialize Trip object by using writeToParcel. 
	 * This function is automatically called by the
	 * system when the object is serialized.
	 * 
	 * @param dest Parcel object that gets written on 
	 * serialization. Use functions to write out the
	 * object stored via your member variables. 
	 * 
	 * @param flags Additional flags about how the object 
	 * should be written. May be 0 or PARCELABLE_WRITE_RETURN_VALUE.
	 * In our case, you should be just passing 0.
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		
		// TODO - fill in here
		dest.writeLong(tripId);
		dest.writeString(name);
		dest.writeString(descriptionOfTrip);
		dest.writeLong(date.getTime());
		dest.writeString(time);
		dest.writeStringList(locationInformation);
		dest.writeString(locationName);
		dest.writeString(locationAddress);
		dest.writeString(locationLatitude);
		dest.writeString(locationLongitude);
		dest.writeTypedList(people);
		dest.writeString(status);
	}
	
	/**
	 * Feel free to add additional functions as necessary below.
	 */

	public long getTripId(){
		return tripId;
	}

	public String getName(){
		return name;
	}

	public String getDescriptionOfTrip(){
		return descriptionOfTrip;
	}

	public Date getDate(){
		return (Date)this.date.clone();
	}

	public String getTime(){
		return time;
	}

	public String getLocation(){
		return locationInformation.get(0);
	}

	public ArrayList<String> getLocationInformation(){
		return locationInformation;
	}

	public String getLocationAddress(){
		return locationInformation.get(1);
	}

	public String getLocationLatitude(){
		return locationInformation.get(2);
	}

	public String getLocationLongitude(){
		return locationInformation.get(3);
	}

	public ArrayList<Person> getPeople(){
		return people;
	}

	public ArrayList<String> getPeopleName(ArrayList<Person> people){
		ArrayList<String> peopleNames = new ArrayList<String>();
		for(Person person : people){
			peopleNames.add(person.getName());
		}
		return peopleNames;
	}

	public String getStatus(){
		return status;
	}

	public void setTripId(long tripId){
		this.tripId = tripId;
	}

	public void setName(String name){
		this.name = name;
	}

	public void setDescriptionOfTrip(String descriptionOfTrip){
		this.descriptionOfTrip = descriptionOfTrip;
	}

	public void setDate(Date date){
		this.date = date;
	}

	public void setTime(String time){
		this.time = time;
	}

	public void setLocation(ArrayList<String> locationInformation){
		this.locationInformation = locationInformation;
	}

	public void setLocationInformation(ArrayList<String> locationInformation){
		this.locationName = locationInformation.get(0);
		this.locationAddress = locationInformation.get(1);
		this.locationLatitude = locationInformation.get(2);
		this.locationLongitude = locationInformation.get(3);
	}

	public void setPeople(ArrayList<Person> people){
		this.people = people;
	}

	public void setStatus(String status){
		this.status = status;
	}

	/**
	 * Do not implement
	 */
	@Override
	public int describeContents() {
		// Do not implement!
		return 0;
	}
}
