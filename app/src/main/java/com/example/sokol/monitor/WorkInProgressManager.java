package com.example.sokol.monitor;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.util.Calendar;

/**
 * Manages the pieces of work that are currently going on.
 * <p>
 * When work starts, a new entry is added here and kept till it's finished.
 * Once the work is done, it drops its start-entry.
 * <p>
 * Supports multiple cats having work simultaneously.
 * Created by Sokol on 25.03.2018.
 */

public class WorkInProgressManager {

    private static final String FILENAME = "started_works";
    private static final String KEY_STARTED = "work_started_";
    private static final String KEY_COUNTER= "counter";

    /**
     * createStartEntry for work.
     */
    public static void startNow(Context context, long catID) {
        long now = Calendar.getInstance().getTimeInMillis();
        String full_key = getFullKey(catID);
        SharedPreferences sp = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putLong(full_key, now);
        ed.apply();
    }

    /**
     * once work is done, or has to end for any other reason(like cat deletion), call this method.
     * It givee you startTime back and removes the entry form shared prefs file.
     *
     * @return -1 if no work is going on. Otherwise an accurate startTime.
     */
    public static long pullStartEntry(Context context, long catID){
        SharedPreferences sp = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        String full_key = getFullKey(catID);

        long startTime = sp.getLong(full_key, -1);
        // if no work is in progress for that category, return -1;
        if(startTime == -1) return -1;

        long counter = sp.getLong(KEY_COUNTER, 0);

        SharedPreferences.Editor ed = sp.edit();
        ed.remove(full_key);
        ed.putLong(KEY_COUNTER,counter+1);
        ed.apply();
        return startTime;
    }

    /**
     * checks whether or not work is going on within a given category.
     */
    public static boolean isWorkGoingOn(Context context, long catID){
        SharedPreferences sp = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        String full_key = getFullKey(catID);

        return sp.contains(full_key);
    }

    public static long getCounter(Context context){
        SharedPreferences sp = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        return sp.getLong(KEY_COUNTER, 0);
    }

    @NonNull
    private static String getFullKey(long catID) {
        return KEY_STARTED + catID;
    }
}
