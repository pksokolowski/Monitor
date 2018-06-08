package com.example.sokol.monitor.DateTimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HumanReadabilityHelper {
//    public static String getDateStampString(int year, int month, int day){
//        return String.format(Locale.US, "%d:%d:%d", year, month, day);
//    }
//
//    public static String getTimeStampString(int hour, int minute){
//        return String.format(Locale.US, "%d:%d", minute, hour);
//    }

    public static String getDateStampString(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        return getDateStampString(c);
    }

    public static String getDateStampString(Calendar c) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yy", Locale.US);
        // formatter.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
        return formatter.format(new Date(c.getTimeInMillis()));
    }

    public static String getTimeStampString(int hour, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        return getTimeStampString(c);
    }

    public static String getTimeStampString(Calendar c) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.US);
        // formatter.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
        return formatter.format(new Date(c.getTimeInMillis()));
    }
}
