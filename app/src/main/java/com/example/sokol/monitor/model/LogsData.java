package com.example.sokol.monitor.model;

import com.example.sokol.monitor.utils.TimeHelper;

import java.util.ArrayList;
import java.util.List;

/** Datum class for data of multiple logs
 * It maintains several parallel arrays
 * Created by Sokol on 25.03.2018.
 */

public class LogsData
{
    private long[] startTimes;
    private long[] endTimes;
    private long[] IDs;
    private long[] catIDs;

    public LogsData(long[] startTimes, long[]endTimes, long[] IDs, long[] catIDs){
        this.startTimes = startTimes;
        this.endTimes = endTimes;
        this.IDs = IDs;
        this.catIDs = catIDs;
    }

    public long[] getStartTimes(){
        return  startTimes;
    }

    public long[] getEndTimes(){
        return endTimes;
    }

    public long getRangeStartDay0Hour(){
        if(startTimes.length==0) return 0;
        long smallest = Long.MAX_VALUE;
        for(int i =0; i<startTimes.length;i++){
            if(startTimes[i] < smallest) smallest= startTimes[i];
        }
        return TimeHelper.get0HourTimeOfAGivenDay(smallest);
    }

    /**
     * seeks monday that is the first one before the earliest datapoint.
     * @return timestamp of the monday 00:00 in milliseconds
     */
    public long getRangeStartMonday(long earliestDatapoint){
        if(startTimes.length==0) return 0;

        return TimeHelper.getLastMonday0HourSinceGivenMoment(earliestDatapoint);
//
//        long zeroHourOfDay = TimeHelper.get0HourTimeOfAGivenDay(startTimes[0]);
//
//        Calendar cal = Calendar.getInstance();
//        cal.setFirstDayOfWeek(Calendar.MONDAY);
//        cal.setTimeInMillis(zeroHourOfDay);
//
//        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
//        cal.add(Calendar.DAY_OF_WEEK, -7);
//
//        return cal.getTimeInMillis();
    }

    public int getLength(){
        return startTimes.length;
    }

    public long getCatIDat(int i) {
        return catIDs[i];
    }

    public long getIDat(int i) { return IDs[i]; }

    public long getDuration(int i) {
        return endTimes[i]-startTimes[i];
    }

    public static class LogsDataBuilder{

        List<Long> startTimes;
        List<Long> endTimes;
        List<Long> IDs;
        List<Long> catIDs;

        public LogsDataBuilder(){
            startTimes = new ArrayList<>();
            endTimes = new ArrayList<>();
            IDs = new ArrayList<>();
            catIDs = new ArrayList<>();
        }

        public void addDataPoint(long startTime, long endTime, long ID, long catID){
            startTimes.add(startTime);
            endTimes.add(endTime);
            IDs.add(ID);
            catIDs.add(catID);
        }

        public LogsData spitItOut(){
            int n = startTimes.size();
            return new LogsData(cnvrt(startTimes), cnvrt(endTimes), cnvrt(IDs), cnvrt(catIDs));
        }

        /** What a C#'s .ToArray() would do...
         */
        private long[] cnvrt(List<Long> a) {
            long[] w = new long[a.size()];

            for (int i = 0; i < w.length; i++) {
                Long l = a.get(i);
                w[i]=l;
            }

            return w;
        }
    }
}
