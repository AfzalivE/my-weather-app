package com.afzaln.kijijiweather.weather;

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
        loadLastWeather();
    }

    @Override
    public void unsubscribe() {
        subscriptions.clear();
    }

    @Override
    public void doWeatherSearch(String searchStr) {
        if (searchStr == null || searchStr.isEmpty()) {
            // don't do anything if the EditText is empty
        } else {
            weatherRepository.saveRecentSearch(searchStr);
            loadWeather(true, searchStr);
        }
    }

    @Override
    public void deleteRecentSearch(@NonNull Search search) {
        if (search != null) {
            weatherRepository.deleteRecentSearch(search.getTimestamp());
        }
    }

    private void loadLastWeather() {
        weatherView.setLoadingIndicator(true);
        subscriptions.clear();

        Subscription subscription = weatherRepository.getRecentSearches()
                .map(searches -> {
                    if (searches.isEmpty()) {
                        return null; // no previous searches
                    } else {
                        return searches.get(0);
                    }
                })
                .subscribe(search -> {
                    if (search != null) {
                        loadWeather(true, search.getSearchStr());
                    } else {
                        processEmptyWeather();
                    }
                });
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

    private void processEmptyWeather() {
        weatherView.showNoWeather();
    }

    private void processWeather(Weather weather) {
        weatherView.showWeather(weather);
    }

    private void processError(Throwable throwable) {
        weatherView.showError(throwable.getMessage());
    }
}
