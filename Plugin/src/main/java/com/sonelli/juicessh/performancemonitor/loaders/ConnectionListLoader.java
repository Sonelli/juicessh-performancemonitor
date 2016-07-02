package com.sonelli.juicessh.performancemonitor.loaders;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.sonelli.juicessh.performancemonitor.adapters.ConnectionSpinnerAdapter;
import com.sonelli.juicessh.pluginlibrary.PluginContract;

public class ConnectionListLoader implements LoaderManager.LoaderCallbacks<Cursor> {

    private Context context;
    private ConnectionSpinnerAdapter adapter;

    /**
     * Creates a {@link android.support.v4.content.Loader} to fetch all connection
     * items from the database on a background thread (similar to an {@link android.os.AsyncTask}.
     * Once the connections are loaded it will populate the associated listview/spinner adapter.
     */
    public ConnectionListLoader(Context context, ConnectionSpinnerAdapter adapter) {
        this.context = context;
        this.adapter = adapter;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        return new CursorLoader(
                context,
                PluginContract.Connections.CONTENT_URI,
                PluginContract.Connections.PROJECTION,
                null,
                null,
                PluginContract.Connections.SORT_ORDER_DEFAULT
        );

    }

    /**
     * Swaps out the associated adapter's cursor for a populated one
     * once the loader has fetched all of the connections from the DB
     */
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if(adapter != null){
            adapter.swapCursor(cursor);
        }
    }

    /**
     * Flip back to the original state before connections were loaded
     * and set the associated adapter's cursor to null.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        if(adapter != null){
            adapter.swapCursor(null);
        }
    }
}
