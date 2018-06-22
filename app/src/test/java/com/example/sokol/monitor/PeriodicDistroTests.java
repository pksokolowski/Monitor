package com.example.sokol.monitor;

import org.junit.Test;

import static org.junit.Assert.*;

public class PeriodicDistroTests {

    @Test
    public void isDailyOK(){
        long day = TimeHelper.DAY_LEN_IN_MILLIS;
        long day2 = day*2;
        long day3 = day*3;

        long[] starts = new long[]{0, day-50, day-100, day, day2+10, day3-50};
        long[] ends   = new long[]{0, day+50, day, day+100, day2+110, day3+80};
        long[] catIDs   = new long[]{0, 0, 0, 0, 0, 0};
        long[] desired_output = new long[]{150, 150, 150};

        pushAllLogsInTime(day, starts, ends);
        PeriodicDistro pDistro = getPeriodicDistroObject(day, starts, ends, catIDs);

        assertArrayEquals(desired_output, pDistro.mDaily);
    }

    @Test
    public void isWeeklyCorrect_forCrossPeriodLogs() {
        long day = TimeHelper.DAY_LEN_IN_MILLIS;
        long hour = TimeHelper.MINUTE_LEN_IN_MILLIS*60;
        long halfHour = TimeHelper.MINUTE_LEN_IN_MILLIS*30;

        long[] starts = new long[]{0, day, day-halfHour};
        long[] ends   = new long[]{hour, day+hour, day+halfHour};
        long[] catIDs   = new long[]{0, 0, 0};
        long[] desired_output = new long[]{0, hour+halfHour, hour+halfHour, 0, 0, 0, 0};

        pushAllLogsInTime(day, starts, ends);
        PeriodicDistro pDistro = getPeriodicDistroObject(day, starts, ends, catIDs);

        assertArrayEquals(desired_output, pDistro.mWeekly);
    }

    /**
     * Since user can generate logs in logs editor and they appear on the lists out of order,
     * the method must correctly recognize it's already seen the day and not count it as a unique
     * one.
     */
    @Test
    public void isWeeklyCorrect_forCrossPeriodLogsOutOfOrder(){
        long day = TimeHelper.DAY_LEN_IN_MILLIS;
        long hour = TimeHelper.MINUTE_LEN_IN_MILLIS*60;
        long halfHour = TimeHelper.MINUTE_LEN_IN_MILLIS*30;

        long[] starts = new long[]{day-halfHour, day, 0};
        long[] ends   = new long[]{day+halfHour, day+hour, hour};
        long[] catIDs   = new long[]{0, 0, 0};
        long[] desired_output = new long[]{0, hour+halfHour, hour+halfHour, 0, 0, 0, 0};

        pushAllLogsInTime(day, starts, ends);
        PeriodicDistro pDistro = getPeriodicDistroObject(day, starts, ends, catIDs);

        assertArrayEquals(desired_output, pDistro.mWeekly);
    }

    private PeriodicDistro getPeriodicDistroObject(long day, long[] starts, long[] ends, long[] catIDs) {
        LogsData logs = new LogsData(starts, ends, ends, catIDs);
        return new PeriodicDistro(logs, false);
    }

    private void pushAllLogsInTime(long day, long[] starts, long[] ends) {
        // push all logs a bit into the future, to stay more true to life
        long zeroDay = TimeHelper.get0HourNdaysAgo(0)-day*3;
        for(int i=0; i< starts.length;i++){
            starts[i]+= zeroDay;
            ends[i]+=zeroDay;
        }
    }
}