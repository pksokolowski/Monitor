package com.example.sokol.monitor;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void periodicDistro_isDailyOK() throws Exception{
        long day = TimeHelper.DAY_LEN_IN_MILLIS;
        long day2 = day*2;
        long day3 = day*3;

        long[] starts = new long[]{0, day-50, day-100, day, day2+10, day3-50};
        long[] ends   = new long[]{0, day+50, day, day+100, day2+110, day3+80};

        long zeroDay = TimeHelper.get0HourNdaysAgo(0)-day*3;
        for(int i=0; i< starts.length;i++){
            starts[i]+= zeroDay;
            ends[i]+=zeroDay;
        }

        long[] desired_output = new long[]{150, 150, 150};
        LogsData logs = new LogsData(starts, ends, ends);
        PeriodicDistro peri = new PeriodicDistro(logs);

        assertArrayEquals(desired_output, peri.mDaily);
    }
}