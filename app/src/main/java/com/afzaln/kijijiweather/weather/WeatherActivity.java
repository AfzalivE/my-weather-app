package com.afzaln.kijijiweather.weather;

import android.os.Bundle;

import butterknife.ButterKnife;
import com.afzaln.kijijiweather.R;
import com.afzaln.kijijiweather.util.BaseActivity;

/**
 * Main entry point of the app
 */
public class WeatherActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_activity);
        // to slightly improve overdraw performance
        getWindow().setBackgroundDrawable(null);
        ButterKnife.bind(this);

        WeatherFragment weatherFragment = (WeatherFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);

        if (weatherFragment == null) {
            // Create the fragment
            weatherFragment = WeatherFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, weatherFragment)
                    .commit();
        }
    }
}
