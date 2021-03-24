package com.sonelli.juicessh.performancemonitor.controllers;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.sonelli.juicessh.performancemonitor.R;
import com.sonelli.juicessh.pluginlibrary.exceptions.ServiceNotConnectedException;
import com.sonelli.juicessh.pluginlibrary.listeners.OnSessionExecuteListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemperatureController extends BaseController {

    public static final String TAG = "TemperatureController";

    public TemperatureController(Context context) {
        super(context);
    }

    @Override
    public BaseController start() {
        super.start();

        // Work out the temperature of the device

        // assuming 'sensors' has been setup properly on the system, checks CPU0's package temp
        // Tested on an Intel i5-4460, Kubuntu
        //final Pattern tempPattern = Pattern.compile("(Package id 0:[\\s]+\\+[0-9]+.[0-9]+°C[A-z\\s=+°,().0-9]+)");
        final Pattern tempPattern = Pattern.compile("(Package id 0:[\\s]+\\+[0-9]+.[0-9]+°C)");

        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {

                try {

                    getPluginClient().executeCommandOnSession(getSessionId(), getSessionKey(), "sensors", new OnSessionExecuteListener() {
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
                            Matcher tempMatcher = tempPattern.matcher(line);
                            if(tempMatcher.find()){
                                // String has this format "Package id 0:  +28.0°C"
                                String temperatureString = tempMatcher.group(0);

                                setText(temperatureString.substring(temperatureString.indexOf('+')));// only keep the temp value, add a +1 to also remove the '+'
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