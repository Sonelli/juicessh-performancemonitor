package com.sonelli.juicessh.pluginlibrary;

// Declare any non-default types here with import statements
import com.sonelli.juicessh.pluginlibrary.listeners.IPingListener;
import com.sonelli.juicessh.pluginlibrary.listeners.ISessionFinishedListener;


interface IConnectionPluginService {

    // AIDLs can pass the following basic primitives:
    // int, long, boolean, float, double and String.
    // Anything else will have to be parcelable.

    void ping(IPingListener listener);
    void connect(String id);
    void disconnect(int sessionId, String sessionKey);
    void addSessionFinishedListener(int sessionId, String sessionKey, ISessionFinishedListener listener);
}