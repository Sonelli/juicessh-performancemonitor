package com.sonelli.juicessh.pluginlibrary.listeners;

interface ICommandSuccessListener {
    void onSuccess();
    void onCommandFailure(String reason);
    void onSessionFailure(String reason);
}