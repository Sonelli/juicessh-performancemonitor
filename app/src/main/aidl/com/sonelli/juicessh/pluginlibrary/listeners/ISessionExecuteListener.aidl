package com.sonelli.juicessh.pluginlibrary.listeners;

interface ISessionExecuteListener {
    void onCompleted(int returnCode);
    void onOutputLine(String line);
    void onError(int reason, String message);
}