package com.afzaln.kijijiweather.data.source;

import java.util.List;

import com.afzaln.kijijiweather.data.Search;
import com.afzaln.kijijiweather.data.Weather;
import rx.Observable;

/**
 * Created by afzal on 2016-06-04.
 */
public interface WeatherDataSource {
    void deleteAllRecentSearches();
    Observable<Weather> getWeather(String cityName);
    void saveRecentSearch(String search);
    Observable<? extends List<Search>> getRecentSearches();
    void deleteRecentSearch(long timestamp);
}
