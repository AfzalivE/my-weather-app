package com.afzaln.myweatherapp.data;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by afzal on 2016-06-04.
 */
public class Search extends RealmObject {
    long timestamp;
    String searchStr;
    private double lon;
    private double lat;
    private String zipCode;
    private int searchType;

    public boolean areCoordinatesSet;

    @PrimaryKey
    private int hashCode; // save this to identify searches

    public Search() {
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
        searchType = SEARCH_TYPE_CITY;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
        searchType = SEARCH_TYPE_ZIPCODE;
    }

    public void setLatLon(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
        searchType = SEARCH_TYPE_COORDINATES;
        areCoordinatesSet = true;
        this.hashCode = hashCode();
    }

    public String getZipCode() {
        return zipCode;
    }

    public int getSearchType() {
        return searchType;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    @Override
    public int hashCode() {
        // a very simple hashCode method
        // because every search would have coordinates since we save them after
        return Double.valueOf(lat).hashCode() + Double.valueOf(lon).hashCode();
    }

    public static int determineSearchType(String searchStr) {
        // check if search string contains numbers
        // if it does, treat it as a zip code
        if (searchStr.matches(".*\\d+.*")) {
            return SEARCH_TYPE_ZIPCODE;
        } else {
            // else treat it as a city name
            return SEARCH_TYPE_CITY;
        }
    }

    // because realm doesn't support enums
    public static int SEARCH_TYPE_CITY = 1;
    public static int SEARCH_TYPE_ZIPCODE = 2;
    public static int SEARCH_TYPE_COORDINATES = 3;
}