package com.example.sokol.monitor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

/**
 * Created by Sokol on 21.03.2018.
 */

public class SettingsFragment extends PreferenceFragment {
    // preference key determining whether or not a notification with remote controls is being shown
    public static final String KEY_SHOW_NOTIFICATION = "setting_show_notification";
    public static final String KEY_DARK_THEME = "setting_dark_theme";

    /**
     * tells you whether or not the notification should be shown right now.
     * @param context
     * @return
     */
    public static boolean getShowNotification(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(SettingsFragment.KEY_SHOW_NOTIFICATION, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        findPreference(KEY_SHOW_NOTIFICATION).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                boolean nw = (boolean) o;
                if(nw){
                    // show notification, using the enforce = true, because the method would
                    // otherwise read the setting, which is not yet changed, and refuse to show the
                    // notification.
                    NotificationProvider.showNotification(getActivity(), true);
                }
                else{
                    // remove notification
                    NotificationProvider.removeNotification(getActivity());
                }
                return true;
            }
        });

        findPreference(KEY_DARK_THEME).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor ed = sp.edit();

                // remove notification
                NotificationProvider.removeNotification(getActivity());

                ed.putBoolean(KEY_DARK_THEME, (boolean) o);
                ed.apply();
                Intent i = getActivity().getApplicationContext().getPackageManager()
                        .getLaunchIntentForPackage(getActivity().getApplicationContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            }
        });
    }
}
