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

    public PeriodicDistro(LogsData data) {
        long rangeStart = data.getRangeStartDay0Hour();
        long weekStart = data.getRangeStartMonday(rangeStart);
        long today0Hour = TimeHelper.get0HourNdaysAgo(0);

        if (data.getLength() == 0) {
            mHourly = new long[1];
            mDaily = new long[1];
            mWeekly = new long[1];
            return;
        }

        if (today0Hour == rangeStart) today0Hour += TimeHelper.DAY_LEN_IN_MILLIS;

        mHourly = getDistribution(data, rangeStart, TimeHelper.MINUTE_LEN_IN_MILLIS * 5, rangeStart + TimeHelper.DAY_LEN_IN_MILLIS, today0Hour);
        mDaily = getDistribution(data, rangeStart, TimeHelper.DAY_LEN_IN_MILLIS, today0Hour, today0Hour); // when last two are equal, bug happens when real first log is known...
        //long[] fffff = dailySpecial(data, rangeStart, today0Hour, TimeHelper.DAY_LEN_IN_MILLIS);
        //long[] bezecne =  bezecenstwo(data, rangeStart, today0Hour);
        mWeekly = getDistribution(data, weekStart, TimeHelper.DAY_LEN_IN_MILLIS, weekStart + TimeHelper.WEEK_LEN_IN_MILLIS, TimeHelper.getLastMonday());
    }

    private long[] getDistribution(LogsData data, long rangeStart, long period, long rangeEnd, long dropDataLaterThan) {
        long rangeWidth = rangeEnd - rangeStart;
        if (rangeWidth < 1) return new long[1];
        int periodsInRange = (int) (rangeWidth / period);
        long[] array = new long[periodsInRange];

        long[] data_start_times = data.getStartTimes();
        long[] data_end_times = data.getEndTimes();

        for (int i = 0; i < data.getLength(); i++) {

            long startTime = data_start_times[i];
            long endTime = data_end_times[i];

            if (startTime > dropDataLaterThan) continue;
            if (endTime > dropDataLaterThan) {
                // the -1 is important
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
            } else {
                long toAdd = length;
                // first
                long firstVal = period - time_of_period_start;
                array[first_period_index] += firstVal;
                toAdd -= firstVal;
                //the rest
                int p = first_period_index;
                while (toAdd > 0) {
                    p = (p + 1) % periodsInRange;
                    long val = Math.min(period, toAdd);
                    array[p] += val;
                    toAdd -= val;
                }
            }

            // old implementation
//            if (roznica_indexow == 0) {
//                array[first_period_index] += time_of_range_End - time_of_range_Start;
//            } else {
//                // first
//                array[first_period_index] += period - time_of_period_start;
//                // last
//                array[last_period_index] += time_of_period_end;
//                //all between:
//                for (int ii = 1; ii < roznica_indexow; ii++) {
//                    array[first_period_index + ii] += period;
//                }
//            }
        }

        // save the updated array, new String, so shared prefs editor doesn't fail to recognize changes.
        return array;
    }

    private long[] dailySpecial(LogsData data, long firstDay0hour, long lastDay0hour, long period) {
        int periods = (int) ((lastDay0hour - firstDay0hour) / period);

        long[] data_start_times = data.getStartTimes();
        long[] data_end_times = data.getEndTimes();

        long output[] = new long[periods];

        for (int i = 0; i < data.getLength(); i++) {
            long start = Math.min(data_start_times[i] - firstDay0hour, lastDay0hour);
            long end = Math.min(data_end_times[i] - firstDay0hour, lastDay0hour);
            long len = end - start;

            // find first index
            int firstIndex = (int) (len / period);
            if (firstIndex == periods) firstIndex = periods - 1;

            // add to first, special case:
            long time_of_period_start = start - (firstIndex * period);
            output[firstIndex] += period - time_of_period_start;

            // add to all the others
            int ii = firstIndex;
            long toDistribute = len;
            while (toDistribute > 0) {
                ii = (ii + 1) % periods;
                long amount_to_add = Math.min(period, toDistribute);
                output[ii] += amount_to_add;
                toDistribute -= amount_to_add;
            }
        }

        return output;
    }

    private long[] bezecenstwo(LogsData data, long rangeStart, long rangeEnd) {
        long period = TimeHelper.DAY_LEN_IN_MILLIS;
        long rangeWidth = rangeEnd - rangeStart;
        if (rangeWidth < 1) return new long[1];
        int periodsInRange = (int) (rangeWidth / period);
        long[] array = new long[periodsInRange];

        long[] data_start_times = data.getStartTimes();
        long[] data_end_times = data.getEndTimes();

        long[] output = new long[periodsInRange];

        long[] p_starts = new long[periodsInRange];
        long[] p_ends = new long[periodsInRange];
        for (int i = 0; i < periodsInRange; i++) {
            p_starts[i] = i * period;
            p_ends[i] = (i + 1) * period;
        }

        for (int i = 0; i < data.getLength(); i++) {
            long start = Math.min(data_start_times[i] - rangeStart, rangeEnd);
            long end = Math.min(data_end_times[i] - rangeStart, rangeEnd);
            long len = end - start;
            for (int ii = 0; ii < periodsInRange; ii++) {
                // within
                if (start >= p_starts[ii] && end <= p_ends[ii]) {
                    output[ii] += len;
                } else if (start >= p_starts[ii] && start <= p_ends[ii] && end > p_ends[ii]) {
                    output[ii] += p_ends[ii] - start;
                    output[(ii + 1) % periodsInRange] += end - p_ends[ii];
                }
            }
        }

        return output;
    }

    private int getPeriodindexFromTime(long time, long period, int periodsPerSpan) {
//        return (int) Math.min(periodsPerSpan - 1, (double) time / (double) period);
        int index = (int) (time / period);
        return index;
    }
}
