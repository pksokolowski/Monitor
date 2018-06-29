package com.example.sokol.monitor.EasyCatsDialog;

import com.example.sokol.monitor.CatData;

import java.util.ArrayList;
import java.util.List;

public class ToCatArrayHelper {
    public static String[] getArray(List<String> catTitles){
        return catTitles.toArray(new String[catTitles.size()]);
    }

    /**
     * Extracts titles of the listed cats, but only those that aren't deleted.
     * @param cats
     * @return
     */
    public static List<String> getTitlesOfNonDeletedCats(List<CatData> cats){
        List<String> titles = new ArrayList<>(cats.size());
        for(CatData cat : cats){
            if(cat.getStatus() <= CatData.CATEGORY_STATUS_DELETED) continue;
            titles.add(cat.getTitle());
        }
        return titles;
    }
}
