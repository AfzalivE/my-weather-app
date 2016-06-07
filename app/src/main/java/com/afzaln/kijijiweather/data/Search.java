package com.afzaln.kijijiweather.data;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by afzal on 2016-06-04.
 */
public class Search extends RealmObject {
    @PrimaryKey
    long timestamp;

    String searchStr;

    public Search() {}

    public Search(String searchStr) {
        this.searchStr = searchStr;
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSearchStr() {
        return searchStr;
    }

    public void setSearchStr(String searchStr) {
        this.searchStr = searchStr;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}