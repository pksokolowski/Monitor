package com.example.sokol.monitor.LogsDialog;

import com.example.sokol.monitor.LogsData;

import java.util.ArrayList;
import java.util.List;

public class Log {
    public long id;
    public String catInitial;
    public long startTime;
    public long endTime;

    public long getDuration(){
        return endTime-startTime;
    }

    public Log(long ID, String initial, long startTime, long endTime){
        this.id = ID;
        this.catInitial = initial;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static List<Log> getLogsList(LogsData logsData){
        List<Log> list = new ArrayList<>(logsData.getLength());

        for(int i = 0; i< logsData.getLength(); i++){
            list.add(getLogAtI(i, logsData));
        }

        return list;
    }

    private static Log getLogAtI(int i, LogsData data){
        // TODO: 06.06.2018 figure out how to get the initial (probably alter the way logs are obtained from DB, and the builder
        return new Log(data.getIDat(i), "?", data.getStartTimes()[i], data.getEndTimes()[i]);
    }
}
