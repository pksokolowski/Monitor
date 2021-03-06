package com.example.sokol.monitor.utils;

import com.example.sokol.monitor.model.LogsData;
import com.example.sokol.monitor.utils.TimeHelper;

import java.util.Calendar;
import java.util.HashSet;

/**
 * This class is meant to combine data into a period based distributions.
 * <p>
 * For example distribution within a day, between days of week or simply last n days separately.
 */
public class PeriodicDistro {

    public long[] mHourly;
    public long[] mDaily;
    public long[] mWeekly;

    // reusable calendar object
    private Calendar mCalendar = Calendar.getInstance();

    public PeriodicDistro(LogsData data, boolean includeToday){
        this(data, includeToday, -1, -1);
    }

    public PeriodicDistro(LogsData data, boolean includeToday, long dailyStart, long dailyEnd) {
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

        if(dailyStart == -1 || dailyEnd == -1){
            dailyStart = rangeStart;
            dailyEnd = today0Hour;
        }

        mHourly = getDistribution(data, rangeStart, TimeHelper.MINUTE_LEN_IN_MILLIS * 5, rangeStart + TimeHelper.DAY_LEN_IN_MILLIS, today0Hour, false);
        //mDaily = getDistribution(data, rangeStart, TimeHelper.DAY_LEN_IN_MILLIS, today0Hour, today0Hour, false);
        mDaily = getDaily(data, dailyStart, dailyEnd);
        mWeekly = getDistribution(data, weekStart, TimeHelper.DAY_LEN_IN_MILLIS, weekStart + TimeHelper.WEEK_LEN_IN_MILLIS, today0Hour, true);
    }

    private long[] getDaily(LogsData data, long rangeStart, long rangeEnd){
        long rangeLen = rangeEnd - rangeStart;
        final long DAY_LEN = TimeHelper.DAY_LEN_IN_MILLIS;
        int N = (int)(rangeLen / DAY_LEN);
        long[] days = new long[N];

        long[] startTimes = data.getStartTimes();
        long[] endTimes = data.getEndTimes();

        for (int i =0; i<data.getLength(); i++) {
            long start = Math.max(rangeStart, startTimes[i]) - rangeStart;
            long end = Math.min(rangeEnd, endTimes[i]) - rangeStart;
            long durationLeft = end - start;
            if(durationLeft <= 0) continue;

            for (int d = (int) (start / DAY_LEN); d < days.length; d++) {
                long dayStart = d * DAY_LEN;
                long dayEnd = dayStart + DAY_LEN;
                if (end <= dayEnd) {
                    days[d] += durationLeft;
                    break;
                } else {
                    long thatDaysWork = dayEnd - Math.max(dayStart, start);
                    days[d] += thatDaysWork;
                    durationLeft -= thatDaysWork;
                }
            }
        }
        return  days;
    }

    private long[] getDistribution(LogsData data, long rangeStart, long period, long rangeEnd, long dropDataLaterThan, boolean takeAveragePerPeriod) {
        long rangeWidth = rangeEnd - rangeStart;
        if (rangeWidth < 1) return new long[1];
        int periodsInRange = (int) Math.ceil((double) rangeWidth / (double) period);
        long[] array = new long[periodsInRange];

        // stuff related to averaging, only important if takeAveragePerPeriod is set to true
        int[] populationSizePerPeriod = new int[1];
        HashSet<Long> daysSeen = new HashSet<>();
        if (takeAveragePerPeriod) {
            populationSizePerPeriod = new int[periodsInRange];
        }

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

               if(takeAveragePerPeriod) addToPopulationN(first_period_index, data_start_times[i], populationSizePerPeriod, daysSeen);
            } else {
                long toAdd = length;
                // first
                long firstVal = period - time_of_period_start;
                array[first_period_index] += firstVal;
                if (takeAveragePerPeriod) addToPopulationN(first_period_index, data_start_times[i], populationSizePerPeriod, daysSeen);
                toAdd -= firstVal;
                //the rest
                int p = first_period_index;
                while (toAdd > 0) {
                    p = (p + 1) % periodsInRange;
                    long val = Math.min(period, toAdd);
                    array[p] += val;
                    if (takeAveragePerPeriod) addToPopulationN(p, data_start_times[i] + (length - toAdd), populationSizePerPeriod, daysSeen);
                    toAdd -= val;
                }
            }
        }

        if (takeAveragePerPeriod) {
            // perform averaging
            for (int i = 0; i < array.length; i++) {
                if (populationSizePerPeriod[i] == 0) continue;
                array[i] /= populationSizePerPeriod[i];
            }
        }

        // save the updated array, new String, so shared prefs editor doesn't fail to recognize changes.
        return array;
    }

    /**
     * if takingAveragePerPeriod is enabled, this method maintains the population size per period array.
     * It makes sure to only count unique days on which data was gathered. This way population size array only contains
     * counts of unique days on which logs appeared. Array is of length equal to the number of periods, counts of days are maintained
     * per period, each index of the array is a different period's counter.
     */
    private void addToPopulationN( int arrayIndex, long startTime, int[] population, HashSet<Long> daysSeen) {
        long uniqueDayIdentifier = TimeHelper.applyTimeStampAs0HourOfItsDay(mCalendar, startTime);
        if (!daysSeen.contains(uniqueDayIdentifier)) {
            population[arrayIndex] += 1;
            daysSeen.add(uniqueDayIdentifier);
        }
    }

    private int getPeriodindexFromTime(long time, long period, int periodsPerSpan) {
//        return (int) Math.min(periodsPerSpan - 1, (double) time / (double) period);
        int index = (int) (time / period);
        return index;
    }
}
