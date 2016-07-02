package com.sonelli.juicessh.pluginlibrary.listeners;

public interface OnCommandSuccessListener {
    void onSuccess();
    void onCommandFailure(String reason);
    void onSessionFailure(String reason);
}
