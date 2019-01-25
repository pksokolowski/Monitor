package com.example.sokol.monitor.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.sokol.monitor.R;
import com.example.sokol.monitor.SettingsFragment;

public class ThemeChanger {
    public static final int THEME_BRIGHT = 0;
    public static final int THEME_MATERIAL = 1;
    public static final int THEME_BLACK = 2;
    /**
     * sets theme according to app settings. Notice that 0 theme is set by default so this method
     * does nothing if settings say "0" theme should be presented.
     *
     * @param context activity's context.
     * @return 0, 1, 2 - number of the theme that is set.
     */
    public static int changeTheme(Context context){
        int themeID = getTheme(context);

        switch (themeID){
            case THEME_MATERIAL: context.setTheme(R.style.DarkMaterial);
                break;
            case THEME_BLACK:  context.setTheme(R.style.Dark);
                break;
        }

        return themeID;
    }

    /**
     * gets you the active theme's number.
     *
     * @param context
     * @return
     */
    public static int getTheme(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean themePreferred = sp.getBoolean(SettingsFragment.KEY_DARK_THEME, false);

        if(themePreferred) return THEME_MATERIAL;

        return THEME_BRIGHT;
    }
}
