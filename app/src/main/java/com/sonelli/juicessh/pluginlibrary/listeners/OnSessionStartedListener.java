package com.sonelli.juicessh.pluginlibrary.listeners;

public interface OnSessionStartedListener {
    void onSessionStarted(int sessionId, String sessionKey);
    void onSessionCancelled();
}
