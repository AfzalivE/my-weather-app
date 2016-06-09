package com.afzaln.kijijiweather.data.source.local;

import java.util.List;

import android.content.Context;
import android.support.annotation.NonNull;

import com.afzaln.kijijiweather.data.Search;
import com.afzaln.kijijiweather.data.Weather;
import com.afzaln.kijijiweather.data.source.WeatherDataSource;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.Sort;
import rx.Observable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by afzal on 2016-06-04.
 */
public class WeatherLocalDataSource implements WeatherDataSource {

    private static WeatherLocalDataSource INSTANCE;
    private final RealmConfiguration realmConfig;

    public static WeatherLocalDataSource getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new WeatherLocalDataSource(context);
        }

        return INSTANCE;
    }

    public WeatherLocalDataSource(Context context) {
        checkNotNull(context);
        realmConfig = new RealmConfiguration.Builder(context).build();
    }

    /**
     * Get a realm instance for the thread that is calling
     * for it. Realm has issues with the same instance
     * being called from threads other than the one that it
     * was created in.
     *
     * @return Realm instance for the caller thread
     */
    private Realm getRealm() {
        return Realm.getInstance(realmConfig);
    }

    @Override
    public void saveRecentSearch(Search search) {
        checkNotNull(search);
        if (!search.areCoordinatesSet) {
            // don't save the search if coordinates aren't set
            throw new IllegalArgumentException("Search object should have latitude and longitude");
        }
        getRealm().beginTransaction();
        // delete existing search of the same term
        getRealm().where(Search.class).equalTo("hashCode", search.hashCode()).findAll().deleteAllFromRealm();
        Search realmSearch = getRealm().copyToRealm(search);
        getRealm().commitTransaction();
    }

    @Override
    public Observable<? extends List<Search>> getRecentSearches() {
        return getRealm().where(Search.class).findAll().sort("timestamp", Sort.DESCENDING).asObservable();
    }

    @Override
    public void deleteRecentSearch(long timestamp) {
        getRealm().beginTransaction();
        // theoretically since timestamp is the primary key, there can only be one deleting
        getRealm().where(Search.class).equalTo("timestamp", timestamp).findAll().deleteAllFromRealm();
        getRealm().commitTransaction();
    }

    @Override
    public void deleteAllRecentSearches() {
        getRealm().beginTransaction();
        // theoretically since timestamp is the primary key, there can only be one deleting
        getRealm().where(Search.class).findAll().deleteAllFromRealm();
        getRealm().commitTransaction();
    }

    // we don't fetch weather from the local database so just return null
    // this method isn't called at all, and throws an exception if it is called
    // by mistake, failing the unit tests
    @Override
    public Observable<Weather> getWeather(Search search) {
        throw new UnsupportedOperationException("Local database is not used to fetch weather information yet");
    }
}
