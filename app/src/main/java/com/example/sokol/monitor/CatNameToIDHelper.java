package com.example.sokol.monitor;

import java.util.HashMap;
import java.util.List;

public class CatNameToIDHelper {

    HashMap<String, Long> catToID = new HashMap<>();

    public CatNameToIDHelper(List<CatData> cats){
        for(CatData cat : cats){
            catToID.putIfAbsent(cat.getTitle(), cat.getID());
        }
    }

    public Long getIDByTitle(String title){
        return catToID.get(title);
    }
}
