package com.afzaln.kijijiweather.weather;

import java.util.List;

import android.support.annotation.NonNull;

import com.afzaln.kijijiweather.util.BasePresenter;
import com.afzaln.kijijiweather.util.BaseView;
import com.afzaln.kijijiweather.data.Search;
import com.afzaln.kijijiweather.data.Weather;

/**
 * Created by afzal on 2016-06-04.
 */
public class WeatherContract {
    interface  View extends BaseView<Presenter> {
        void setLoadingIndicator(boolean show);
        void showWeather(Weather weather);
        void showEmptyWeather();
        void showError(String message);
        void populateRecentSearches(List<Search> searches);
    }

    interface Presenter extends BasePresenter {
        void doCoordinatesWeatherSearch();
        void doStringWeatherSeach(String searchStr);
        void deleteRecentSearch(@NonNull Search search);
    }
}
