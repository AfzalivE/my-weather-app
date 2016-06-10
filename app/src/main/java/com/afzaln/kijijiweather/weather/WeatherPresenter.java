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
import rx.Observable;
import rx.Observable.Transformer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by afzal on 2016-06-04.
 */
public class WeatherPresenter implements Presenter<WeatherContract.View> {
    private final WeatherRepository weatherRepository;
    private WeatherContract.View weatherView;
    private final LocationProvider locationProvider;

    private ViewState lastViewState;

    public WeatherPresenter(@NonNull WeatherRepository weatherRepository, LocationProvider locationProvider) {
        this.weatherRepository = checkNotNull(weatherRepository);
        this.locationProvider = checkNotNull(locationProvider);
    }

    @Override
    public void onViewAttached(View view, boolean load) {
        weatherView = view;
        if (load) {
            if (lastViewState != null) {
                updateView(lastViewState, false);
            } else {
                doLastWeatherSearch();
            }
        }
    }

    @Override
    public void onViewDetached() {
        weatherView = null;
    }

    @Override
    public void onDestroyed() {
        // nothing to clean up
    }

    /**
     * Perform a search by querying for the user's location
     * and using the latitude and longitude for the search
     */
    @Override
    public void doCoordinatesWeatherSearch() {
        Observable<ViewState> weatherObservable = locationProvider.getLastLocation()
                // to obtain location in the background thread
                .observeOn(Schedulers.io())
                .map(location -> {
                    if (location != null) {
                        Search search = new Search();
                        search.setLatLon(location.getLatitude(), location.getLongitude());
                        return search;
                    } else {
                        throw new NullPointerException("Unable to fetch location");
                    }
                })
                .compose(loadWeather(true))
                .compose(loadSearches())
                .compose(applySchedulers());

        updateView(weatherObservable);
    }


    /**
     * Perform a string based search. Could be a city name or a zip code.
     *
     * @param searchStr      The search string
     * @param isoCountryCode Country code of the user to guess country
     *                       in case zip code is not accompanied by a country code
     */
    @Override
    public void doStringWeatherSeach(String searchStr, String isoCountryCode) {
        if (searchStr != null && !searchStr.isEmpty()) {
            Search search = new Search();
            if (Search.determineSearchType(searchStr) == Search.SEARCH_TYPE_ZIPCODE) {
                // check for country code presence
                if (!searchStr.contains(",")) {
                    searchStr = searchStr + ", " + isoCountryCode;
                }
                search.setZipCode(searchStr);
            } else {
                search.setSearchStr(searchStr);
            }
            Observable<ViewState> stateObservable = Observable.just(search)
                    .compose(loadWeather(true))
                    .compose(loadSearches())
                    .compose(applySchedulers());

            updateView(stateObservable);
        }
        // don't do anything if the EditText is empty
    }

    /**
     * Search weather for the last searched location
     */
    private void doLastWeatherSearch() {
        Observable<ViewState> stateObservable = weatherRepository.getRecentSearches()
                .map(searches -> {
                    if (!searches.isEmpty()) {
                        return searches.get(0);
                    } else {
                        // if this is null then empty weather will be shown
                        return null;
                    }
                })
                .compose(loadWeather(true))
                .compose(loadSearches())
                .compose(applySchedulers());

        updateView(stateObservable);
    }


    /**
     * Load the weather
     *
     * @param forceUpdate Skip cache and update
     *
     * @return The Observable containing weather data
     */
    Transformer<Search, Weather> loadWeather(boolean forceUpdate) {
        return searchObservable -> searchObservable
                .flatMap(search -> {
                    if (search == null) {
                        return null;
                    }
                    if (forceUpdate) {
                        // forces the repository to skip the cache
                        weatherRepository.refreshWeather();
                    }
                    return weatherRepository.getWeather(search);
                });
    }

    /**
     * Load all recent searches
     *
     * @return Observable containing all the searches
     */
    private Transformer<Weather, ViewState> loadSearches() {
        return weatherObservable -> weatherObservable
                .flatMap(weather -> weatherRepository.getRecentSearches()
                        .map(searches -> new ViewState(weather, searches)));
    }

    /**
     * Apply schedulers to make sure things run in the thread they should
     *
     * @param <T> Another type of observable
     *
     * @return Same observable with specified schedulers
     */
    public static <T> Transformer<T, T> applySchedulers() {
        return observable -> observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Delete the requested search and upate the search list
     *
     * @param search The search item to delete
     */
    @Override
    public void deleteRecentSearch(@NonNull Search search) {
        if (search != null) {
            weatherRepository.deleteRecentSearch(search.getTimestamp());
            weatherRepository.getRecentSearches()
                    .<List<Search>>map(searches -> searches)
                    .compose(applySchedulers())
                    .subscribe(searches -> {
                        updateViewSearches(searches);
                    });
        }
    }

    /**
     * @param observable The ViewState object which contains the Weather and list of searches
     *
     * @return Subscription to future updates
     */
    private Subscription updateView(Observable<ViewState> observable) {
        return observable
                .subscribe(viewState -> {
                    // onNext
                    updateView(viewState, true);
                    lastViewState = viewState;
                }, throwable -> {
                    // onError
                    showError(throwable);
                }, () -> {
                    // onCompleted
                    if (weatherView != null) {
                        weatherView.showProgressBar(false);
                    }
                });
    }

    /**
     * Update view with the view state
     *
     * @param viewState The object containing Searches and Weather data
     * @param animate Whether to animate this update or not
     */
    private void updateView(ViewState viewState, boolean animate) {
        updateViewSearches(viewState.searches);
        if (viewState.weather == null) {
            showEmptyWeather();
        } else {
            updateViewWeather(viewState.weather, animate);
        }
    }

    /**
     * Update the list of recent searches
     *
     * @param searches Recent searches
     */
    private void updateViewSearches(List<Search> searches) {
        if (weatherView != null) {
            weatherView.populateRecentSearches(searches);
        }
    }

    private void showEmptyWeather() {
        if (weatherView != null) {
            weatherView.showEmptyWeather();
            weatherView.showProgressBar(false);
        }
    }

    private void updateViewWeather(Weather weather, boolean animate) {
        if (weatherView != null) {
            weatherView.showWeather(weather, animate);
            weatherView.showProgressBar(false);
        }
    }


    private void showError(Throwable throwable) {
        if (weatherView != null) {
            weatherView.showError(throwable.getMessage());
        }
    }

    /**
     * State of the view
     */
    private static class ViewState {
        List<Search> searches;
        Weather weather;

        public ViewState(Weather weather, List<Search> searches) {
            this.weather = weather;
            this.searches = searches;
        }
    }
}
