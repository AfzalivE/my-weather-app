package com.afzaln.kijijiweather.weather;

import java.util.List;

import android.support.annotation.NonNull;

import com.afzaln.kijijiweather.data.Search;
import com.afzaln.kijijiweather.data.Weather;
import com.afzaln.kijijiweather.data.source.WeatherRepository;
import com.afzaln.kijijiweather.data.source.location.LocationProvider;
import com.afzaln.kijijiweather.weather.WeatherContract.Presenter;
import com.afzaln.kijijiweather.weather.WeatherContract.View;
import static com.google.common.base.Preconditions.checkNotNull;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by afzal on 2016-06-04.
 */
public class WeatherPresenter implements Presenter<WeatherContract.View> {
    private final String title;
    private final WeatherRepository weatherRepository;
    private WeatherContract.View weatherView;
    private final LocationProvider locationProvider;
    private CompositeSubscription subscriptions;

    private Weather lastWeather;

    public WeatherPresenter(String title, @NonNull WeatherRepository weatherRepository, LocationProvider locationProvider) {
        this.title = title;
        this.weatherRepository = checkNotNull(weatherRepository);
        this.locationProvider = checkNotNull(locationProvider);
        subscriptions = new CompositeSubscription();
    }

    @Override
    public void onViewAttached(View view) {
        onViewAttached(view, true);
    }

    @Override
    public void onViewAttached(View view, boolean load) {
        weatherView = view;
        if (load) {
            if (lastWeather != null) {
                processWeather(lastWeather);
            }
            loadSearchesAndLastWeather();
        }
    }

    public void onViewDetached() {
        weatherView = null;
        subscriptions.clear();
    }

    public void onDestroyed() {
        // nothing to clean up
    }

    @Override
    public void doCoordinatesWeatherSearch() {
        locationProvider.getLastLocation()
                .subscribe(location -> {
                    if (location != null) {
                        Search search = new Search();
                        search.setLatLon(location.getLatitude(), location.getLongitude());
                        loadWeather(true, search);
                    } else {
                        // TODO maybe show error?
                    }
                });
    }

    @Override
    public void doStringWeatherSeach(String searchStr) {
        if (searchStr == null || searchStr.isEmpty()) {
            // don't do anything if the EditText is empty
        } else {
            Search search = new Search();
            if (Search.determineSearchType(searchStr) == Search.SEARCH_TYPE_ZIPCODE) {
                search.setZipCode(searchStr);
            } else {
                search.setSearchStr(searchStr);
            }
            loadWeather(true, search);
        }
    }

    @Override
    public void deleteRecentSearch(@NonNull Search search) {
        if (search != null) {
            weatherRepository.deleteRecentSearch(search.getTimestamp());
            loadSearches();
        }
    }

    private void loadSearches() {
        subscriptions.clear();

        Subscription subscription = weatherRepository.getRecentSearches()
                .subscribe(searches -> {
                    processSearches(searches);
                });

        subscriptions.add(subscription);
    }

    private void loadSearchesAndLastWeather() {
        subscriptions.clear();

        Subscription subscription = weatherRepository.getRecentSearches()
                .subscribe(searches -> {
                    if (!searches.isEmpty()) {
                        processSearches(searches);
                        loadWeather(true, searches.get(0));
                    } else {
                        processEmptyWeather();
                    }
                });

        subscriptions.add(subscription);
    }

    private void loadWeather(boolean forceUpdate, Search search) {
        if (weatherView != null) {
            weatherView.setLoadingIndicator(true);
        }

        if (forceUpdate) {
            weatherRepository.refreshWeather();
        }

        subscriptions.clear();

        Subscription subscription = weatherRepository.getWeather(search)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(weather -> {
                    // onNext
                    loadSearches(); // refresh searches
                    processWeather(weather);
                    lastWeather = weather;
                }, throwable -> {
                    // onError
                    processError(throwable);
                }, () -> {
                    // onCompleted
                    weatherView.setLoadingIndicator(false);
                });

        subscriptions.add(subscription);
    }

    private void processSearches(List<Search> searches) {
        if (weatherView != null) {
            weatherView.populateRecentSearches(searches);
        }
    }

    private void processEmptyWeather() {
        if (weatherView != null) {
            weatherView.showEmptyWeather();
            weatherView.setLoadingIndicator(false);
        }
    }

    private void processWeather(Weather weather) {
        if (weatherView != null) {
            weatherView.showWeather(weather);
            weatherView.setLoadingIndicator(false);
        }
    }

    private void processError(Throwable throwable) {
        if (weatherView != null) {
            weatherView.showError(throwable.getMessage());
            weatherView.setLoadingIndicator(false);
        }
    }
}
