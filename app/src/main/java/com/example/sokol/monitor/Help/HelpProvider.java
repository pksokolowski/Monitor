package com.example.sokol.monitor.Help;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;

/**
 * Displays help UI for the user. A form of a manual.
 * Contains different "pages", tabs...
 */
public class HelpProvider {

    public static final int TOPIC_GETTING_STARTED = 0;
    public static final int TOPIC_MAIN_ACTIVITY = 1;
    public static final int TOPIC_CATS = 2;
    public static final int TOPIC_LOGS = 3;

    public static void requestHelp(AppCompatActivity context, int topic){
        HelpDialogFragment helpDialog = new HelpDialogFragment();
        helpDialog.setInitialTopic(topic);
        helpDialog.show(context.getSupportFragmentManager(), "help");
    }
}
