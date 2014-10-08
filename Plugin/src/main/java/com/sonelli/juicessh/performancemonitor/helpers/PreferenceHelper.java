package com.sonelli.juicessh.performancemonitor.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceHelper {

    private static final String KEEP_SCREEN_ON_KEY = "keep_screen_on_key";
    private static final String DISK_USAGE_PARTITION_KEY = "disk_usage_partition_key";
    private static final String DEFAULT_PARTITION = "/";
    private Context context;

    public PreferenceHelper(Context context){
        this.context = context;
    }

    public void setKeepScreenOnFlag(boolean flag) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEEP_SCREEN_ON_KEY, flag);
        editor.commit();
    }

    public boolean getKeepScreenOnFlag() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(KEEP_SCREEN_ON_KEY, false);
    }

    public void setDiskUsagePartition(String partition) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DISK_USAGE_PARTITION_KEY, partition);
        editor.commit();
    }

    public String getDiskUsagePartition() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(DISK_USAGE_PARTITION_KEY, DEFAULT_PARTITION);
    }
}
