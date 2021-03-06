package com.example.sokol.monitor.utils;

import android.content.Context;

import com.example.sokol.monitor.DataBase.DbHelper;
import com.example.sokol.monitor.Graphs.PieChart;
import com.example.sokol.monitor.model.CatData;
import com.example.sokol.monitor.model.LogsData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class LogsSelector {
    public LogsSelector(Context context) {
        List<CatData> cats = DbHelper.getInstance(context).getCategories(CatData.CATEGORY_STATUS_INACTIVE);
        mCatMapByID = new HashMap<>(cats.size());
        for(int i =0; i<cats.size();i++){
            CatData cat = cats.get(i);
            mCatMapByID.put(cat.getID(), cat);
        }
    }

    private HashMap<Long, CatData> mCatMapByID;

    public LogsData getLogsForAllNonDeletedCats(Context context, long since, long till) {
        // get all categories that are non-deleted
        List<CatData> cats = DbHelper.getInstance(context).getCategories(CatData.CATEGORY_STATUS_INACTIVE);
        Long[] catIDs = CatData.getCatIDsArray(cats);

        return fetchDataFromDb(since, till, context, catIDs);
    }

    public LogsData fetchDataFromDb(long since, long till, Context context, Long... catIDs) {
        return DbHelper.getInstance(context).getLogs(since, till, catIDs);
    }

    public List<PieChart.Datum> convertLogsToCatSums(final LogsData data){
        HashMap<Long, Long> idToSum = new HashMap<>();
        for(int i =0; i<data.getLength(); i++){
            long id = data.getCatIDat(i);
            long val = data.getDuration(i);
            if(!idToSum.containsKey(id)) idToSum.put(id, val);
            else
            {
                val += idToSum.get(id);
                idToSum.put(id, val);
            }
        }

        Long[] keys = idToSum.keySet().toArray(new Long[idToSum.keySet().size()]);
        List<PieChart.Datum> pieData = new ArrayList<>(keys.length);
        for(int i =0; i<keys.length;i++){
            pieData.add(new PieChart.Datum(mCatMapByID.get(keys[i]).getInitial(), idToSum.get(keys[i]), keys[i]));
        }

        Collections.sort(pieData, (datum, t1) -> Long.compare(t1.totalTime, datum.totalTime));

        return pieData;
    }

}
