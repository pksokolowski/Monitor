package com.example.sokol.monitor;

import android.content.Context;

import com.example.sokol.monitor.DataBase.DbHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Log {
    private long id;
    private String catInitial;
    private String catTitle;
    private long startTime;

    public long getID() {
        return id;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    private long endTime;

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public void setCat(CatData cat) {
        this.catTitle = cat.getTitle();
        this.catInitial = cat.getInitial();
    }

    public String getCatInitial() {
        return catInitial;
    }

    public String getCatTitle() {
        return catTitle;
    }

    public long getDuration() {
        return endTime - startTime;
    }

    public String getDurationString() {
        long duration = getDuration();
        if(duration >= TimeHelper.DAY_LEN_IN_MILLIS){
            // display duration in hours only
            return TimeHelper.getHoursCount(duration);
        }
        return TimeHelper.getDuration(duration);
    }

    public String getStartTimeString() {
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
     * Thus a CatNameToIDHelper object is necessary, as it provides a (CatID to cat initial) mapping.
     *
     * @return a list of Log objects, convenient for adapters of recycler views etc, a compiled pack of data about individual logs.
     */
    public static List<Log> getLogsList(Context context, long lowerBound, long upperBound) {
        DbHelper db = DbHelper.getInstance(context);

        // obtain cats then use them to obtain logs 
        List<CatData> cats = db.getCategories(CatData.CATEGORY_STATUS_INACTIVE);
        Long[] catIDs = CatData.getCatIDsArray(cats);
        LogsData logsData = db.getLogs(lowerBound, upperBound, catIDs, false);

        // prepare a list and a helper to use in the loop below
        List<Log> list = new ArrayList<>(logsData.getLength());
        CatNameToIDHelper catHelper = new CatNameToIDHelper(cats);

        for (int i = 0; i < logsData.getLength(); i++) {
            long catID = logsData.getCatIDat(i);
            String initial = catHelper.getCatByID(catID).getInitial();
            String title = catHelper.getCatByID(catID).getTitle();
            list.add(getLogAtI(i, logsData, initial, title));
        }

        return list;
    }

    private static Log getLogAtI(int i, LogsData data, String initial, String title) {
        return new Log(data.getIDat(i), initial, title, data.getStartTimes()[i], data.getEndTimes()[i]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Log log = (Log) o;
        return id == log.id &&
                startTime == log.startTime &&
                endTime == log.endTime &&
                Objects.equals(catInitial, log.catInitial) &&
                Objects.equals(catTitle, log.catTitle);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, catInitial, catTitle, startTime, endTime);
    }
}
