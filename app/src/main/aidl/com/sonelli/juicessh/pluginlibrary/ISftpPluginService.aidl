package com.sonelli.juicessh.pluginlibrary;

// Declare any non-default types here with import statements
import com.sonelli.juicessh.pluginlibrary.listeners.ILsEntryListener;
import com.sonelli.juicessh.pluginlibrary.listeners.ICommandSuccessListener;
import com.sonelli.juicessh.pluginlibrary.listeners.IStatResultListener;
import com.sonelli.juicessh.pluginlibrary.listeners.IStringResultListener;


interface ISftpPluginService {

    // AIDLs can pass the following basic primitives:
    // int, long, boolean, float, double and String.
    // Anything else will have to be parcelable.

    void cd(int sessionId, String sessionKey, String path, ICommandSuccessListener listener);
    void chgrp(int sessionId, String sessionKey, int gid, String path, ICommandSuccessListener listener);
    void chmod(int sessionId, String sessionKey, int permissions, String path, ICommandSuccessListener listener);
    void chown(int sessionId, String sessionKey, int uid, String path, ICommandSuccessListener listener);
    void get(int sessionId, String sessionKey, String src, ICommandSuccessListener listener);
    void lcd(int sessionId, String sessionKey, String path, ICommandSuccessListener listener);
    void lpwd(int sessionId, String sessionKey, IStringResultListener listener);
    void ls(int sessionId, String sessionKey, String path, ILsEntryListener listener);
    void lstat(int sessionId, String sessionKey, String path, IStatResultListener listener);
    //void put(int sessionId, String sessionKey, Parcel contentUriParcel, String dst);
    void pwd(int sessionId, String sessionKey, IStringResultListener listener);
    void readlink(int sessionId, String sessionKey, String path, IStringResultListener listener);
    void realpath(int sessionId, String sessionKey, String path, IStringResultListener listener);
    void rename(int sessionId, String sessionKey, String oldpath, String newpath, ICommandSuccessListener listener);
    void rm(int sessionId, String sessionKey, String path, ICommandSuccessListener listener);
    void rmdir(int sessionId, String sessionKey, String path, ICommandSuccessListener listener);
    void stat(int sessionId, String sessionKey, String path, IStatResultListener listener);
}