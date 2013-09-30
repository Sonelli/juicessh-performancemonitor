package com.sonelli.juicessh.pluginexample.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.sonelli.juicessh.pluginexample.contracts.JuiceSSHContract;

import java.util.UUID;

public class ConnectionSpinnerAdapter extends CursorAdapter {

    public static final String TAG = "ConnectionSnipperAdapter";

    private LayoutInflater inflater;

    public ConnectionSpinnerAdapter(Context context) {
        super(context, null, false);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public UUID getConnectionId(int position) {
        try {
            getCursor().moveToPosition(position);
            return UUID.fromString(getCursor().getString(0));
        } catch (NullPointerException e){
            Log.e(TAG, "Cursor is null or empty");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return inflater.inflate(android.R.layout.simple_spinner_item, null, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int nameColumn = cursor.getColumnIndex(JuiceSSHContract.Connections.NAME);
        if(nameColumn > -1){
            String name = cursor.getString(nameColumn);
            ((TextView) view).setText(name);
        }
    }

}
