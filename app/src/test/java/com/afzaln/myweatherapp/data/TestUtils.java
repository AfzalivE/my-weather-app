package com.afzaln.myweatherapp.data;

import java.util.ArrayList;

/**
 * Created by afzal on 2016-06-10.
 */
public class TestUtils {
    public static ArrayList<Search> listOf(String... searchStrs) {
        ArrayList<Search> searches = new ArrayList<>();
        for (String searchStr : searchStrs) {
            Search search = new Search();
            search.setSearchStr(searchStr);
            searches.add(search);
        }

        return searches;
    }
}
