package com.sonelli.juicessh.performancemonitor.controllers;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.sonelli.juicessh.performancemonitor.R;
import com.sonelli.juicessh.pluginlibrary.exceptions.ServiceNotConnectedException;
import com.sonelli.juicessh.pluginlibrary.listeners.OnSessionExecuteListener;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetworkUsageController extends BaseController {

    public static final String TAG = "NetworkUsageController";
    public NetworkUsageController(Context context) {
        super(context);
    }

    @Override
    public BaseController start() {
        super.start();

        /*
        Inter-|   Receive                                                |  Transmit
        face |bytes    packets errs drop fifo frame compressed multicast|bytes    packets errs drop fifo colls carrier compressed
        eth0: 50811400841 184568513    0    0    0     0          0         0 56186328133 220490876    0    0    0     0       0          0
        lo: 22868206326 75472541    0    0    0     0          0         0 22868206326 75472541    0    0    0     0       0          0
        */

        final Pattern networkUsagePattern = Pattern.compile("^\\s*([a-z0-9]+):\\s*([0-9]+)\\s*([0-9]+)\\s*([0-9]+)\\s*([0-9]+)\\s*([0-9]+)\\s*([0-9]+)\\s*([0-9]+)\\s*([0-9]+)\\s*([0-9]+)");

        final AtomicLong lastCheck = new AtomicLong(0);
        final AtomicLong lastTotal = new AtomicLong(0);

        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {

                try {
                    getPluginClient().executeCommandOnSession(getSessionId(), getSessionKey(), "cat /proc/net/dev", new OnSessionExecuteListener() {

                        // Store the devices and the number of rx/tx bytes combined
                        HashMap<String, Long> devices = new HashMap<>();

                        @Override
                        public void onCompleted(int exitCode) {
                            switch(exitCode){
                                case 0:

                                    // Add up all found devices bytes
                                    long total = 0;
                                    for(String device: devices.keySet()){
                                        total += devices.get(device);
                                    }

                                    // Now work out the diff between last check
                                    long bytes = total - lastTotal.get();

                                    // Now work out how many ms since the last check
                                    long milliseconds = System.currentTimeMillis() - lastCheck.get();

                                    // We now know we've done bytes in the last <milliseconds> ms.
                                    // Lets get that into bytes / second
                                    long seconds = milliseconds / 1000;

                                    // If the last check was < 1 sec ago - don't process this one
                                    if(seconds < 1)
                                        return;

                                    long bytesPerSecond = bytes / seconds;
                                    long bitsPerSecond = bytesPerSecond * 8;

                                    if(bitsPerSecond > 1048576*8){
                                        setText(bitsPerSecond / 1024 / 1024 / 8 + " MB/s");
                                    } else if(bitsPerSecond > 1024*8){
                                        setText(bitsPerSecond / 1024 / 8 + " KB/s");
                                    } else {
                                        setText("< 1 KB/s");
                                    }

                                    lastTotal.set(total);
                                    lastCheck.set(System.currentTimeMillis());
                                    break;
                                case 127:
                                    setText(getString(R.string.error));
                                    Log.d(TAG, "Tried to run a command but the command was not found on the server");
                                    break;
                            }
                        }
                        @Override
                        public void onOutputLine(String line) {
                            Matcher networkUsageMatcher = networkUsagePattern.matcher(line);
                            if(networkUsageMatcher.find()){
                                long rx = Long.valueOf(networkUsageMatcher.group(2));
                                long tx = Long.valueOf(networkUsageMatcher.group(10));
                                devices.put(networkUsageMatcher.group(1), rx + tx);
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
