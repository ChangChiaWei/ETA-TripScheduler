package com.nyu.cs9033.eta.adaptors;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.nyu.cs9033.eta.R;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by chia-weichang on 10/21/15.
 */
public class ListViewCursorAdaptor extends CursorAdapter{

    private LayoutInflater layoutInflater;

    public ListViewCursorAdaptor(Context context, Cursor cursor, int flags){
        super(context, cursor, flags);
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    //this method is used to inflate a new view and return it
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return layoutInflater.inflate(R.layout.custom_list_item, parent, false);
    }

    //this method is used to bind the required data from the cursor to the given view
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView txtTripName = (TextView) view.findViewById(R.id.item_tripName);
        TextView txtTripDate = (TextView) view.findViewById(R.id.item_tripDate);

        String itemTripName = cursor.getString(cursor.getColumnIndexOrThrow("t_name"));
        long itemTripDate = cursor.getLong(cursor.getColumnIndexOrThrow("t_date"));

        final Date date = new Date(itemTripDate);

        DateFormat dateFormat = DateFormat.getDateInstance();

        txtTripName.setText(itemTripName);

        //format the trip date into a user friendly format and display it on the view/UI
        txtTripDate.setText(dateFormat.format(date.getTime()));

    }

}
