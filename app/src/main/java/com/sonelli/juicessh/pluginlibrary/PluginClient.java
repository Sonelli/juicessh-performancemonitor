package com.sonelli.juicessh.pluginlibrary;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.sonelli.juicessh.performancemonitor.R;
import com.sonelli.juicessh.pluginlibrary.exceptions.ServiceNotConnectedException;
import com.sonelli.juicessh.pluginlibrary.listeners.ISessionExecuteListener;
import com.sonelli.juicessh.pluginlibrary.listeners.OnClientStartedListener;
import com.sonelli.juicessh.pluginlibrary.listeners.OnSessionExecuteListener;


public class PluginClient extends ConnectionPluginClient {

    private static final String TAG = "PluginService";

    private ServiceConnection connection;
    private boolean isConnected = false;
    private IPluginService service;
    private Handler handler = new Handler();

    // The minimum JuiceSSH version with plugins support
    public static final int MINIMUM_JUICESSH_VERSION_CODE = 89;
    public static final String MINIMUM_JUICESSH_VERSION_NAME = "1.5.4";

    public static final class Errors {
        public static final int WRONG_CONNECTION_TYPE = 1;
    }

    @Override
    protected void add_connect_intent_extras(Intent intent) {
        intent.putExtra("background", true);
    }

    @Override
    public void start(Context context, final OnClientStartedListener listener) {

        // Check the JuiceSSH version
        if (!checkVersion(context)) return;

        // Dual service listener only triggers when both the connection plugin service and
        // this plugin service (dis)connects
        final OnClientStartedListener dualServiceListener = new OnClientStartedListener() {

            int runningCount = 0;

            @Override
            public void onClientStarted() {

                runningCount++;

                if (runningCount == 2)
                    if (isConnected())
                        listener.onClientStarted();

            }

            @Override
            public void onClientStopped() {

                runningCount--;

                if (runningCount == 0)
                    if (!isConnected())
                        listener.onClientStopped();

            }
        };

        // Connect the Connection Plugin Service
        super.start(context, dualServiceListener);

        this.connection = new ServiceConnection(){
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d(TAG, "Bound to the JuiceSSH plugin service");
                isConnected = true;
                service = IPluginService.Stub.asInterface(iBinder);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        dualServiceListener.onClientStarted();
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d(TAG, "Unbound from the JuiceSSH plugin service");
                isConnected = false;
                service = null;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        dualServiceListener.onClientStopped();
                    }
                });
            }
        };

        // Attempt to lookup the explicit service information
        ResolveInfo resolve = context.getPackageManager().resolveService(
                new Intent("com.sonelli.juicessh.pluginservice"),
                0
        );

        if (resolve == null) {
            Log.e(TAG, "Could not look up explicit intent for com.sonelli.juicessh.pluginservice service");
            return;
        }

        // Create an explicit intent for the service
        Intent serviceIntent = new Intent();
        serviceIntent.setComponent(new ComponentName(resolve.serviceInfo.packageName, resolve.serviceInfo.name));
        context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

    }

    /**
     * Checks to see if the currently installed JuiceSSH version is
     * sufficient to run plugins - if not show a warning dialog.
     *
     * @param context Context used to show an alert dialog
     * @return True if version is OK, false if not
     */
    private boolean checkVersion(Context context) {

        // Get the installed version of JuiceSSH
        PackageManager pm = context.getPackageManager();

        try {
            PackageInfo info = pm.getPackageInfo("com.sonelli.juicessh", 0);
            if (info.versionCode < MINIMUM_JUICESSH_VERSION_CODE) {
                showVersionWarning(context);
                return false;
            }

            // Check that we have the necessary permission granted to launch sessions.
            // If JuiceSSH was installed *after* the plugin, then it won't have the correct
            // permissions granted to it. Check for this and give the user the bad news.

            String permission = "com.sonelli.juicessh.api.v1.permission.OPEN_SESSIONS";
            int result = context.checkCallingOrSelfPermission(permission);
            if(result != PackageManager.PERMISSION_GRANTED){
                showPermissionWarning(context);
                return false;
            }

        } catch (PackageManager.NameNotFoundException e) {
            // JuiceSSH is not installed
            showVersionWarning(context);
            return false;
        }

        return true;

    }

    /**
     * Shows a warning stating that JuiceSSH plugins require a newer version of JuiceSSH be installed
     *
     * @param context Context used to show an alert dialog
     */
    private void showVersionWarning(final Context context) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.juicessh_version)
                .setMessage(String.format(context.getString(R.string.juicessh_wrong_version), MINIMUM_JUICESSH_VERSION_NAME))
                .setPositiveButton(R.string.install, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.sonelli.juicessh"));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

                        try {
                            context.startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            // Play Store not installed - send user to website
                            Intent website = new Intent(Intent.ACTION_VIEW, Uri.parse("http://juicessh.com/changelog"));
                            website.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                            context.startActivity(website);
                        }

                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /**
     * Shows a warning stating that the required JuiceSSH permissions have not been granted.
     * This can happen if JuiceSSH is installed after the plugin. Reinstalling the plugin is
     * necessary.
     *
     * @param context Context used to show an alert dialog
     */
    private void showPermissionWarning(final Context context){
        new AlertDialog.Builder(context)
                .setTitle(R.string.security_error)
                .setMessage(context.getString(R.string.juicessh_permissions_not_granted))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

                        try {
                            context.startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(context, context.getString(R.string.play_store_not_installed), Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();

    }

    @Override
    public void stop(Context context) {
        // Disconnect the Connection Plugin Service
        super.stop(context);
        context.unbindService(connection);
    }

    @Override
    protected boolean isConnected() {
        return super.isConnected() && isConnected;
    }

    /**
     * Writes out a string to the sessions terminal window
     *
     * @param sessionId The integer session ID returned when the session was started
     * @param sessionKey The session key returned when the session was started
     * @param command The command(s) to write
     * @throws ServiceNotConnectedException
     */
    public void writeToSession(int sessionId, String sessionKey, String command) throws ServiceNotConnectedException {

        if(!isConnected())
            throw new ServiceNotConnectedException();

        try {
            service.writeToSession(sessionId, sessionKey, command);
        } catch (RemoteException e){
            throw new ServiceNotConnectedException();
        }
    }

    /**
     * Opens a new exec channel on an existing SSH connection and executes a command.
     * Returns the output to the plugin via an {@link com.sonelli.juicessh.pluginlibrary.listeners.OnSessionExecuteListener }.
     * @param sessionId The integer session ID returned when the session was started
     * @param sessionKey The session key returned when the session was started
     * @param command The command to run
     * @param listener The callback listener
     * @throws ServiceNotConnectedException
     */
    public void executeCommandOnSession(int sessionId, String sessionKey, String command, final OnSessionExecuteListener listener)  throws ServiceNotConnectedException {

        if(!isConnected())
            throw new ServiceNotConnectedException();

        try {
            service.executeCommandOnSession(sessionId, sessionKey, command, new ISessionExecuteListener.Stub() {

                @Override
                public void onError(final int reason, final String error) throws RemoteException {
                    if(reason == Errors.WRONG_CONNECTION_TYPE){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(listener != null)
                                    listener.onError(Errors.WRONG_CONNECTION_TYPE, error);
                            }
                        });
                    }
                }

                @Override
                public void onCompleted(final int returnCode) throws RemoteException {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(listener != null)
                                listener.onCompleted(returnCode);
                        }
                    });
                }

                @Override
                public void onOutputLine(final String line) throws RemoteException {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(listener != null)
                                listener.onOutputLine(line);
                        }
                    });
                }
            });
        } catch (RemoteException e){
            throw new ServiceNotConnectedException();
        }
    }

    /**
     * Brings a JuiceSSH session to the foreground.
     * @param sessionId The integer session ID returned when the session was started
     * @param sessionKey The session key returned when the session was started
     * @throws ServiceNotConnectedException
     */
    public void attach(int sessionId, String sessionKey) throws ServiceNotConnectedException {

        if(!isConnected())
            throw new ServiceNotConnectedException();

        try {
            // This is just for logging/auditing
            service.attach(sessionId, sessionKey);
        } catch (RemoteException e){
            throw new ServiceNotConnectedException();
        }


    }
}
