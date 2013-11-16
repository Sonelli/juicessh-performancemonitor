package com.sonelli.juicessh.pluginexample.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sonelli.juicessh.pluginexample.R;
import com.sonelli.juicessh.pluginexample.adapters.ConnectionSpinnerAdapter;
import com.sonelli.juicessh.pluginexample.loaders.ConnectionListLoader;
import com.sonelli.juicessh.pluginlibrary.PluginClient;
import com.sonelli.juicessh.pluginlibrary.PluginContract;
import com.sonelli.juicessh.pluginlibrary.exceptions.ServiceNotConnectedException;
import com.sonelli.juicessh.pluginlibrary.exceptions.WrongConnectionTypeException;
import com.sonelli.juicessh.pluginlibrary.listeners.OnClientStartedListener;
import com.sonelli.juicessh.pluginlibrary.listeners.OnSessionExecuteListener;
import com.sonelli.juicessh.pluginlibrary.listeners.OnSessionFinishedListener;
import com.sonelli.juicessh.pluginlibrary.listeners.OnSessionStartedListener;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends FragmentActivity implements OnSessionStartedListener, OnSessionFinishedListener {

    public static final String TAG = "MainActivity";

    private boolean isClientStarted = false;
    private final PluginClient client = new PluginClient();

    private Button connectButton;
    private Button disconnectButton;
    private TextView loadAverageTextView;
    private Spinner spinner;
    private ConnectionSpinnerAdapter spinnerAdapter;

    // State
    private volatile int sessionId;
    private volatile String sessionKey;
    private volatile boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.spinner = (Spinner) findViewById(R.id.connection_spinner);
        this.spinnerAdapter = new ConnectionSpinnerAdapter(this);
        spinner.setAdapter(spinnerAdapter);

        this.loadAverageTextView = (TextView) findViewById(R.id.load_average);

        this.connectButton = (Button) findViewById(R.id.connect_button);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UUID id = spinnerAdapter.getConnectionId(spinner.getSelectedItemPosition());
                if(id != null){
                    if(isClientStarted){
                        try {
                            client.connect(MainActivity.this, id, true, MainActivity.this);
                        } catch (ServiceNotConnectedException e){
                            Toast.makeText(MainActivity.this, "Could not connect to JuiceSSH Plugin Service", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        this.disconnectButton = (Button) findViewById(R.id.disconnect_button);
        this.disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sessionId > -1 && sessionKey != null){
                    if(isClientStarted){
                        try {
                            client.disconnect(sessionId, sessionKey);
                        } catch (ServiceNotConnectedException e){
                            Toast.makeText(MainActivity.this, "Could not connect to JuiceSSH Plugin Service", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Use a Loader to load the connection list into the adapter from the JuiceSSH content provider
        // This keeps DB activity async and off the UI thread to prevent the plugin lagging
        getSupportLoaderManager().initLoader(0, null, new ConnectionListLoader(this, spinnerAdapter));

        if(this.isConnected){
            connectButton.setVisibility(View.GONE);
            disconnectButton.setVisibility(View.VISIBLE);
        } else {
            connectButton.setVisibility(View.VISIBLE);
            disconnectButton.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        client.start(this, new OnClientStartedListener() {
            @Override
            public void onClientStarted() {
                isClientStarted = true;
                connectButton.setEnabled(true);
            }

            @Override
            public void onClientStopped() {
                isClientStarted = false;
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

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        /**
         * The following menu items give a good example for how to interact
         * with the JuiceSSH database. In this example we add, update and delete
         * some connections - however you're not limited to connections in your
         * plugin. See our FAQ for details of what items can be modified.
         */
        switch(item.getItemId()){

            /**
             * Insert a dummy connection and get the UUID of it.
             * You can then use this UUID to launch a session using:
             * PluginClient.connect()
             */
            case R.id.action_create_test_connection:

                // Insert a dummy connection
                ContentValues values = new ContentValues();
                values.put(PluginContract.Connections.ID, UUID.randomUUID().toString());
                values.put(PluginContract.Connections.NICKNAME, "*** Test Connection ***");
                values.put(PluginContract.Connections.ADDRESS, "test.connection.example.com");
                Uri addedConnectionUri = getContentResolver().insert(PluginContract.Connections.CONTENT_URI, values);
                UUID addedConnectionId = UUID.fromString(addedConnectionUri.getLastPathSegment());

                String createdMessage =  String.format( getString(R.string.created_test_connection), addedConnectionId );
                Toast.makeText(this, createdMessage, Toast.LENGTH_SHORT).show();
                return true;

            /**
             * Update any previously added dummy connections.
             * In this example we just change the nickname of the connection.
             */
            case R.id.action_update_test_connection:

                // First find any previously added dummy connections
                Cursor cursor = getContentResolver().query(
                        PluginContract.Connections.CONTENT_URI,
                        new String[]{ PluginContract.Connections.ID, PluginContract.Connections.NICKNAME },
                        PluginContract.Connections.NICKNAME + " = ?",
                        new String[]{ "*** Test Connection ***" }, null);

                while(cursor.moveToNext()){

                    // Then update them
                    String connectionId = cursor.getString(0);
                    Uri connectionUri = Uri.withAppendedPath(PluginContract.Connections.CONTENT_URI, connectionId);
                    ContentValues updatedValues = new ContentValues();
                    updatedValues.put(PluginContract.Connections.NICKNAME, "*** Updated Test Connection ***");
                    getContentResolver().update(connectionUri, updatedValues, null, null);

                    String updatedMessage = String.format(getString(R.string.updated_test_connection), connectionId);
                    Toast.makeText(this, updatedMessage, Toast.LENGTH_SHORT).show();

                }
                cursor.close();

                return true;

            /**
             * Delete any previously added dummy connections
             */
            case R.id.action_delete_test_connections:
                getContentResolver().delete(PluginContract.Connections.CONTENT_URI,
                        PluginContract.Connections.ADDRESS + " = ?",
                        new String[]{ "test.connection.example.com" });
                Toast.makeText(this, getString(R.string.deleted_all_test_connections), Toast.LENGTH_SHORT).show();
                return true;

        }

        return false;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // This is important if you want to be able to interact with JuiceSSH sessions that you
        // have started otherwise the plugin won't have access.
        if(requestCode == PluginClient.JUICESSH_REQUEST){
            client.gotActivityResult(resultCode, data);
        }
    }

    @Override
    public void onSessionStarted(final int sessionId, final String sessionKey) {

        MainActivity.this.sessionId = sessionId;
        MainActivity.this.sessionKey = sessionKey;
        MainActivity.this.isConnected = true;

        connectButton.setVisibility(View.GONE);
        disconnectButton.setVisibility(View.VISIBLE);

        // Register a listener for session finish events so that we know when the session has been disconnected
        try {
            client.addSessionFinishedListener(sessionId, sessionKey, this);
        } catch (ServiceNotConnectedException e){}

        // Execute the 'uptime' command on the server every second and parse out the load average
        // with a regular expression. Then update the big load average TextView.
        // Wrap the load average with *'s on every other update so that you can easily see
        // when it updates if the load average doesn't change much.
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {

                try {
                    final Pattern loadAvgPattern = Pattern.compile("average[s]?:\\s*([0-9.]+)"); // Heavy cpu so do out of loops.
                    client.executeCommandOnSession(sessionId, sessionKey, "uptime", new OnSessionExecuteListener() {
                        @Override
                        public void onCompleted(int exitCode) {
                            switch(exitCode){
                                case 127:
                                    loadAverageTextView.setText(getString(R.string.error));
                                    Log.d(TAG, "Tried to run a command but the command was not found on the server");
                                    break;
                            }
                        }
                        @Override
                        public void onOutputLine(String line) {
                            Matcher loadAvgMatcher = loadAvgPattern.matcher(line);
                            if (loadAvgMatcher.find()) {
                                if(loadAverageTextView.getText().toString().contains("*")){
                                    loadAverageTextView.setText(loadAvgMatcher.group(1));
                                } else {
                                    loadAverageTextView.setText("*" + loadAvgMatcher.group(1) + "*");
                                }
                            }
                        }
                    });
                } catch (ServiceNotConnectedException e){
                    Log.d(TAG, "Tried to execute a command but could not connect to JuiceSSH plugin service");
                } catch (WrongConnectionTypeException e){
                    loadAverageTextView.setText(getString(R.string.error));
                    Log.d(TAG, "Commands can only be executed on SSH sessions (not mosh/telnet/local)");
                }

                if(isConnected){
                    handler.postDelayed(this, 1000L);
                }
            }
        });


    }

    @Override
    public void onSessionCancelled() {
        // The user cancelled our JuiceSSH connection before it finished
        // connecting or failed authentication.
    }

    @Override
    public void onSessionFinished() {

        MainActivity.this.sessionId = -1;
        MainActivity.this.sessionKey = null;
        MainActivity.this.isConnected = false;

        loadAverageTextView.setText(R.string.unknown);
        disconnectButton.setVisibility(View.GONE);
        connectButton.setVisibility(View.VISIBLE);

    }

}
