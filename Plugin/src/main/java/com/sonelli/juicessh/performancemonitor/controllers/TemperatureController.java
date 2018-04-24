package com.sonelli.juicessh.performancemonitor.controllers;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.sonelli.juicessh.performancemonitor.R;
import com.sonelli.juicessh.pluginlibrary.exceptions.ServiceNotConnectedException;
import com.sonelli.juicessh.pluginlibrary.listeners.OnSessionExecuteListener;

public class TemperatureController extends BaseController {

    public static final String TAG = "TemperatureController";

    public TemperatureController(Context context) {
        super(context);
    }

    @Override
    public BaseController start() {
        super.start();

        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    getPluginClient().executeCommandOnSession(getSessionId(),getSessionKey(), "cat /sys/class/thermal/thermal_zone*/temp", new OnSessionExecuteListener() {
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
                            //input is in millidegrees C
                            double temp = Double.parseDouble(line)/1000;
                            //only one decimal place
                            temp = (double)Math.round(temp*10)/10;
                            setText(Double.toString(temp) + "°C");
                        }

                        @Override
                        public void onError(int error, String reason) {
                            toast(reason);
                        }
                    });
                } catch (ServiceNotConnectedException e) {
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
