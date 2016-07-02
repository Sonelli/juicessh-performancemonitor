package com.sonelli.juicessh.pluginlibrary;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;

import com.sonelli.juicessh.pluginlibrary.exceptions.ServiceNotConnectedException;
import com.sonelli.juicessh.pluginlibrary.listeners.IPingListener;
import com.sonelli.juicessh.pluginlibrary.listeners.ISessionFinishedListener;
import com.sonelli.juicessh.pluginlibrary.listeners.OnClientStartedListener;
import com.sonelli.juicessh.pluginlibrary.listeners.OnSessionFinishedListener;
import com.sonelli.juicessh.pluginlibrary.listeners.OnSessionStartedListener;

import java.util.UUID;


abstract class ConnectionPluginClient {

    private static final String TAG = "ConnectionPluginService";

    protected ServiceConnection connection;
    protected boolean isConnected = false;
    protected IConnectionPluginService service;
    protected SparseArray<OnSessionStartedListener> sessionStartedListeners = new SparseArray<OnSessionStartedListener>();
    protected final Handler handler = new Handler();

    protected boolean isConnected() {
        return isConnected;
    }

    /**
     * Sends a PING to the JuiceSSH Plugin Service.
     * Internal use only.
     * @throws com.sonelli.juicessh.pluginlibrary.exceptions.ServiceNotConnectedException
     */
    public void ping() throws ServiceNotConnectedException {
        if(!isConnected())
            throw new ServiceNotConnectedException();

        try {
            service.ping(new IPingListener.Stub() {
                @Override
                public void pong() throws RemoteException {
                    Log.d(TAG, "JuiceSSH Plugin: Got PONG");
                }
            });
        } catch (RemoteException e){
            throw new ServiceNotConnectedException();
        }
    }

    /**
     * Launches a JuiceSSH connection either in the background or foreground.
     * @param activity Activity context required for launching connection (may show authentication dialogs)
     * @param id The UUID id of the connection in the JuiceSSH DB
     * @param listener Callback for connected/disconnected events
     * @throws ServiceNotConnectedException
     */
    public void connect(Activity activity, UUID id, OnSessionStartedListener listener, int requestId) throws ServiceNotConnectedException {

        if(!isConnected())
            throw new ServiceNotConnectedException();

        sessionStartedListeners.put(requestId, listener);

        try {
            // This is just for logging/auditing
            service.connect(id.toString());
        } catch (RemoteException e){
            throw new ServiceNotConnectedException();
        }

        // Always start sessions in the background initially so that we can get them to return
        // instantly with a sessionId & sessionKey. If a foreground session has been requested
        // we can always do an resume/attach later.
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setData(Uri.parse("ssh://" + id));
        add_connect_intent_extras(intent);
        activity.startActivityForResult(intent, requestId);

    }

    /**
     * Launches a JuiceSSH connection either in the background or foreground.
     * @param fragment fragment context required for launching connection (may show authentication dialogs)
     * @param id The UUID id of the connection in the JuiceSSH DB
     * @param listener Callback for connected/disconnected events
     * @throws ServiceNotConnectedException
     */
    public void connect(Fragment fragment, UUID id, OnSessionStartedListener listener, int requestId) throws ServiceNotConnectedException {

        if(!isConnected())
            throw new ServiceNotConnectedException();

        sessionStartedListeners.put(requestId, listener);

        try {
            // This is just for logging/auditing
            service.connect(id.toString());
        } catch (RemoteException e){
            throw new ServiceNotConnectedException();
        }

        // Always start sessions in the background initially so that we can get them to return
        // instantly with a sessionId & sessionKey. If a foreground session has been requested
        // we can always do an resume/attach later.
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setData(Uri.parse("ssh://" + id));
        add_connect_intent_extras(intent);
        fragment.startActivityForResult(intent, requestId);

    }

    protected abstract void add_connect_intent_extras(Intent intent);

    /**
     * Disconnects a previously started session.
     * @param sessionId The integer session ID returned when the session was started
     * @param sessionKey The session key returned when the session was started
     * @throws ServiceNotConnectedException
     */
    public void disconnect(final int sessionId, String sessionKey) throws ServiceNotConnectedException {
        if(!isConnected())
            throw new ServiceNotConnectedException();

        try {
            service.disconnect(sessionId, sessionKey);
        } catch (RemoteException e){
            throw new ServiceNotConnectedException();
        }
    }

    /**
     * Start PluginClient and connect to the JuiceSSH Plugin Service.
     * This should be run in your Activity.onStart();
     * @param context
     * @param listener
     */
    public void start(final Context context, final OnClientStartedListener listener){
        this.connection = new ServiceConnection(){

            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d(TAG, "Bound to the JuiceSSH connection plugin service");
                isConnected = true;
                service = IConnectionPluginService.Stub.asInterface(iBinder);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onClientStarted();
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d(TAG, "Unbound from the JuiceSSH connection plugin service");
                isConnected = false;
                service = null;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onClientStopped();
                    }
                });
            }
        };

        // Attempt to lookup the explicit service information
        ResolveInfo resolve = context.getPackageManager().resolveService(
                new Intent("com.sonelli.juicessh.connectionpluginservice"),
                0
        );

        if (resolve == null) {
            Log.e(TAG, "Could not look up explicit intent for com.sonelli.juicessh.connectionpluginservice service");
            return;
        }

        // Create an explicit intent for the service
        Intent serviceIntent = new Intent();
        serviceIntent.setComponent(new ComponentName(resolve.serviceInfo.packageName, resolve.serviceInfo.name));
        context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

    }

    /**
     * Stop the PluginClient and disconnect from the JuiceSSH Plugin Service.
     * @param context
     */
    public void stop(Context context){
        context.unbindService(connection);
    }

    /**
     * Registers an OnSessionStartedListener to receive disconnect events for a session
     * @param sessionId The integer session ID returned when the session was started
     * @param sessionKey The session key returned when the session was started
     * @param listener Listener to recieve disconnect events
     * @throws ServiceNotConnectedException
     */
    public void addSessionFinishedListener(final int sessionId, final String sessionKey, final OnSessionFinishedListener listener) throws ServiceNotConnectedException {
        if(!isConnected())
            throw new ServiceNotConnectedException();

        try {
            service.addSessionFinishedListener(sessionId, sessionKey, new ISessionFinishedListener.Stub() {
                @Override
                public void onSessionFinished() throws RemoteException {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onSessionFinished();
                        }
                    });
                }
            });
        } catch (RemoteException e){
            throw new ServiceNotConnectedException();
        }
    }

    /**
     * Gets the name of the plugin for reporting back to the PluginService
     * @param context Context required to obtain PackageManager reference
     * @return Application label as defined in <application>
     */
    private static String getPluginName(Context context){

        final PackageManager pm = context.getPackageManager();

        ApplicationInfo applicationInfo;
        try {
            applicationInfo = pm.getApplicationInfo( context.getPackageName(), 0);
        } catch (final PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }

        String pluginName = "Unknown";
        if(applicationInfo != null){
            pluginName = (String) pm.getApplicationLabel(applicationInfo);
        }

        return pluginName;

    }

    /**
     * If you want to interact with JuiceSSH sessions then this should called from your
     * Activity.onActivityResult() method if the requestCode is in your connect request ids.
     * @param requestCode The requestCode used when connecting
     * @param resultCode The resultCode param from onActivityResult
     * @param data The data param from onActivityResult
     */
    public void gotActivityResult(int requestCode, final int resultCode, final Intent data){
        final OnSessionStartedListener onSessionStartedListener = sessionStartedListeners.get(requestCode);
        if(onSessionStartedListener != null){
            switch(resultCode){
                case Activity.RESULT_OK:

                    final int sessionId = data.getIntExtra("session_id", 0);
                    final String sessionKey = data.getStringExtra("session_key");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onSessionStartedListener.onSessionStarted(sessionId, sessionKey);
                        }
                    });

                case Activity.RESULT_CANCELED:
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onSessionStartedListener.onSessionCancelled();
                        }
                    });
                    break;
            }
        }
    }

}
