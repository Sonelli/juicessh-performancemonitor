package com.sonelli.juicessh.performancemonitor.controllers;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.sonelli.juicessh.performancemonitor.R;
import com.sonelli.juicessh.pluginlibrary.PluginClient;
import com.sonelli.juicessh.pluginlibrary.exceptions.ServiceNotConnectedException;
import com.sonelli.juicessh.pluginlibrary.listeners.OnSessionExecuteListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CpuUsageController extends BaseController {

    public static final String TAG = "CpuUsageController";

    public CpuUsageController(Context context) {
        super(context);
    }

    @Override
    public BaseController start() {
        super.start();

        // USER    NICE    SYS   IDLE   IOWAIT  IRQ  SOFTIRQ  STEAL  GUEST
        final Pattern cpuOldPattern = Pattern.compile("^cpu \\s*([0-9]+) ([0-9]+) ([0-9]+) ([0-9]+) ([0-9]+) ([0-9]+) ([0-9]+) ([0-9]+)"); // Heavy cpu so do out of loops.
        final Pattern cpuNewPattern = Pattern.compile("^cpu \\s*([0-9]+) ([0-9]+) ([0-9]+) ([0-9]+) ([0-9]+) ([0-9]+) ([0-9]+) ([0-9]+) ([0-9]+) ([0-9]+)"); // Heavy cpu so do out of loops.

        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {

                try {

                    getPluginClient().executeCommandOnSession(getSessionId(), getSessionKey(), "cat /proc/stat", new OnSessionExecuteListener() {
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

                            Matcher cpuOldMatcher = cpuOldPattern.matcher(line);
                            Matcher cpuNewMatcher = cpuNewPattern.matcher(line);

                            if(cpuOldMatcher.find()){

                                long user = Long.valueOf(cpuOldMatcher.group(1));
                                long nice = Long.valueOf(cpuOldMatcher.group(2));
                                long sys = Long.valueOf(cpuOldMatcher.group(3));
                                long idle = Long.valueOf(cpuOldMatcher.group(4));
                                long iowait = Long.valueOf(cpuOldMatcher.group(5));
                                long irq = Long.valueOf(cpuOldMatcher.group(6));
                                long softirq = Long.valueOf(cpuOldMatcher.group(7));
                                long steal = Long.valueOf(cpuOldMatcher.group(8));

                                long guest = 0;
                                long guestnice = 0;

                                if(cpuNewMatcher.find()){
                                    guest = Long.valueOf(cpuNewMatcher.group(9));
                                    guestnice = Long.valueOf(cpuNewMatcher.group(10));
                                }

                                long total = user + nice + sys + idle + iowait + irq + softirq + steal + guest + guestnice;
                                int free = (int)((idle * 100.0) / total + 0.5);

                                setText((100 - free) + "%");

                            }
                        }

                        @Override
                        public void onError(int error, String reason) {
                            if(error == PluginClient.Errors.WRONG_CONNECTION_TYPE){
                                toast(reason);
                            }
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
