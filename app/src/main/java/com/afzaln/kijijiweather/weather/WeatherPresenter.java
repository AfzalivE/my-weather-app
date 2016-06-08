package com.afzaln.kijijiweather.weather;

import java.util.List;

import android.support.annotation.NonNull;

import com.afzaln.kijijiweather.data.Search;
import com.afzaln.kijijiweather.data.Weather;
import com.afzaln.kijijiweather.data.source.WeatherRepository;
import com.afzaln.kijijiweather.weather.WeatherContract.Presenter;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by afzal on 2016-06-04.
 */
public class WeatherPresenter implements Presenter {
    private final WeatherRepository weatherRepository;
    private final WeatherContract.View weatherView;

    private CompositeSubscription subscriptions;

    public WeatherPresenter(@NonNull WeatherRepository weatherRepository, @NonNull WeatherContract.View weatherView) {
        this.weatherRepository = checkNotNull(weatherRepository);
        this.weatherView = checkNotNull(weatherView, "weatherView cannot be null");
        weatherView.setPresenter(this);
        subscriptions = new CompositeSubscription();
    }

    @Override
    public void subscribe() {
        loadSearchesAndLastWeather();
    }

    @Override
    public void unsubscribe() {
        subscriptions.clear();
    }

    @Override
    public void doWeatherSearch(String searchStr, boolean isFromRecentSearch) {
        if (searchStr == null || searchStr.isEmpty()) {
            // don't do anything if the EditText is empty
        } else {
            if (!isFromRecentSearch) {
                weatherRepository.saveRecentSearch(searchStr);
                loadSearches();
            }
            loadWeather(true, searchStr);
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
                        loadWeather(true, searches.get(0).getSearchStr());
                    } else {
                        processEmptyWeather();
                    }
                });

        subscriptions.add(subscription);
    }

    private void loadWeather(boolean forceUpdate, String cityName) {
        weatherView.setLoadingIndicator(true);

        if (forceUpdate) {
            weatherRepository.refreshWeather();
        }

        subscriptions.clear();

        Subscription subscription = weatherRepository.getWeather(cityName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(weather -> {
                    // onNext
                    processWeather(weather);
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
        weatherView.populateRecentSearches(searches);
    }

    private void processEmptyWeather() {
        weatherView.showEmptyWeather();
        weatherView.setLoadingIndicator(false);
    }

    private void processWeather(Weather weather) {
        weatherView.showWeather(weather);
        weatherView.setLoadingIndicator(false);
    }

    private void processError(Throwable throwable) {
        weatherView.showError(throwable.getMessage());
        weatherView.setLoadingIndicator(false);
    }
}
