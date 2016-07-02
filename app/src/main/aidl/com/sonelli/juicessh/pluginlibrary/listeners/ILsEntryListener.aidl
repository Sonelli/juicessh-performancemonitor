package com.sonelli.juicessh.pluginlibrary.listeners;

import com.sonelli.juicessh.pluginlibrary.parcelables.ParcelableLsEntry;

interface ILsEntryListener {
    void onSuccess(in ParcelableLsEntry[] lsEntry);
    void onCommandFailure(String reason);
    void onSessionFailure(String reason);
}