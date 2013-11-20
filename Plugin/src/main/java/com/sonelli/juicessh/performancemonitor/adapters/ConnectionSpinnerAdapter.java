package com.sonelli.juicessh.performancemonitor.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sonelli.juicessh.performancemonitor.R;
import com.sonelli.juicessh.pluginlibrary.PluginContract;

import java.util.UUID;

public class ConnectionSpinnerAdapter extends CursorAdapter {

    public static final String TAG = "ConnectionAdapter";

    private LayoutInflater inflater;

    public ConnectionSpinnerAdapter(Context context) {
        super(context, null, false);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public UUID getConnectionId(int position) {

        UUID id = null;

        if(getCursor() != null){
            getCursor().moveToPosition(position);
            int idIndex = getCursor().getColumnIndex(PluginContract.Connections.ID);
            if(idIndex > -1){
                id = UUID.fromString(getCursor().getString(idIndex));
            }
        }

        return id;

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return inflater.inflate(R.layout.spinner_list_item, null, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        int nameColumn = cursor.getColumnIndex(PluginContract.Connections.NAME);

        if(nameColumn > -1){
            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            String name = cursor.getString(nameColumn);
            textView.setText(name);
        }

    }

}
