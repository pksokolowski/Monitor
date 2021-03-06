package com.example.sokol.monitor.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeHelper {

    public static long get0HourTimeOfAGivenDay(long any_moment_within_the_day){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(any_moment_within_the_day);
        // zero every field that's supposed to be zero
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTimeInMillis();
    }

    /**
     * cuts off seconds and milliseconds, to make sure they are zero.
     * @param timeToModify
     * @return for input like 05:30:20:999 (hours:minutes:seconds:millis) it will return 05:30:00:000
     */
    public static long putZerosInSecondsAndMillis(long timeToModify){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timeToModify);

        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTimeInMillis();
    }

    public static long getNowWithoutSecondsAndMillis(){
        return putZerosInSecondsAndMillis(now());
    }

    public static long get0HourNdaysAgo(int n){
        long now = Calendar.getInstance().getTimeInMillis();

        return get0HourTimeOfAGivenDay(now-(n*DAY_LEN_IN_MILLIS));
    }

    public static long getLastMonday0HourSinceGivenMoment(long moment){

        long zeroHourOfDay = TimeHelper.get0HourTimeOfAGivenDay(moment);

        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.setTimeInMillis(zeroHourOfDay);

        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.add(Calendar.DAY_OF_WEEK, -7);

        return cal.getTimeInMillis();
    }

    public static long getLastMonday(){
        long now = Calendar.getInstance().getTimeInMillis();
        return getLastMonday0HourSinceGivenMoment(now);
    }

    public static String getDurationIntelligently(long milliSeconds){
        if(milliSeconds >= DAY_LEN_IN_MILLIS){
            return getHoursCount(milliSeconds);
        }else
            return getDuration(milliSeconds);
    }

    public static String getDuration(long milliSeconds)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.US);
        formatter.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
        String dateString = formatter.format(new Date(milliSeconds));

        return dateString;
    }

    public static String getHoursCount(long durationInMillis){
        long hours= durationInMillis / 3600000;
        return String.valueOf(hours) + " h";
    }

    public static String getDateTimeStampString(long millis){
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm  dd.MM.yy");
        // formatter.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
        String dateString = formatter.format(new Date(millis));
        return dateString;
    }

    public static String getDateStampString(long millis){
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yy");
        // formatter.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
        String dateString = formatter.format(new Date(millis));
        return dateString;
    }

    public static String getTimeStampString(long millis){
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        // formatter.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
        String dateString = formatter.format(new Date(millis));
        return dateString;
    }

    public static final long MINUTE_LEN_IN_MILLIS = 60000;
    public static final long DAY_LEN_IN_MILLIS = 86400000;
    public static final long WEEK_LEN_IN_MILLIS = 604800000;

    public static long now() {
        return Calendar.getInstance().getTimeInMillis();
    }

    public static boolean areSameDay(long a, long b){
        return get0HourTimeOfAGivenDay(a) == get0HourTimeOfAGivenDay(b);
    }

    public static boolean isToday(long a){
        return areSameDay(a, now());
    }

    /**
     * reuses a Calendar instance provided, changes it's value to match timeStamp but
     * only to day's precision. Effectively it's a get0HourTimeOfAGivenDay() but reusing
     * a Calendar instance instead of creating a new one. This is supposed to be used in
     * cases where lots of calls will be made and preventing construction of thousands of Calendar
     * objects is desired.
     * @param c Calendar instance to be reused and set to 0 hour of the day in which timeStamp lands
     * @param timeStamp the timestamp to be added to the calendar instance,
     *                  but without it's millis, seconds, minutes and hours_of_day.
     * @return timeStamp after cutting hours and smaller units out.
     *         0 hour of the day of the original timeStamp, in other words.
     */
    public static long applyTimeStampAs0HourOfItsDay(Calendar c, long timeStamp){
        c.setTimeInMillis(timeStamp);
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR_OF_DAY, 0);

        return c.getTimeInMillis();
    }
}
