package com.afzaln.kijijiweather.data.source;

import org.junit.Before;
import org.junit.Test;

import com.afzaln.kijijiweather.data.Weather;
import com.afzaln.kijijiweather.data.source.remote.WeatherRemoteDataSource;
import rx.observers.TestSubscriber;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by afzal on 2016-06-05.
 */
public class WeatherRemoteDataSourceTest {
    private static final String CITY_NAME = "Toronto";

    private WeatherDataSource weatherRemoteDataSource;

    @Before
    public void setup() {
        weatherRemoteDataSource = WeatherRemoteDataSource.getInstance();
    }

    /**
     * Just a test to make sure that the remote source is working fine
     * and weather response is being fetched
     */
    @Test
    public void getWeather_requestWeatherFromApi() {
        TestSubscriber<Weather> testSubscriber = new TestSubscriber<>();
        weatherRemoteDataSource.getWeather(CITY_NAME).subscribe(testSubscriber);

        Weather onNextEvents = testSubscriber.getOnNextEvents().get(0);
        assertThat(onNextEvents.name, is(CITY_NAME));
    }
}
