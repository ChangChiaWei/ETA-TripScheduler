package com.nyu.cs9033.eta.controllers;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;

import com.nyu.cs9033.eta.R;
import com.nyu.cs9033.eta.TripDatabaseHelper;
import com.nyu.cs9033.eta.adaptors.ListViewCursorAdaptor;
import com.nyu.cs9033.eta.models.Person;
import com.nyu.cs9033.eta.models.Trip;

import java.util.ArrayList;

/**
 * Created by chia-weichang on 10/21/15.
 */
public class TripHistoryActivity extends Activity {
    Button homebtn;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_history);

        homebtn = (Button)findViewById(R.id.buttonHome);
        ListView tripListViewPresent = (ListView) findViewById(R.id.listPresent);
        ListView tripListViewFuture = (ListView) findViewById(R.id.listFuture);
        ListView tripListViewPast = (ListView) findViewById(R.id.listPast);

        final TripDatabaseHelper tripDatabaseHelper = new TripDatabaseHelper(this);
        SQLiteDatabase db = tripDatabaseHelper.getWritableDatabase();

        //Query for today's trips from the database and get a cursor back
        Cursor currentItemCursor = db.rawQuery("select * from trip where date(datetime(t_date/1000, 'unixepoch', 'localtime')) = date('now') order by t_date", null);

        //Query for future trips from the database and get a cursor back
        Cursor futureItemCursor = db.rawQuery("select * from trip where date(datetime(t_date/1000, 'unixepoch', 'localtime')) > date('now') order by t_date", null);

        //Query for past trips from the database and get a cursor back
        Cursor pastItemCursor = db.rawQuery("select * from trip where date(datetime(t_date/1000, 'unixepoch', 'localtime')) < date('now') order by t_date", null);

        // Setup cursor adapters
        ListViewCursorAdaptor cursorAdaptorPresent = new ListViewCursorAdaptor(this, currentItemCursor, 0);
        ListViewCursorAdaptor cursorAdaptorFuture = new ListViewCursorAdaptor(this, futureItemCursor, 0);
        ListViewCursorAdaptor cursorAdaptorPast = new ListViewCursorAdaptor(this, pastItemCursor, 0);

        //Attach cursor adapters to the respective ListViews
        tripListViewPresent.setAdapter(cursorAdaptorPresent);
        tripListViewFuture.setAdapter(cursorAdaptorFuture);
        tripListViewPast.setAdapter(cursorAdaptorPast);

        //adjust the height of the ListViews
        setListViewHeight(tripListViewPresent);
        setListViewHeight(tripListViewFuture);
        setListViewHeight(tripListViewPast);

        homebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        tripListViewPresent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Trip tripObject = tripDatabaseHelper.getTripObjectFromDB(id);
                ArrayList<String> locationObject = tripDatabaseHelper.getLocationInformationFromDB(id);
                ArrayList<Person> people = tripDatabaseHelper.getPeopleFromDB(id);
                tripObject.setPeople(people);
                tripObject.setLocation(locationObject);
                Intent viewTripIntent = new Intent(getBaseContext(), ViewTripActivity.class);
                viewTripIntent.putExtra("tripDetails", tripObject);
                startActivity(viewTripIntent);

            }
        });

        tripListViewFuture.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                Trip tripObject = tripDatabaseHelper.getTripObjectFromDB(id);
                ArrayList<String> locationObject = tripDatabaseHelper.getLocationInformationFromDB(id);
                ArrayList<Person> people = tripDatabaseHelper.getPeopleFromDB(id);
                tripObject.setPeople(people);
                tripObject.setLocation(locationObject);
                Intent viewTripIntent = new Intent(getBaseContext(), ViewTripActivity.class);
                viewTripIntent.putExtra("tripDetails", tripObject);
                startActivity(viewTripIntent);
            }
        });

        tripListViewPast.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Trip tripObject = tripDatabaseHelper.getTripObjectFromDB(id);
                ArrayList<String> locationObject = tripDatabaseHelper.getLocationInformationFromDB(id);
                ArrayList<Person> people = tripDatabaseHelper.getPeopleFromDB(id);
                tripObject.setPeople(people);
                tripObject.setLocation(locationObject);
                Intent viewTripIntent = new Intent(getBaseContext(), ViewTripActivity.class);
                viewTripIntent.putExtra("tripDetails", tripObject);
                startActivity(viewTripIntent);
            }
        });

        tripDatabaseHelper.close();
    }

    //method to set the height of a ListView based on the number of children items displayed in it
    public void setListViewHeight(ListView listView) {
        CursorAdapter cursorAdapter = (CursorAdapter) listView.getAdapter();
        if (cursorAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < cursorAdapter.getCount(); i++) {
            View listItem = cursorAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (cursorAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}
