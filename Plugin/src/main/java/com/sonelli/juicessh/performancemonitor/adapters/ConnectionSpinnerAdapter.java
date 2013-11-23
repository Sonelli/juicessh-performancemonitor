package com.sonelli.juicessh.performancemonitor.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sonelli.juicessh.performancemonitor.R;
import com.sonelli.juicessh.pluginlibrary.PluginClient;
import com.sonelli.juicessh.pluginlibrary.PluginContract;

import java.util.UUID;

/**
 * Loads JuiceSSH connections from a cursor and provides an adapter
 * that can be used in a ListView or Spinner. You can optionally pass
 * a {@link com.sonelli.juicessh.pluginlibrary.PluginContract.Connections.TYPE}
 * to the constructor to filter down the list to a specific connection type.
 */
public class ConnectionSpinnerAdapter extends CursorAdapter {

    public static final String TAG = "ConnectionAdapter";

    private LayoutInflater inflater;
    private int type;

    /**
     * Loads all JuiceSSH connections from a cursor ready for a ListView/Spinner
     * @param context
     */
    public ConnectionSpinnerAdapter(Context context) {
        this(context, -1);
    }

    /**
     * Loads JuiceSSH connections of a specific type ready for a ListView/Spinner
     * @param context
     * @param type
     */
    public ConnectionSpinnerAdapter(Context context, int type){
        super(context, null, false);
        this.type = type;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Returns the UUID connection ID for the item at a given position, or null if not available
     * @param position
     * @return The UUID connection ID
     */
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
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return inflater.inflate(R.layout.spinner_list_item, null, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        int nameColumn = cursor.getColumnIndex(PluginContract.Connections.NAME);
        int typeColumn = cursor.getColumnIndex(PluginContract.Connections.TYPE);

        if(nameColumn > -1){

            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            String name = cursor.getString(nameColumn);
            textView.setText(name);

            // If the connection type != SSH (ie, it's a Mosh/telnet/local one)
            // then disable the item in the list so that the plugin user cannot
            // select it - as sending commands to non-ssh connections is not supported.

            // Oddly, we need to make setClickable(true) on the TextView & surrounding layout.
            // This will force the TextView to take the touch input rather than the Spinner

            if(type != -1){
                if(cursor.getInt(typeColumn) != type){
                    view.setEnabled(false);
                    view.setClickable(true);
                    textView.setEnabled(false);
                    textView.setClickable(true);
                    textView.setTextColor(context.getResources().getColor(android.R.color.tab_indicator_text));
                } else {
                    view.setEnabled(true);
                    view.setClickable(false);
                    textView.setEnabled(true);
                    textView.setClickable(false);
                    textView.setTextColor(context.getResources().getColor(android.R.color.white));
                }
            }

        }

    }

}
