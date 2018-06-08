package com.example.sokol.monitor.LogsDialog;

import android.content.Context;

import com.example.sokol.monitor.LogsData;
import com.example.sokol.monitor.LogsSelector;
import com.example.sokol.monitor.TimeHelper;

import java.util.ArrayList;
import java.util.List;

public class Log {
    public long id;
    private String catInitial;
    private String catTitle;
    private long startTime;
    private long endTime;

    public String getCatInitial(){
        return catInitial;
    }

    public String getCatTitle() {
        return catTitle;
    }

    public long getDuration() {
        return endTime - startTime;
    }

    public String getDurationString(){
        return TimeHelper.getDuration(this.getDuration());
    }

    public String getStartTimeString(){
        return TimeHelper.getDateTimeStampString(startTime);
    }

    public Log(long ID, String initial, String title, long startTime, long endTime) {
        this.id = ID;
        this.catInitial = initial;
        this.catTitle = title;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * statically provides a list of Log objects, category initials are shared, as references to the same Strings, per category
     * Thus a LogsSelector object is necessary, as it provides the (CatID to cat initial) mapping, as well as it governs the query to db.
     * @return a list of Log objects, convenient for adapters of recycler views etc, a compiled pack of data about individual logs.
     */
    public static List<Log> getLogsList(Context context, long lowerBound, long upperBound) {
        LogsSelector selector = new LogsSelector(context);
        LogsData logsData = selector.getLogsForAllNonDeletedCats(context, lowerBound, upperBound);

        List<Log> list = new ArrayList<>(logsData.getLength());

        for (int i = 0; i < logsData.getLength(); i++) {
            long catID = logsData.getCatIDat(i);
            String initial = selector.getCatInitialByID(catID);
            String title = selector.getCatTitleByID(catID);
            list.add(getLogAtI(i, logsData, initial, title));
        }

        return list;
    }

    private static Log getLogAtI(int i, LogsData data, String initial, String title) {
        // TODO: 06.06.2018 figure out how to get the initial (probably alter the way logs are obtained from DB, and the builder
        return new Log(data.getIDat(i), initial, title, data.getStartTimes()[i], data.getEndTimes()[i]);
    }
}
