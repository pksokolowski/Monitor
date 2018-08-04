package com.example.sokol.monitor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import com.example.sokol.monitor.Help.HelpProvider;

/**
 * Created by Sokol on 21.03.2018.
 */

public class SettingsFragment extends PreferenceFragmentCompat {
    // preference key determining whether or not a notification with remote controls is being shown
    public static final String KEY_SHOW_NOTIFICATION = "setting_show_notification";
    public static final String KEY_DARK_THEME = "setting_dark_theme";
    public static final String KEY_HELP = "help";

    /**
     * tells you whether or not the notification should be shown right now.
     * @param context
     * @return
     */
    public static boolean getShowNotification(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(SettingsFragment.KEY_SHOW_NOTIFICATION, true);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setup();
    }

    private void setup(){
        addPreferencesFromResource(R.xml.preferences);

        findPreference(KEY_SHOW_NOTIFICATION).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference, Object o) {
                boolean nw = (boolean) o;
                if(nw){
                    // show notification, using the enforce = true, because the method would
                    // otherwise read the setting, which is not yet changed, and refuse to show the
                    // notification.
                    NotificationProvider.showNotification(getActivity(), true);
                    BootFinishedBroadcastReceiver.setEnabled(getActivity(),true);
                    return true;
                }
                else{
                    // remove notification if user confirms it
                    ConfirmationDialogFragment.ask(getActivity(),getString(R.string.settings_warning_disabling_notification),
                            new ConfirmationDialogFragment.OnConfirmationListener() {
                                @Override
                                public void onConfirmation() {
                                    NotificationProvider.removeNotification(getActivity());
                                    ((SwitchPreference) preference).setChecked(false);
                                    BootFinishedBroadcastReceiver.setEnabled(getActivity(),false);
                                }
                            });
                    return false;
                }

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
                getActivity().finish();
                startActivity(i);
                return true;
            }
        });

        findPreference(KEY_HELP).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                HelpProvider.requestHelp((AppCompatActivity)getActivity(), HelpProvider.TOPIC_MAIN_ACTIVITY);
                return true;
            }
        });
    }
}
