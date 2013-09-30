package com.sonelli.juicessh.pluginexample.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.sonelli.juicessh.pluginexample.contracts.JuiceSSHContract;

import java.util.UUID;

public class ConnectionSpinnerAdapter extends CursorAdapter {

    private LayoutInflater inflater;

    public ConnectionSpinnerAdapter(Context context) {
        super(context, null, false);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public UUID getConnectionId(int position) {
        getCursor().moveToPosition(position);
        return UUID.fromString(getCursor().getString(0));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return inflater.inflate(android.R.layout.simple_spinner_item, null, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        try {
            String name = cursor.getString(cursor.getColumnIndex(JuiceSSHContract.Connections.NAME));
            ((TextView) view).setText(name);
        } catch (IllegalStateException e){
            e.printStackTrace();
        }
    }
}
