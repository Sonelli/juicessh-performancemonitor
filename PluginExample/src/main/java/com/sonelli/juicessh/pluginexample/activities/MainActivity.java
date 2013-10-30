package com.sonelli.juicessh.pluginexample.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.sonelli.juicessh.pluginexample.R;
import com.sonelli.juicessh.pluginexample.adapters.ConnectionSpinnerAdapter;
import com.sonelli.juicessh.pluginexample.contracts.JuiceSSHContract;
import com.sonelli.juicessh.pluginexample.loaders.ConnectionListLoader;

import java.util.UUID;

public class MainActivity extends FragmentActivity {

    public static final String TAG = "MainActivity";

    private Button connectButton;
    private CheckBox runInBackground;
    private Spinner spinner;
    private ConnectionSpinnerAdapter spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.spinner = (Spinner) findViewById(R.id.connection_spinner);
        this.runInBackground = (CheckBox) findViewById(R.id.run_in_background);

        this.spinnerAdapter = new ConnectionSpinnerAdapter(this);
        spinner.setAdapter(spinnerAdapter);

        // Use a Loader to load the connection list into the adapter from the JuiceSSH content provider
        // This keeps DB activity async and off the UI thread to prevent the plugin lagging
        getSupportLoaderManager().initLoader(0, null, new ConnectionListLoader(this, spinnerAdapter));

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

}
