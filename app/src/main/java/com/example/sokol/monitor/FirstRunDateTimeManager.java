package com.example.sokol.monitor;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * All it does is tracking the time of the first run to conveniently and efficiently
 * expose this information to other pieces of code. It assumes the defaultSharedPreferences are
 * loaded into memory anyway, so no additional file IO is required and due to SharedPrefs nature
 * access to it's data should be constant time.
 */
public class FirstRunDateTimeManager {
    private static final String KEY_FIRST_RUN_DATETIME = "first_run_datetime";
    public static long getFirstStartupTime(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        long firstStartup = sp.getLong(KEY_FIRST_RUN_DATETIME, -1);

        if(firstStartup == -1) {
            firstStartup = TimeHelper.now();
            saveFirstStartTime(sp, firstStartup);
        }

        return firstStartup;
    }

    private static void saveFirstStartTime(SharedPreferences sp, long firstStartup) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(KEY_FIRST_RUN_DATETIME, firstStartup);
        editor.apply();
    }
}
