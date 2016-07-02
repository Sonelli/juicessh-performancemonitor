package com.sonelli.juicessh.performancemonitor.controllers;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.sonelli.juicessh.performancemonitor.R;
import com.sonelli.juicessh.pluginlibrary.exceptions.ServiceNotConnectedException;
import com.sonelli.juicessh.pluginlibrary.listeners.OnSessionExecuteListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FreeRamController extends BaseController {

    public static final String TAG = "FreeRamController";

    public FreeRamController(Context context) {
        super(context);
    }

    @Override
    public BaseController start() {
        super.start();

        // Compile the regex patterns outside of the menu_main loop (cpu heavy)
        final Pattern buffersPattern = Pattern.compile("^Buffers:\\s*([0-9]+)");
        final Pattern freePattern = Pattern.compile("^MemFree:\\s*([0-9]+)");
        final Pattern cachedPattern = Pattern.compile("^Cached:\\s*([0-9]+)");

        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {

                try {

                    getPluginClient().executeCommandOnSession(getSessionId(), getSessionKey(), "cat /proc/meminfo", new OnSessionExecuteListener() {

                        private int buffers = -1;
                        private int free = -1;
                        private int cached = -1;

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

                            Matcher buffersMatcher = buffersPattern.matcher(line);
                            Matcher freeMatcher = freePattern.matcher(line);
                            Matcher cachedMatcher = cachedPattern.matcher(line);

                            if(buffersMatcher.find()){
                                buffers = Integer.valueOf(buffersMatcher.group(1));
                                if(buffers > -1 && free > -1 && cached > -1){
                                    long kb = free + buffers + cached;
                                    if(kb > 1048576){
                                        setText(kb / 1024 / 1024 + " GB");
                                    } else if (kb > 1024){
                                        setText(kb / 1024 + " MB");
                                    } else {
                                        setText(kb + " KB");
                                    }
                                }
                            }

                            if(freeMatcher.find()){
                                free = Integer.valueOf(freeMatcher.group(1));
                                if(buffers > -1 && free > -1 && cached > -1){
                                    long kb = free + buffers + cached;
                                    if(kb > 1048576){
                                        setText(kb / 1024 / 1024 + " GB");
                                    } else if (kb > 1024){
                                        setText(kb / 1024 + " MB");
                                    } else {
                                        setText(kb + " KB");
                                    }
                                }
                            }

                            if(cachedMatcher.find()){
                                cached = Integer.valueOf(cachedMatcher.group(1));
                                if(buffers > -1 && free > -1 && cached > -1){
                                    long kb = free + buffers + cached;
                                    if(kb > 1048576){
                                        setText(kb / 1024 / 1024 + " GB");
                                    } else if (kb > 1024){
                                        setText(kb / 1024 + " MB");
                                    } else {
                                        setText(kb + " KB");
                                    }
                                }
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
