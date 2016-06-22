package com.afzaln.myweatherapp.weather;

import java.util.List;

import android.support.annotation.NonNull;

import com.afzaln.myweatherapp.data.Search;
import com.afzaln.myweatherapp.data.Weather;
import com.afzaln.myweatherapp.util.BasePresenter;

/**
 * Interface for View and Presenters handling the Weather
 * data.
 */
public interface WeatherContract {
    interface View {
        /**
         * Hide or show the progress bar to indicate loading.
         *
         * @param show Visibility of the progress bar
         */
        void showProgressBar(boolean show);
        /**
         * Load the Weather object into the view
         *
         * @param weather The object containing weather data
         * @param animate Whether this update should be animated or not. In case of configuration changes
         */
        void showWeather(Weather weather, boolean animate);

        /**
         * Show empty layout when there is no search in history
         * to automatically load
         */
        void showEmptyWeather();

        /**
         * Show error messages in the UI
         *
         * @param message The message to show
         */
        void showError(String message);

        /**
         * Populate the RecyclerView showing all recent searches
         *
         * @param searches The list of searches
         */
        void populateRecentSearches(List<Search> searches);
    }

    interface Presenter<V> extends BasePresenter<V> {
        /**
         * Perform a search by querying for the user's location
         * and using the latitude and longitude for the search
         */
        void doCoordinatesWeatherSearch();

        /**
         * Perform a string based search. Could be a city name or a zip code.
         *
         * @param searchStr      The search string
         * @param isoCountryCode Country code of the user to guess country
         *                       in case zip code is not accompanied by a country code
         */
        void doStringWeatherSeach(String searchStr, String isoCountryCode);

        /**
         * Delete a recent search item
         *
         * @param search The search item to delete
         */
        void deleteRecentSearch(@NonNull Search search);
    }
}
