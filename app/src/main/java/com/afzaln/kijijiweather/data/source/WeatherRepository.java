package com.afzaln.kijijiweather.data.source;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.support.annotation.NonNull;

import com.afzaln.kijijiweather.data.Search;
import com.afzaln.kijijiweather.data.Weather;
import rx.Observable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by afzal on 2016-06-04.
 */
public class WeatherRepository implements WeatherDataSource {

    private static WeatherRepository INSTANCE = null;
    private final WeatherDataSource weatherRemoteDataSource;
    private final WeatherDataSource weatherLocalDataSource;

    /**
     * This variable has package local visibility so it can be accessed from tests.
     */
    Map<String, Weather> cachedWeather;

    /**
     * Marks the cache as invalid, to force an update the next time data is requested. This variable
     * has package local visibility so it can be accessed from tests.
     */
    boolean isCacheDirty = false;

    private WeatherRepository(@NonNull WeatherDataSource weatherRemoteDataSource,
                              @NonNull WeatherDataSource weatherLocalDataSource) {
        this.weatherRemoteDataSource = checkNotNull(weatherRemoteDataSource);
        this.weatherLocalDataSource = checkNotNull(weatherLocalDataSource);
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @param weatherRemoteDataSource the backend data source
     * @param weatherLocalDataSource  the device storage data source
     * @return the {@link WeatherRepository} instance
     */
    public static WeatherRepository getInstance(WeatherDataSource weatherRemoteDataSource,
                                              WeatherDataSource weatherLocalDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new WeatherRepository(weatherRemoteDataSource, weatherLocalDataSource);
        }
        return INSTANCE;
    }

    /**
     * Used to force {@link #getInstance(WeatherDataSource, WeatherDataSource)} to create a new instance
     * next time it's called.
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }


    /**
     * Gets weather from cache, or remote data source, whichever is
     * available first.
     */
    @Override
    public Observable<Weather> getWeather(String cityName) {
        if (cachedWeather != null && !isCacheDirty) {
            return Observable.just(cachedWeather.get(cityName));
        } else if (cachedWeather == null) {
            cachedWeather = new LinkedHashMap<>();
        }

        Observable<Weather> remoteWeather = weatherRemoteDataSource
                .getWeather(cityName)
                .doOnNext(weather -> {
                    // cache this weather report
                    cachedWeather.put(cityName, weather);
                })
                .doOnError(throwable -> {
                    throwable.printStackTrace();
                })
                .doOnCompleted(() -> isCacheDirty = false);

        return remoteWeather;
    }

    public void refreshWeather() {
        isCacheDirty = true;
    }

    @Override
    public void saveRecentSearch(String search) {
        weatherLocalDataSource.saveRecentSearch(search);
    }

    @Override
    public Observable<? extends List<Search>> getRecentSearches() {
        return weatherLocalDataSource.getRecentSearches();
    }

    @Override
    public void deleteRecentSearch(long timestamp) {
        weatherLocalDataSource.deleteRecentSearch(timestamp);
    }

    @Override
    public void deleteAllRecentSearches() {
        weatherLocalDataSource.deleteAllRecentSearches();
    }
}
