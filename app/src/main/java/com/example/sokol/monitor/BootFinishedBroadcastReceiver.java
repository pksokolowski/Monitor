package com.example.sokol.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Sokol on 23.03.2018.
 */

public class BootFinishedBroadcastReceiver extends BroadcastReceiver
{
    public static final String ACTION_ANDROID_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(intent.getAction().equals(ACTION_ANDROID_BOOT_COMPLETED)){
            // after reboot: show notification:
           NotificationProvider.showNotificationIfEnabled(context);
        }
    }
}