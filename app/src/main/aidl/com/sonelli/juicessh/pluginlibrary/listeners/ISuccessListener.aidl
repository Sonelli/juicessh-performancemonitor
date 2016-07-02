package com.sonelli.juicessh.pluginlibrary.listeners;

interface ISuccessListener {
    void onSuccess();
    void onFailure(String reason);
}