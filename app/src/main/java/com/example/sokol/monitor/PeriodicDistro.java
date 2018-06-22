package com.example.sokol.monitor;

import android.widget.Toast;

import java.sql.Time;

/**
 * This class is meant to combine data into a period based distributions.
 * <p>
 * For example distribution within a day, between days of week or simply last n days separately.
 */
public class PeriodicDistro {

    public long[] mHourly;
    public long[] mDaily;
    public long[] mWeekly;

    public PeriodicDistro(LogsData data, boolean includeToday) {
        long rangeStart = data.getRangeStartDay0Hour();
        long weekStart = data.getRangeStartMonday(rangeStart);
        long today0Hour = TimeHelper.get0HourNdaysAgo(0);

        if (data.getLength() == 0) {
            mHourly = new long[1];
            mDaily = new long[1];
            mWeekly = new long[1];
            return;
        }

        if (includeToday) today0Hour += TimeHelper.DAY_LEN_IN_MILLIS;

        mHourly = getDistribution(data, rangeStart, TimeHelper.MINUTE_LEN_IN_MILLIS * 5, rangeStart + TimeHelper.DAY_LEN_IN_MILLIS, today0Hour, false);
        mDaily = getDistribution(data, rangeStart, TimeHelper.DAY_LEN_IN_MILLIS, today0Hour, today0Hour, false);
        mWeekly = getDistribution(data, weekStart, TimeHelper.DAY_LEN_IN_MILLIS, weekStart + TimeHelper.WEEK_LEN_IN_MILLIS, today0Hour, true);
    }

    private long[] getDistribution(LogsData data, long rangeStart, long period, long rangeEnd, long dropDataLaterThan, boolean takeAveragePerPeriod) {
        long rangeWidth = rangeEnd - rangeStart;
        if (rangeWidth < 1) return new long[1];
        int periodsInRange = (int) Math.ceil((double)rangeWidth / (double)period);
        long[] array = new long[periodsInRange];
        int[] population = new int[1];
        if (takeAveragePerPeriod) {
            population = new int[periodsInRange];
        }
        long population_lastDaySeen = 0;

        long[] data_start_times = data.getStartTimes();
        long[] data_end_times = data.getEndTimes();

        for (int i = 0; i < data.getLength(); i++) {

            long startTime = data_start_times[i];
            long endTime = data_end_times[i];

            if (startTime > dropDataLaterThan) continue;
            if (endTime > dropDataLaterThan) {
                endTime = dropDataLaterThan;
            }

            long length = endTime - startTime;

            // zabezpieczenie przed logami z przeszłości, jeżeli user grzebał w czasie
            // i popracował za wczoraj :P Inaczej crash, jeżeli popracował przed pierwszym dniem
            // używania aplikacji, bo range start jest liczony na podstawie pierwszego loga w tablicy!
            if (startTime < rangeStart) continue;

            // update the array:
            long time_of_range_Start = (startTime - rangeStart) % rangeWidth;
            long time_of_range_End = (endTime - rangeStart) % rangeWidth;

            int first_period_index = getPeriodindexFromTime(time_of_range_Start, period, periodsInRange);
            int last_period_index = getPeriodindexFromTime(time_of_range_End, period, periodsInRange);

            long time_of_period_start = time_of_range_Start - (first_period_index * period);
            //long time_of_period_end = time_of_range_End - (last_period_index * period);

            int roznica_indexow = last_period_index - first_period_index;

            // dodaję czas wypracowany do odpowiednich periodów:
            if (roznica_indexow == 0 && length <= period) {
                array[first_period_index] += time_of_range_End - time_of_range_Start;

                if (takeAveragePerPeriod){
                    // only count anoter unique day of input when it's a new day
                    // don't count single day twice.
                    long dayNubmer = TimeHelper.getDayNumOf(data_start_times[i]);
                    if (dayNubmer != population_lastDaySeen) {
                        population[first_period_index] += 1;
                        population_lastDaySeen = dayNubmer;
                    }
                }
            } else {
                long toAdd = length;
                // first
                long firstVal = period - time_of_period_start;
                array[first_period_index] += firstVal;
                if(takeAveragePerPeriod){
                    long dayNubmer = TimeHelper.getDayNumOf(data_start_times[i]);
                    if (dayNubmer != population_lastDaySeen) {
                        population[first_period_index] += 1;
                        population_lastDaySeen = dayNubmer;
                    }
                }
                toAdd -= firstVal;
                //the rest
                int p = first_period_index;
                while (toAdd > 0) {
                    p = (p + 1) % periodsInRange;
                    long val = Math.min(period, toAdd);
                    array[p] += val;
                    if(takeAveragePerPeriod){
                        long dayNubmer = TimeHelper.getDayNumOf(data_start_times[i] + (length - toAdd) );
                        if (dayNubmer != population_lastDaySeen) {
                            population[p] += 1;
                            population_lastDaySeen = dayNubmer;
                        }
                    }
                    toAdd -= val;
                }
            }
        }

        if(takeAveragePerPeriod) {
            // perform averaging
            for (int i = 0; i < array.length; i++) {
                if(population[i]==0) continue;
                array[i] /= population[i];
            }
        }

        // save the updated array, new String, so shared prefs editor doesn't fail to recognize changes.
        return array;
    }

    private int getPeriodindexFromTime(long time, long period, int periodsPerSpan) {
//        return (int) Math.min(periodsPerSpan - 1, (double) time / (double) period);
        int index = (int) (time / period);
        return index;
    }
}
