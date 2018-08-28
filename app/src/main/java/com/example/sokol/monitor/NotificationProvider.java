package com.example.sokol.monitor;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;

import com.example.sokol.monitor.DataBase.DbHelper;

import java.util.Calendar;
import java.util.List;

/**
 * Provides the control notification.
 * Created by Sokol on 21.03.2018.
 */

public class NotificationProvider extends BroadcastReceiver {
    public static final int NOTIFICATION_ID_REMOTE_CONTROL = 0;

    public static final String ACTION_NOTIFICATION_BUTTON_CLICK = "com.example.sokolrandomstringppp.Monitor.NOTIFICATION_BUTTON_CLICK";
    public static final String ACTION_NOTIFICATION_PLACEHOLDER_CLICK = "com.example.sokolrandomstringppp.Monitor.NOTIFICATION_PLACEHOLDER_CLICK";
    public static final String EXTRA_CAT_ID = "cat_title";

    public static final String CHANNEL_ID_NOTIFICATION_CONTROLS = "notification_controls";
    public static final String CHANNEL_ID_NOTIFICATION_CONTROLS_SILENT = "notification_controls_silent";

    public static void removeNotification(Context context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager == null) return;
        mNotificationManager.cancel(NOTIFICATION_ID_REMOTE_CONTROL);
    }

    public static void showNotification(Context context, boolean withSound) {

        // load categories from the database
        DbHelper db = DbHelper.getInstance(context);
        List<CatData> cats = db.getCategories(CatData.CATEGORY_STATUS_ACTIVE);

        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.controls);

        // make notification consistent with the chosen theme in the app
        boolean darkTheme = ThemeChanger.getTheme(context) == ThemeChanger.THEME_MATERIAL;
        if (darkTheme) {
            rv.setInt(R.id.button_holder_layout, "setBackgroundColor", ContextCompat.getColor(context, R.color.dark_notification_background));
        } else {
            rv.setInt(R.id.button_holder_layout, "setBackgroundColor", ContextCompat.getColor(context, R.color.bright_notification_background));
        }

        // clear it first in case of some weird reuse
        rv.removeAllViews(R.id.buttonHolder);

         /* When no cats, show placeholder:
            this block will add a placeholder, info for the user
            when touched it will send the user to the app and preferably
            open the cats Dialog.
             */
        if (cats.size() == 0) {
            RemoteViews button = new RemoteViews(context.getPackageName(), R.layout.button_view);
            if (darkTheme)
                button.setInt(R.id.button, "setTextColor", ContextCompat.getColor(context, R.color.dark_notification_text));
            button.setTextViewText(R.id.button, context.getString(R.string.notification_no_categories_message));
            rv.addView(R.id.buttonHolder, button);

            Intent intent = new Intent(context, NotificationProvider.class)
                    .setAction(ACTION_NOTIFICATION_PLACEHOLDER_CLICK);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            button.setOnClickPendingIntent(R.id.button, pendingIntent);
        }

        // for all cats, add buttons:
        for (int i = 0; i < cats.size(); i++) {
            CatData cat = cats.get(i);

            // make the button look the right way:
            int button_layout = R.layout.button_view;
            if (WorkInProgressManager.isWorkGoingOn(context, cat.getID())) {
                button_layout = R.layout.button_view_pressed;
            }

            RemoteViews button = new RemoteViews(context.getPackageName(), button_layout);
            if (darkTheme)
                button.setInt(R.id.button, "setTextColor", ContextCompat.getColor(context, R.color.dark_notification_text));
            button.setTextViewText(R.id.button, cat.getInitial());
            rv.addView(R.id.buttonHolder, button);

            Intent intent = new Intent(context, NotificationProvider.class)
                    .putExtra(EXTRA_CAT_ID, cat.getID())
                    .setAction(ACTION_NOTIFICATION_BUTTON_CLICK);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            //rv.setOnClickPendingIntent(R.id.button, pendingIntent);
            button.setOnClickPendingIntent(R.id.button, pendingIntent);


        }

        Uri startSoundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.click_sound);

        // use the appropriate channel depending on sound setting
        String channelID = CHANNEL_ID_NOTIFICATION_CONTROLS_SILENT;
        if (withSound) { channelID = CHANNEL_ID_NOTIFICATION_CONTROLS; }

        NotificationCompat.Builder B = new NotificationCompat.Builder(context, channelID)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_wb_incandescent_white_24dp)
                .setOngoing(true)
                .setCustomContentView(rv)
                .setPriority(Notification.PRIORITY_MAX);

        if (withSound) {
            B.setSound(startSoundUri);
        }


        Notification notif = B.build();

        NotificationManager NotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // to 0 to ID, pozwoli mi potem manipulować tym notification, w szczególności je usunąc lub zastąpić
        NotificationManager.notify(NOTIFICATION_ID_REMOTE_CONTROL, notif);
    }

    /**
     * checks whether the remote control notification is there or wasn't yet pushed/was cancelled
     * should not be used to determine whether or not to update the notification!
     * It would break theme changes.
     * @return true if notification controls are already among the active notifications.
     */
//    public static boolean isControlNotificationAlreadyThere(Context context){
//        NotificationManager NotificationManager =
//                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        if(NotificationManager == null) return false;
//
//        StatusBarNotification[] activeNotifs =  NotificationManager.getActiveNotifications();
//        if(activeNotifs == null || activeNotifs.length == 0) return false;
//
//        for(StatusBarNotification sn : activeNotifs){
//            if(sn.getId() == NOTIFICATION_ID_REMOTE_CONTROL) return true;
//        }
//
//        return false;
//    }

    public static void showNotificationIfEnabled(Context context, boolean withSound) {
        if (!SettingsFragment.getShowNotification(context)) return;

        showNotification(context, withSound);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        switch (action) {
            case ACTION_NOTIFICATION_BUTTON_CLICK:
                // get cat title from extra
                Bundle extras = intent.getExtras();
                if (extras == null) return;
                long catID = extras.getLong(EXTRA_CAT_ID, -1);
                if (catID == -1) return;

                // check for any work for that cat:
                long starTime = WorkInProgressManager.pullStartEntry(context, catID);
                if (starTime != -1) {
                    // work was going on, now stopped.
                    long endTime = Calendar.getInstance().getTimeInMillis();
                    DbHelper db = DbHelper.getInstance(context);
                    long logID = db.pushLog(catID, starTime, endTime);
                    showNotificationIfEnabled(context, true);

                    Intent freshData = new Intent(MainActivity.ACTION_SAVED_NEW_DATA);
                    freshData.putExtra(MainActivity.EXTRA_NEW_LOG_ID, logID);
                    context.sendBroadcast(freshData);
                } else {
                    // no work going on here, start it:
                    WorkInProgressManager.startNow(context, catID);
                    showNotificationIfEnabled(context, true);
                }
                break;
            case ACTION_NOTIFICATION_PLACEHOLDER_CLICK:
                // a placeholder was clicked, no cats exist, user should be moved to MainActivity
                // in order to add some cats
                MainActivity.startMe(context, MainActivity.COMMAND_DISPLAY_CATS_DIALOG);
                break;
        }
    }


    public static void createNotificationChannels(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.notification_channel_sound_title);
            String description = context.getString(R.string.notification_channel_sound_description);

            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            Uri startSoundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.click_sound);

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_NOTIFICATION_CONTROLS, name, importance);
            channel.setDescription(description);
            // make sure the channel has the correct custom sound
            channel.setSound(startSoundUri, attributes);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // also create the silent channel for no_sound notifications
        createSilentNotificationChannel(context);
    }

    private static void createSilentNotificationChannel(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.notification_channel_silent_title);
            String description = context.getString(R.string.notification_channel_silent_description);

            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_NOTIFICATION_CONTROLS_SILENT, name, importance);
            channel.setDescription(description);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
