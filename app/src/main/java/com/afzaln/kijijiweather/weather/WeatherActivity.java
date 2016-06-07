package com.afzaln.kijijiweather.weather;

import android.os.Bundle;

import butterknife.ButterKnife;
import com.afzaln.kijijiweather.Injection;
import com.afzaln.kijijiweather.R;
import com.afzaln.kijijiweather.util.BaseActivity;

/**
 * Created by afzal on 2016-06-04.
 */
public class WeatherActivity extends BaseActivity {

    WeatherPresenter weatherPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_activity);
        ButterKnife.bind(this);

        WeatherFragment weatherFragment = (WeatherFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);

        if (weatherFragment == null) {
            // Create the fragment
            weatherFragment = WeatherFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, weatherFragment)
                    .commit();
        }

        // Create the presenter
        weatherPresenter = new WeatherPresenter(Injection.provideWeatherRepository(getApplicationContext()), weatherFragment);

        // do things with restoring instance state
        if (savedInstanceState != null) {

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // do things with saving instance state
        super.onSaveInstanceState(outState);
    }
}
