package com.afzaln.kijijiweather.data.source;

import org.junit.Before;
import org.junit.Test;

import com.afzaln.kijijiweather.data.Search;
import com.afzaln.kijijiweather.data.Weather;
import com.afzaln.kijijiweather.data.source.remote.WeatherRemoteDataSource;
import rx.observers.TestSubscriber;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by afzal on 2016-06-05.
 */
public class WeatherRemoteDataSourceTest {
    static Search CITY_NAME_SEARCH = new Search();

    static Search ZIPCODE_SEARCH = new Search();
    static String ZIPCODE_SEARCH_NAME = "Downtown Toronto";

    static Search COORDINATES_SEARCH = new Search();
    static String COORDINATES_SEARCH_NAME = "Portland";

    static {
        CITY_NAME_SEARCH.setSearchStr("Toronto");
        ZIPCODE_SEARCH.setZipCode("M5C1G3, CA");
        COORDINATES_SEARCH.setLatLon(45.523062, -122.676482);
    }

    private WeatherDataSource weatherRemoteDataSource;

    @Before
    public void setup() {
        weatherRemoteDataSource = WeatherRemoteDataSource.getInstance();
    }

    /**
     * Do a city name search and match city names
     */
    @Test
    public void getWeather_cityName() {
        TestSubscriber<Weather> testSubscriber = new TestSubscriber<>();
        weatherRemoteDataSource.getWeather(CITY_NAME_SEARCH).subscribe(testSubscriber);

        Weather onNextEvents = testSubscriber.getOnNextEvents().get(0);
        assertThat(onNextEvents.name, is(CITY_NAME_SEARCH.getSearchStr()));
    }

    /**
     * Do a city name search and match city names
     */
    @Test
    public void getWeather_zipCode() {
        TestSubscriber<Weather> testSubscriber = new TestSubscriber<>();
        weatherRemoteDataSource.getWeather(ZIPCODE_SEARCH).subscribe(testSubscriber);

        Weather onNextEvents = testSubscriber.getOnNextEvents().get(0);
        assertThat(onNextEvents.name, is(ZIPCODE_SEARCH_NAME));
    }

    /**
     * Do a city name search and match city names
     */
    @Test
    public void getWeather_coordinates() {
        TestSubscriber<Weather> testSubscriber = new TestSubscriber<>();
        weatherRemoteDataSource.getWeather(COORDINATES_SEARCH).subscribe(testSubscriber);

        Weather onNextEvents = testSubscriber.getOnNextEvents().get(0);
        assertThat(onNextEvents.name, is(COORDINATES_SEARCH_NAME));
    }
}
