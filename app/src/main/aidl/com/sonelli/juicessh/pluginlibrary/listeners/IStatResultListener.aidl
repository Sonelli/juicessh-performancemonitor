package com.sonelli.juicessh.pluginlibrary.listeners;

import com.sonelli.juicessh.pluginlibrary.parcelables.ParcelableSftpATTRS;

interface IStatResultListener {
    void onSuccess(in ParcelableSftpATTRS sftpAttrs);
    void onCommandFailure(String reason);
    void onSessionFailure(String reason);
}