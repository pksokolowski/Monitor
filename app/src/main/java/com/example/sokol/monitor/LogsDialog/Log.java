package com.example.sokol.monitor.LogsDialog;

import android.content.Context;

import com.example.sokol.monitor.LogsData;
import com.example.sokol.monitor.LogsSelector;

import java.util.ArrayList;
import java.util.List;

public class Log {
    public long id;
    public String catInitial;
    public long startTime;
    public long endTime;

    public long getDuration() {
        return endTime - startTime;
    }

    public Log(long ID, String initial, long startTime, long endTime) {
        this.id = ID;
        this.catInitial = initial;
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
            list.add(getLogAtI(i, logsData, initial));
        }

        return list;
    }

    private static Log getLogAtI(int i, LogsData data, String initial) {
        // TODO: 06.06.2018 figure out how to get the initial (probably alter the way logs are obtained from DB, and the builder
        return new Log(data.getIDat(i), initial, data.getStartTimes()[i], data.getEndTimes()[i]);
    }
}
