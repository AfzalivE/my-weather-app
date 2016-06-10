package com.afzaln.kijijiweather.weather;

import android.content.Context;

import com.afzaln.kijijiweather.Injection;
import com.afzaln.kijijiweather.util.PresenterFactory;

/**
 * Factory to create a presenter with all the parameters
 */
public class WeatherPresenterFactory extends PresenterFactory<WeatherPresenter> {
    @Override
    public WeatherPresenter create(Context context) {
        return new WeatherPresenter(
                Injection.provideWeatherRepository(context.getApplicationContext()),
                Injection.provideLocationProvider(context.getApplicationContext())
        );
    }

}
