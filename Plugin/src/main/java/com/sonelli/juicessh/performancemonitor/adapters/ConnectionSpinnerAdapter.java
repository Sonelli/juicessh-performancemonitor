package com.sonelli.juicessh.performancemonitor.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sonelli.juicessh.performancemonitor.R;
import com.sonelli.juicessh.pluginlibrary.PluginContract;

import java.util.UUID;

/**
 * Loads JuiceSSH connections from a cursor and provides an adapter
 * that can be used in a ListView or Spinner. You can optionally pass
 * a {@link com.sonelli.juicessh.pluginlibrary.PluginContract.Connections.TYPE}
 * to the constructor to filter down the list to a specific connection type.
 */
public class ConnectionSpinnerAdapter extends CursorAdapter {

    private LayoutInflater inflater;
    private int type;

    /**
     * Loads all JuiceSSH connections from a cursor ready for a ListView/Spinner
     */
    public ConnectionSpinnerAdapter(Context context) {
        this(context, -1);
    }

    /**
     * Loads JuiceSSH connections of a specific type ready for a ListView/Spinner
     */
    public ConnectionSpinnerAdapter(Context context, int type){
        super(context, null, false);
        this.type = type;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Returns the UUID connection ID for the item at a given position, or null if not available
     * @param position Position of the item to fetch the id of
     * @return The UUID connection ID or null if it doesn't exist
     */
    public UUID getConnectionId(int position) {

        UUID id = null;

        if(getCursor() != null){
            if(getCursor().moveToPosition(position)) {
                int idIndex = getCursor().getColumnIndex(PluginContract.Connections.COLUMN_ID);
                if (idIndex > -1) {
                    id = UUID.fromString(getCursor().getString(idIndex));
                }
            }
        }

        return id;

    }

    /**
     * Returns the type of connection form the item at a given position, or -1 if not available
     * @param position Position of the item to fetch the type of
     * @return The connection type or -1 if it doesn't exist
     */
    public int getConnectionType(int position) {

        int type = -1;

        if(getCursor() != null){
            if(getCursor().moveToPosition(position)) {
                int typeIndex = getCursor().getColumnIndex(PluginContract.Connections.COLUMN_TYPE);
                if (typeIndex > -1) {
                    type = getCursor().getInt(typeIndex);
                }
            }
        }

        return type;

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
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        int nameColumn = cursor.getColumnIndex(PluginContract.Connections.COLUMN_NAME);
        int typeColumn = cursor.getColumnIndex(PluginContract.Connections.COLUMN_TYPE);

        if(nameColumn > -1){

            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            String name = cursor.getString(nameColumn);
            textView.setText(name);

            if(type != -1){
                if(cursor.getInt(typeColumn) != type){
                    textView.setTextColor(context.getResources().getColor(android.R.color.tab_indicator_text));
                } else {
                    textView.setTextColor(context.getResources().getColor(android.R.color.black));
                }
            }

        }

    }

}
