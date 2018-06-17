package com.example.sokol.monitor;

import org.junit.Test;

import static org.junit.Assert.*;

public class PeriodicDistroTests {

    @Test
    public void periodicDistro_isDailyOK() throws Exception{
        long day = TimeHelper.DAY_LEN_IN_MILLIS;
        long day2 = day*2;
        long day3 = day*3;

        long[] starts = new long[]{0, day-50, day-100, day, day2+10, day3-50};
        long[] ends   = new long[]{0, day+50, day, day+100, day2+110, day3+80};
        long[] catIDs   = new long[]{0, 0, 0, 0, 0, 0};

        long zeroDay = TimeHelper.get0HourNdaysAgo(0)-day*3;
        for(int i=0; i< starts.length;i++){
            starts[i]+= zeroDay;
            ends[i]+=zeroDay;
        }

        long[] desired_output = new long[]{150, 150, 150};
        LogsData logs = new LogsData(starts, ends, ends, catIDs);
        PeriodicDistro peri = new PeriodicDistro(logs);

        assertArrayEquals(desired_output, peri.mDaily);
    }
}