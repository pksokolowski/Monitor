package com.example.sokol.monitor.model;

import android.content.Context;

import com.example.sokol.monitor.DataBase.DbHelper;
import com.example.sokol.monitor.utils.TimeHelper;

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

    public static Log fetchLogWithId(Context context, long id) {
        DbHelper db = DbHelper.getInstance(context);
        return db.getLogById(id);
    }
}
