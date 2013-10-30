package com.sonelli.juicessh.pluginexample.loaders;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.sonelli.juicessh.pluginexample.adapters.ConnectionSpinnerAdapter;
import com.sonelli.juicessh.pluginexample.contracts.JuiceSSHContract;

public class ConnectionListLoader implements LoaderManager.LoaderCallbacks<Cursor> {

    private Context context;
    private ConnectionSpinnerAdapter adapter;

    public ConnectionListLoader(Context context, ConnectionSpinnerAdapter adapter) {
        this.context = context;
        this.adapter = adapter;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(
                context,
                JuiceSSHContract.Connections.CONTENT_URI,
                JuiceSSHContract.Connections.PROJECTION,
                null,
                null,
                JuiceSSHContract.Connections.SORT_ORDER_DEFAULT
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if(adapter != null){
            adapter.swapCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        if(adapter != null){
            adapter.swapCursor(null);
        }
    }
}
