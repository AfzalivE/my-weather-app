package com.afzaln.myweatherapp.data.source;

import java.util.List;

import com.afzaln.myweatherapp.data.Search;
import com.afzaln.myweatherapp.data.Weather;
import rx.Observable;

/**
 * Created by afzal on 2016-06-04.
 */
public interface WeatherDataSource {
    Observable<Weather> getWeather(Search search);
    void deleteAllRecentSearches();
    void saveRecentSearch(Search search);
    Observable<? extends List<Search>> getRecentSearches();
    void deleteRecentSearch(long timestamp);
}
