package com.afzaln.kijijiweather.data.source;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.support.annotation.NonNull;

import com.afzaln.kijijiweather.data.Search;
import com.afzaln.kijijiweather.data.Weather;
import rx.Observable;

import static com.google.common.base.Preconditions.checkNotNull;
import rx.Observable.Transformer;

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
    Map<Search, Weather> cachedWeather;

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
     *
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
    public Observable<Weather> getWeather(Search search) {
        Weather cachedWeather = checkCache(search);
        if (cachedWeather != null) {
            return Observable.just(cachedWeather);
        }

        return weatherRemoteDataSource
                .getWeather(search)
                .compose(saveSearch(search));
    }

    private Weather checkCache(Search search) {
        if (cachedWeather != null && !isCacheDirty) {
            return cachedWeather.get(search);
        } else if (cachedWeather == null) {
            cachedWeather = new LinkedHashMap<>();
        }

        return null;
    }

    Transformer<Weather, Weather> saveSearch(Search search) {
        return weather -> weather
                .doOnNext(weather1 -> {
                    search.setSearchStr(weather1.name);
                    // set lat lon for hash code generation
                    // and because they're required
                    search.setLatLon(weather1.coord.lat, weather1.coord.lon);
                    // cache this weather report
                    this.cachedWeather.put(search, weather1);
                    // save the search in the local database
                    saveRecentSearch(search);
                })
                .doOnCompleted(() -> isCacheDirty = false);
    }

    public void refreshWeather() {
        isCacheDirty = true;
    }

    @Override
    public void saveRecentSearch(Search search) {
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
