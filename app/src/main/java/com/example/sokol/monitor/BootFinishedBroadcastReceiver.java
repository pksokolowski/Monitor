package com.example.sokol.monitor;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

/**
 * Created by Sokol on 23.03.2018.
 */

public class BootFinishedBroadcastReceiver extends BroadcastReceiver
{
    public static final String ACTION_ANDROID_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        if(action == null) return;
        if(action.equals(ACTION_ANDROID_BOOT_COMPLETED)){
            // after reboot: show notification:
           NotificationProvider.showNotificationIfEnabled(context, false);
        }
    }

    public static void setEnabled(Context context, boolean enabled){
        int statusToSet;
        if(enabled){
            statusToSet = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        }else{
            statusToSet = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        }

        ComponentName receiver = new ComponentName(context, BootFinishedBroadcastReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver, statusToSet, PackageManager.DONT_KILL_APP);
    }
}