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

            private int numOfLines;
            private double sumOfTemps;

            @Override
            public void run() {
                numOfLines = 0;
                sumOfTemps = 0;
                try {
                    getPluginClient().executeCommandOnSession(getSessionId(),getSessionKey(), "cat /sys/class/thermal/thermal_zone*/temp", new OnSessionExecuteListener() {
                        @Override
                        public void onCompleted(int exitCode) {
                            Log.d(TAG,  "Exit code: " + exitCode);
                            switch(exitCode){
                                case 0:
                                    sumOfTemps/=numOfLines; //calc avarage
                                    sumOfTemps = (double)Math.round(sumOfTemps*10)/10; //one decimal place
                                    setText(Double.toString(sumOfTemps) + "°C");
                                    break;
                                case 127:
                                    setText(getString(R.string.error));
                                    Log.d(TAG, "Tried to run a command but the command was not found on the server");
                                    break;
                            }
                        }

                        @Override
                        public void onOutputLine(String line) {
                            Log.d(TAG,  "Line: " + line);
                            //input is in millidegrees C
                            double temp = Double.parseDouble(line)/1000;
                            sumOfTemps+=temp;
                            numOfLines++;
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
