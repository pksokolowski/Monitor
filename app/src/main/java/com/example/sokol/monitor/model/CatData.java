package com.example.sokol.monitor.model;

import java.util.List;

/** Datum Class.
 * Created by Sokol on 23.03.2018.
 */

public class CatData {
    public CatData(long ID, String title, String initial, int status) {
        // not sure why. These two however should not be null.
        if(title == null) title="_";
        if(initial == null) initial = "_";

        this.ID = ID;
        this.title= title;
        this.initial = initial;
        this.status =status;
    }

    public CatData(String title, String initial, int status) {
        this(-1, title, initial, status);
    }

    public static final int CATEGORY_STATUS_DELETED = -1;
    public static final int CATEGORY_STATUS_INACTIVE = 0;
    public static final int CATEGORY_STATUS_ACTIVE = 1;

    public String getTitle() {
        return title;
    }

//    public void setTitle(String title) {
//        this.title = title;
//    }

    public String getInitial() {
        return initial;
    }

//    public void setInitial(String initial) {
//        this.initial = initial;
//    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getID() {
        return ID;
    }

    private long ID;
    private String title;
    private String initial;
    private int status;

    public CatData copy(){
        return new CatData(ID, title, initial, status);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CatData catData = (CatData) o;

        if (ID != catData.ID) return false;
        if (status != catData.status) return false;
        if (!title.equals(catData.title)) return false;
        return initial.equals(catData.initial);
    }

    @Override
    public int hashCode() {
        int result = (int) (ID ^ (ID >>> 32));
        result = 31 * result + title.hashCode();
        result = 31 * result + initial.hashCode();
        result = 31 * result + status;
        return result;
    }

    public static Long[] getCatIDsArray(List<CatData> cats){
        Long[] catIDs = new Long[cats.size()];
        for (int i = 0; i < catIDs.length; i++) {
            catIDs[i] = cats.get(i).getID();
        }
        return catIDs;
    }

}
