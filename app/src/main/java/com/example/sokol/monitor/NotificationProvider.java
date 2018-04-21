package com.example.sokol.monitor;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
    public static final String EXTRA_CAT_ID = "cat_title";

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

        // clear it first in case of some weird reuse
        rv.removeAllViews(R.id.buttonHolder);

        for (int i = 0; i < cats.size(); i++) {
            CatData cat = cats.get(i);

            // make the button look the right way:
            int button_layout = R.layout.button_view;
            if (WorkInProgressManager.isWorkGoingOn(context, cat.getID())) {
                button_layout = R.layout.button_view_pressed;
            }

            RemoteViews button = new RemoteViews(context.getPackageName(), button_layout);
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

        Notification.Builder B = new Notification.Builder(context)
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
                    db.pushLog(catID, starTime, endTime);
                    showNotificationIfEnabled(context, true);
                    context.sendBroadcast(new Intent(MainActivity.ACTION_SAVED_NEW_DATA));
                } else {
                    // no work going on here, start it:
                    WorkInProgressManager.startNow(context, catID);
                    showNotificationIfEnabled(context, true);
                }
                break;
        }
    }

}
