package com.sonelli.juicessh.performancemonitor.controllers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;

import com.sonelli.juicessh.performancemonitor.R;
import com.sonelli.juicessh.pluginlibrary.exceptions.ServiceNotConnectedException;
import com.sonelli.juicessh.pluginlibrary.listeners.OnSessionExecuteListener;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiskUsageController extends BaseController {

    public static final String TAG = "DiskUsageController";

    private String partition;

    final Handler handler;

    final Pattern diskUsagePattern = Pattern.compile("([0-9.]+%)"); // Heavy cpu so do out of loops.
    final Pattern partitionNamePattern = Pattern.compile("(/[\\w/]*$)");

    public DiskUsageController(Context context) {
        super(context);
        partition = "/";
        handler = new Handler();
    }

    public void choosePartition() {
        final ArrayList<String> partitions = new ArrayList<String>();

        handler.post(new Runnable() {
            @Override
            public void run() {
                try {

                    getPluginClient().executeCommandOnSession(getSessionId(), getSessionKey(), "df | sed '1 d'", new OnSessionExecuteListener() {
                        @Override
                        public void onCompleted(int exitCode) {
                            switch(exitCode) {
                                case 0:
                                    if(context.get() != null) {
                                        final String[] partitionsArray = new String[partitions.size()];
                                        partitions.toArray(partitionsArray);
                                        AlertDialog.Builder builder = new AlertDialog.Builder(context.get());
                                        builder.setTitle("Choose a partition (" + partition + ")")
                                                .setItems(partitionsArray, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int which) {
                                                        partition = partitionsArray[which];
                                                        dialogInterface.cancel();
                                                        handler.post(loadUsageTask);
                                                    }
                                                })
                                                .show();
                                    }
                                    break;
                                case 127:
                                default:
                                    Log.d(TAG, "Could not load available partitions");
                                    break;
                            }
                        }

                        @Override
                        public void onOutputLine(String line) {
                            Matcher partitionNameMatcher = partitionNamePattern.matcher(line);
                            if(partitionNameMatcher.find())
                                partitions.add(partitionNameMatcher.group(1));
                        }

                        @Override
                        public void onError(int error, String reason) {
                            toast(reason);
                        }
                    });
                } catch (ServiceNotConnectedException e){
                    Log.d(TAG, "Tried to execute a command but could not connect to JuiceSSH plugin service");
                }
            }
        });
    }

    @Override
    public BaseController start() {
        super.start();

        handler.post(loadUsageTask);

        return this;

    }

    Runnable loadUsageTask = new Runnable() {
        @Override
        public void run() {
            try {

                getPluginClient().executeCommandOnSession(getSessionId(), getSessionKey(), "df | grep ' " + partition + "$'", new OnSessionExecuteListener() {
                    @Override
                    public void onCompleted(int exitCode) {
                        switch(exitCode){
                            case 127:
                                setText(getString(R.string.error));
                                Log.d(TAG, "Tried to run a command but the command was not found on the server");
                                break;
                        }
                    }
                    @Override
                    public void onOutputLine(String line) {
                        Matcher diskUsageMatcher = diskUsagePattern.matcher(line);
                        if(diskUsageMatcher.find()){
                            setText(diskUsageMatcher.group(1));
                        }
                    }

                    @Override
                    public void onError(int error, String reason) {
                        toast(reason);
                    }
                });
            } catch (ServiceNotConnectedException e){
                Log.d(TAG, "Tried to execute a command but could not connect to JuiceSSH plugin service");
            }

            if(isRunning()){
                handler.postDelayed(this, INTERVAL_SECONDS * 1000L);
            }
        }
    };

}
