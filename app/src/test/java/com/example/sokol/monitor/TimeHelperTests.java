package com.example.sokol.monitor;

import org.junit.Test;

import static org.junit.Assert.*;

public class TimeHelperTests {

    @Test
    public void dayNumberIsCorrect() throws Exception{
        long dayLen = TimeHelper.DAY_LEN_IN_MILLIS;
        long yearLen = dayLen*365;
        long[] exampleDays = new long[] { 0, 1, 15, dayLen, dayLen+1, dayLen-1, yearLen+5555, yearLen-1 };
        long[] expecteds = new long[]   { 0, 0,  0,      1,        1,        0,          365,       364 };

        long[] results = new long[expecteds.length];
        for(int i = 0; i< exampleDays.length; i++){
            results[i] = TimeHelper.getDayNumOf(exampleDays[i]);
        }

        assertArrayEquals("DayNumber returned is incorrect", expecteds, results);
    }
}