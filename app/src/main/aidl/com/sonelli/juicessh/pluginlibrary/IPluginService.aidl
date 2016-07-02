package com.sonelli.juicessh.pluginlibrary;

// Declare any non-default types here with import statements
import com.sonelli.juicessh.pluginlibrary.listeners.IPingListener;
import com.sonelli.juicessh.pluginlibrary.listeners.ISessionExecuteListener;
import com.sonelli.juicessh.pluginlibrary.listeners.ISessionFinishedListener;



interface IPluginService {

    // Legacy - See PluginService.java
    void ping(IPingListener listener);
    void connect(String id);
    void disconnect(int sessionId, String sessionKey);
    void addSessionFinishedListener(int sessionId, String sessionKey, ISessionFinishedListener listener);


    // AIDLs can pass the following basic primitives:
    // int, long, boolean, float, double and String.
    // Anything else will have to be parcelable.

    void attach(int sessionId, String sessionKey);
    void executeCommandOnSession(int sessionId, String sessionKey, String command, ISessionExecuteListener listener);
    void writeToSession(int sessionId, String sessionKey, String command);

}