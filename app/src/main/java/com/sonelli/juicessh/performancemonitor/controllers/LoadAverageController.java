package com.sonelli.juicessh.performancemonitor.controllers;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.sonelli.juicessh.performancemonitor.R;
import com.sonelli.juicessh.pluginlibrary.exceptions.ServiceNotConnectedException;
import com.sonelli.juicessh.pluginlibrary.listeners.OnSessionExecuteListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoadAverageController extends BaseController {

    public static final String TAG = "LoadAverageController";

    public LoadAverageController(Context context) {
        super(context);
    }

    @Override
    public BaseController start() {
        super.start();

        // Execute the 'uptime' command on the server every second and parse out the load average
        // with a regular expression. Then update the big load average TextView.
        // Wrap the load average with *'s on every other update so that you can easily see
        // when it updates if the load average doesn't change much.

        final Pattern loadAvgPattern = Pattern.compile("average[s]?:\\s*([0-9.]+)"); // Heavy cpu so do out of loops.

        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {

                try {

                    getPluginClient().executeCommandOnSession(getSessionId(), getSessionKey(), "uptime", new OnSessionExecuteListener() {
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
                            Matcher loadAvgMatcher = loadAvgPattern.matcher(line);
                            if(loadAvgMatcher.find()){
                                setText(loadAvgMatcher.group(1));
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


        });

        return this;

    }

}
