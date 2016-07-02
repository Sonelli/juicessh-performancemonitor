package com.sonelli.juicessh.pluginlibrary.listeners;

public interface OnSessionExecuteListener {
    void onCompleted(int returnCode);
    void onOutputLine(String line);
    void onError(int reason, String Message);
}
