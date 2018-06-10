package com.example.sokol.monitor;

import java.util.HashMap;
import java.util.List;

public class CatNameToIDHelper {

    HashMap<String, Long> catToID = new HashMap<>();
    HashMap<Long, CatData> IDToCat = new HashMap<>();

    public CatNameToIDHelper(List<CatData> cats){
        for(CatData cat : cats){
            catToID.putIfAbsent(cat.getTitle(), cat.getID());
            IDToCat.putIfAbsent(cat.getID(), cat);
        }
    }

    public Long getIDByTitle(String title){
        return catToID.get(title);
    }

    public CatData getCatByID(long ID){
        return IDToCat.get(ID);
    }
}
