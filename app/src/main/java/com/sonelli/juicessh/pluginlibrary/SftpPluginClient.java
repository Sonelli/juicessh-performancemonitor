package com.sonelli.juicessh.pluginlibrary;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.sonelli.juicessh.pluginlibrary.exceptions.ServiceNotConnectedException;
import com.sonelli.juicessh.pluginlibrary.listeners.ICommandSuccessListener;
import com.sonelli.juicessh.pluginlibrary.listeners.ILsEntryListener;
import com.sonelli.juicessh.pluginlibrary.listeners.IStatResultListener;
import com.sonelli.juicessh.pluginlibrary.listeners.IStringResultListener;
import com.sonelli.juicessh.pluginlibrary.listeners.OnClientStartedListener;


public class SftpPluginClient extends ConnectionPluginClient {

    private static final String TAG = "SftpPluginService";

    private ServiceConnection connection;
    private boolean isConnected = false;
    private ISftpPluginService service;
    private Handler handler = new Handler();


    @Override
    protected void add_connect_intent_extras(Intent intent) {
        intent.putExtra("sftp", true);
    }

    /**
     * Start SftpPluginClient and connect to the JuiceSSH Sftp Plugin Service.
     * This should be run in Activity.onStart();
     * @param context
     * @param listener
     */
    @Override
    public void start(final Context context, final OnClientStartedListener listener){
        // Dual service listener only triggers when both the connection plugin service and
        // this plugin service (dis)connects
        final OnClientStartedListener dualServiceListener = new OnClientStartedListener() {
            @Override
            public void onClientStarted() {
                if (isConnected())
                    listener.onClientStarted();
            }

            @Override
            public void onClientStopped() {
                if (!isConnected())
                    listener.onClientStopped();
            }
        };

        // Connect the Connection Plugin Service
        super.start(context, dualServiceListener);

        this.connection = new ServiceConnection(){
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d(TAG, "Bound to the JuiceSSH SFTP plugin service");
                isConnected = true;
                service = ISftpPluginService.Stub.asInterface(iBinder);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        dualServiceListener.onClientStarted();
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d(TAG, "Unbound from the JuiceSSH SFTP plugin service");
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
                new Intent("com.sonelli.juicessh.sftppluginservice"),
                0
        );

        if (resolve == null) {
            Log.e(TAG, "Could not look up explicit intent for com.sonelli.juicessh.sftppluginservice service");
            return;
        }

        // Create an explicit intent for the service
        Intent serviceIntent = new Intent();
        serviceIntent.setComponent(new ComponentName(resolve.serviceInfo.packageName, resolve.serviceInfo.name));
        context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

    }

    /**
     * Stop SftpPluginClient and unbindfrom the JuiceSSH Sftp Plugin Service.
     * This should be run in Activity.onStart();
     * @param context
     */
    @Override
    public void stop(Context context) {
        super.stop(context);
        context.unbindService(connection);
    }

    @Override
    protected boolean isConnected() {
        return super.isConnected() && isConnected;
    }

    public void cd(int session_id, String session_key, String path, ICommandSuccessListener.Stub listener) throws ServiceNotConnectedException,RemoteException {
        if(!isConnected())
            throw new ServiceNotConnectedException();
        service.cd(session_id, session_key, path, listener);
    }

    public void chgrp(int sessionId, String sessionKey, int gid, String path, ICommandSuccessListener.Stub listener) throws  ServiceNotConnectedException,RemoteException {
        if(!isConnected())
            throw new ServiceNotConnectedException();
        service.chgrp(sessionId, sessionKey, gid, path, listener);
    }

    public void chmod(int sessionId, String sessionKey, int permissions, String path, ICommandSuccessListener.Stub listener) throws  ServiceNotConnectedException,RemoteException {
        if(!isConnected())
            throw new ServiceNotConnectedException();
        service.chmod(sessionId, sessionKey, permissions, path, listener);
    }

    public void chown(int sessionId, String sessionKey, int uid, String path, ICommandSuccessListener.Stub listener) throws  ServiceNotConnectedException,RemoteException {
        if(!isConnected())
            throw new ServiceNotConnectedException();
        service.chown(sessionId, sessionKey, uid, path, listener);
    }

    public void get(int sessionId, String sessionKey, String src, ICommandSuccessListener.Stub listener) throws ServiceNotConnectedException,RemoteException {
        if (!isConnected())
            throw new ServiceNotConnectedException();
        service.get(sessionId, sessionKey, src, listener);
    }

    public void lcd(int sessionId, String sessionKey, String path, ICommandSuccessListener.Stub listener) throws  ServiceNotConnectedException,RemoteException {
        if(!isConnected())
            throw new ServiceNotConnectedException();
        service.lcd(sessionId, sessionKey, path, listener);
    }

    public void lpwd(int sessionId, String sessionKey, IStringResultListener.Stub listener) throws  ServiceNotConnectedException,RemoteException {
        if(!isConnected())
            throw new ServiceNotConnectedException();
        service.lpwd(sessionId, sessionKey, listener);
    }

    public void ls(int sessionId, String sessionKey, String path, ILsEntryListener.Stub listener) throws ServiceNotConnectedException,RemoteException {
        if(!isConnected())
            throw new ServiceNotConnectedException();
        service.ls(sessionId, sessionKey, path, listener);
    }

    public void lstat(int sessionId, String sessionKey, String path, IStatResultListener.Stub listener) throws  ServiceNotConnectedException,RemoteException {
        if(!isConnected())
            throw new ServiceNotConnectedException();
        service.lstat(sessionId, sessionKey, path, listener);
    }

    public void pwd(int sessionId, String sessionKey, IStringResultListener.Stub listener) throws  ServiceNotConnectedException,RemoteException {
        if(!isConnected())
            throw new ServiceNotConnectedException();
        service.pwd(sessionId, sessionKey, listener);
    }

    public void readlink(int sessionId, String sessionKey, String path, IStringResultListener.Stub listener) throws  ServiceNotConnectedException,RemoteException {
        if(!isConnected())
            throw new ServiceNotConnectedException();
        service.readlink(sessionId, sessionKey, path, listener);
    }

    public void realpath(int sessionId, String sessionKey, String path, IStringResultListener.Stub listener) throws  ServiceNotConnectedException,RemoteException {
        if(!isConnected())
            throw new ServiceNotConnectedException();
        service.realpath(sessionId, sessionKey, path, listener);
    }

    public void rename(int sessionId, String sessionKey, String oldpath, String newpath, ICommandSuccessListener.Stub listener) throws  ServiceNotConnectedException,RemoteException {
        if(!isConnected())
            throw new ServiceNotConnectedException();
        service.rename(sessionId, sessionKey, oldpath, newpath, listener);
    }

    public void rm(int sessionId, String sessionKey, String path, ICommandSuccessListener.Stub listener) throws  ServiceNotConnectedException,RemoteException {
        if(!isConnected())
            throw new ServiceNotConnectedException();
        service.rm(sessionId, sessionKey, path, listener);
    }

    public void rmdir(int sessionId, String sessionKey, String path, ICommandSuccessListener.Stub listener) throws  ServiceNotConnectedException,RemoteException {
        if(!isConnected())
            throw new ServiceNotConnectedException();
        service.rmdir(sessionId, sessionKey, path, listener);
    }

    public void stat(int sessionId, String sessionKey, String path, IStatResultListener.Stub listener) throws  ServiceNotConnectedException,RemoteException {
        if(!isConnected())
            throw new ServiceNotConnectedException();
        service.stat(sessionId, sessionKey, path, listener);
    }

}
