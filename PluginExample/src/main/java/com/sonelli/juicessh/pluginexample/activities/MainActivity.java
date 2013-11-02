package com.sonelli.juicessh.pluginexample.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import com.sonelli.juicessh.pluginexample.R;
import com.sonelli.juicessh.pluginexample.adapters.ConnectionSpinnerAdapter;
import com.sonelli.juicessh.pluginexample.loaders.ConnectionListLoader;
import com.sonelli.juicessh.pluginlibrary.PluginClient;
import com.sonelli.juicessh.pluginlibrary.listeners.OnClientStartedListener;

import java.util.UUID;

public class MainActivity extends FragmentActivity {

    public static final String TAG = "MainActivity";

    private final PluginClient client = new PluginClient();
    private boolean isClientStarted = false;

    private Button connectButton;
    private Spinner spinner;
    private ConnectionSpinnerAdapter spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.spinner = (Spinner) findViewById(R.id.connection_spinner);

        this.spinnerAdapter = new ConnectionSpinnerAdapter(this);
        spinner.setAdapter(spinnerAdapter);

        // Use a Loader to load the connection list into the adapter from the JuiceSSH content provider
        // This keeps DB activity async and off the UI thread to prevent the plugin lagging
        getSupportLoaderManager().initLoader(0, null, new ConnectionListLoader(this, spinnerAdapter));

        this.connectButton = (Button) findViewById(R.id.connect_button);
        connectButton.setText(getString(R.string.please_wait));
        connectButton.setEnabled(false);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UUID id = spinnerAdapter.getConnectionId(spinner.getSelectedItemPosition());
                if(id != null){
                    if(isClientStarted){
                        client.ping();
                    }
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        client.start(this, new OnClientStartedListener() {
            @Override
            public void onClientStarted() {
                isClientStarted = true;
                connectButton.setText(getString(R.string.connect));
                connectButton.setEnabled(true);
            }

            @Override
            public void onClientStopped() {
                isClientStarted = false;
                connectButton.setText(getString(R.string.please_wait));
                connectButton.setEnabled(false);
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(isClientStarted){
            client.stop(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
