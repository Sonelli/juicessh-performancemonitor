package com.sonelli.juicessh.pluginlibrary.listeners;

interface ISessionStartedListener {
    void onSessionStarted(int sessionId, String sessionKey);
}