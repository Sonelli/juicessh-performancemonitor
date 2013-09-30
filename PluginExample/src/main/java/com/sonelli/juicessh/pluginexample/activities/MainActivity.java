package com.sonelli.juicessh.pluginexample.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import com.sonelli.juicessh.pluginexample.R;
import com.sonelli.juicessh.pluginexample.adapters.ConnectionSpinnerAdapter;
import com.sonelli.juicessh.pluginexample.contracts.JuiceSSHContract;

import java.util.UUID;

public class MainActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = "MainActivity";

    private Button connectButton;
    private CheckBox runInBackground;
    private Spinner spinner;
    private ConnectionSpinnerAdapter spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Lets be good and use a Loader to fetch the cursor from
        // the content provider. The UI thread is not the place for SQL.
        getSupportLoaderManager().initLoader(0, null, this);

        this.spinner = (Spinner) findViewById(R.id.connection_spinner);
        this.runInBackground = (CheckBox) findViewById(R.id.run_in_background);

        this.spinnerAdapter = new ConnectionSpinnerAdapter(this);
        spinner.setAdapter(spinnerAdapter);

        this.connectButton = (Button) findViewById(R.id.connect_button);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UUID id = spinnerAdapter.getConnectionId(spinner.getSelectedItemPosition());
                if(id != null){
                    Intent intent = JuiceSSHContract.Connections.generateConnectIntent(id, null, runInBackground.isChecked());
                    startActivityForResult(intent, 0);
                }
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(
            this,
            JuiceSSHContract.Connections.CONTENT_URI,
            JuiceSSHContract.Connections.PROJECTION,
            null,
            null,
            JuiceSSHContract.Connections.SORT_ORDER_DEFAULT
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        spinnerAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        spinnerAdapter.swapCursor(null);
    }
}
