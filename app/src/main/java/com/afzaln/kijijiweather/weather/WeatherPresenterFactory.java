package com.afzaln.kijijiweather.weather;

import android.content.Context;

import com.afzaln.kijijiweather.Injection;
import com.afzaln.kijijiweather.util.PresenterFactory;

/**
 * Created by afzal on 2016-06-09.
 */
public class WeatherPresenterFactory extends PresenterFactory<WeatherPresenter> {
    private final String title;

    public WeatherPresenterFactory(String title) {
        this.title = title;
    }

    @Override
    public WeatherPresenter create(Context context) {
        return new WeatherPresenter(title, Injection.provideWeatherRepository(context.getApplicationContext()), Injection.provideLocationProvider(context.getApplicationContext()));
    }

}
