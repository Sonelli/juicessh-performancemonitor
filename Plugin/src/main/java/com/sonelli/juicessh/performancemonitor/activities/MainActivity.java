package com.sonelli.juicessh.performancemonitor.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sonelli.juicessh.performancemonitor.R;
import com.sonelli.juicessh.performancemonitor.adapters.ConnectionSpinnerAdapter;
import com.sonelli.juicessh.performancemonitor.controllers.BaseController;
import com.sonelli.juicessh.performancemonitor.controllers.CpuUsageController;
import com.sonelli.juicessh.performancemonitor.controllers.FreeRamController;
import com.sonelli.juicessh.performancemonitor.controllers.LoadAverageController;
import com.sonelli.juicessh.performancemonitor.loaders.ConnectionListLoader;
import com.sonelli.juicessh.performancemonitor.views.AutoResizeTextView;
import com.sonelli.juicessh.pluginlibrary.PluginClient;
import com.sonelli.juicessh.pluginlibrary.exceptions.ServiceNotConnectedException;
import com.sonelli.juicessh.pluginlibrary.listeners.OnClientStartedListener;
import com.sonelli.juicessh.pluginlibrary.listeners.OnSessionFinishedListener;
import com.sonelli.juicessh.pluginlibrary.listeners.OnSessionStartedListener;

import java.util.UUID;

public class MainActivity extends ActionBarActivity implements ActionBar.OnNavigationListener, OnSessionStartedListener, OnSessionFinishedListener {

    public static final String TAG = "MainActivity";

    private boolean isClientStarted = false;
    private final PluginClient client = new PluginClient();

    private Button connectButton;
    private Button disconnectButton;

    private ConnectionSpinnerAdapter spinnerAdapter;

    // Controllers
    private BaseController loadAverageController;
    private BaseController freeRamController;
    private BaseController cpuUsageController;

    // Text displays
    private AutoResizeTextView loadAverageTextView;
    private AutoResizeTextView freeRamTextView;
    private AutoResizeTextView cpuUsageTextView;
    private AutoResizeTextView networkUsageTextView;
    private AutoResizeTextView diskUsageTextView;

    // State
    private volatile int sessionId;
    private volatile String sessionKey;
    private volatile boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.spinnerAdapter = new ConnectionSpinnerAdapter(this);

        getSupportActionBar().setNavigationMode(android.app.ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(spinnerAdapter, this);

        this.loadAverageTextView = (AutoResizeTextView) findViewById(R.id.load_average);
        this.freeRamTextView = (AutoResizeTextView) findViewById(R.id.free_memory);
        this.cpuUsageTextView = (AutoResizeTextView) findViewById(R.id.cpu_usage);
        this.networkUsageTextView = (AutoResizeTextView) findViewById(R.id.network_usage);
        this.diskUsageTextView = (AutoResizeTextView) findViewById(R.id.disk_usage);

        this.connectButton = (Button) findViewById(R.id.connect_button);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final UUID id = spinnerAdapter.getConnectionId(getSupportActionBar().getSelectedNavigationIndex());
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

                        disconnectButton.setText(R.string.disconnecting);
                        disconnectButton.setEnabled(false);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    client.disconnect(sessionId, sessionKey);
                                } catch (ServiceNotConnectedException e){
                                    Toast.makeText(MainActivity.this, "Could not connect to JuiceSSH Plugin Service", Toast.LENGTH_SHORT).show();
                                }
                                disconnectButton.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        disconnectButton.setEnabled(true);
                                        disconnectButton.setText(R.string.disconnect);
                                    }
                                });
                            }
                        }).start();
                    }
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(loadAverageTextView != null){
            loadAverageTextView.resizeText();
        }

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
        connectButton.setEnabled(false);

        disconnectButton.setVisibility(View.VISIBLE);
        disconnectButton.setEnabled(true);

        // Register a listener for session finish events so that we know when the session has been disconnected
        try {
            client.addSessionFinishedListener(sessionId, sessionKey, this);
        } catch (ServiceNotConnectedException e){}


        // TODO: Remove these when working
        networkUsageTextView.setText("0 Mbit/s");
        diskUsageTextView.setText("0ms");


        this.loadAverageController = new LoadAverageController(this)
                .setSessionId(sessionId)
                .setSessionKey(sessionKey)
                .setPluginClient(client)
                .setTextview(loadAverageTextView)
                .start();

        this.freeRamController = new FreeRamController(this)
                .setSessionId(sessionId)
                .setSessionKey(sessionKey)
                .setPluginClient(client)
                .setTextview(freeRamTextView)
                .start();

        this.cpuUsageController = new CpuUsageController(this)
                .setSessionId(sessionId)
                .setSessionKey(sessionKey)
                .setPluginClient(client)
                .setTextview(cpuUsageTextView)
                .start();

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

        if(loadAverageController != null){
            loadAverageController.stop();
        }

        if(freeRamController != null){
            freeRamController.stop();
        }

        if(cpuUsageController != null){
            cpuUsageController.stop();
        }

        loadAverageTextView.setText("--");
        freeRamTextView.setText("--");
        cpuUsageTextView.setText("--");
        networkUsageTextView.setText("--");
        diskUsageTextView.setText("--");

        disconnectButton.setVisibility(View.GONE);
        disconnectButton.setEnabled(false);

        connectButton.setVisibility(View.VISIBLE);
        connectButton.setEnabled(true);

    }

    @Override
    public boolean onNavigationItemSelected(int i, long l) {
        return false;
    }
}
