package com.afzaln.kijijiweather.weather;

import android.os.Bundle;

import butterknife.ButterKnife;
import com.afzaln.kijijiweather.R;
import com.afzaln.kijijiweather.util.BaseActivity;

/**
 * Created by afzal on 2016-06-04.
 */
public class WeatherActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_activity);
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
