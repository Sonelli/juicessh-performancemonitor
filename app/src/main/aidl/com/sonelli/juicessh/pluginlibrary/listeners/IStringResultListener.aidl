package com.sonelli.juicessh.pluginlibrary.listeners;

interface IStringResultListener {
    void onSuccess(String result);
    void onCommandFailure(String reason);
    void onSessionFailure(String reason);
}