package com.afzaln.kijijiweather.data.source;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.OngoingStubbing;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.afzaln.kijijiweather.data.Search;
import com.afzaln.kijijiweather.data.TestUtils;
import com.afzaln.kijijiweather.data.Weather;
import com.afzaln.kijijiweather.data.Weather.Coord;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import rx.Observable;
import rx.observers.TestSubscriber;

/**
 * Created by afzal on 2016-06-04.
 */
public class WeatherRepositoryTest {
    static Search CITY_NAME_SEARCH = new Search();
    static Search TEST_SEARCH = new Search();

    static {
        CITY_NAME_SEARCH.setSearchStr("Toronto");
    }

    private WeatherRepository weatherRepository;

    @Mock
    private WeatherDataSource weatherRemoteDataSource;

    @Mock
    private WeatherDataSource weatherLocalDataSource;

    @Mock
    private Context context;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
//        weatherRemoteDataSource = WeatherRemoteDataSource.getInstance();
        Weather responseWeather = new Weather();
        responseWeather.coord = new Coord();
        responseWeather.coord.lat = 0;
        responseWeather.coord.lon = 0;
        when(weatherRemoteDataSource.getWeather(CITY_NAME_SEARCH)).thenReturn(Observable.just(responseWeather));
        Observable<ArrayList<Search>> just = Observable.just(TestUtils.listOf("test1", "test2"));
        OngoingStubbing<Observable<? extends List<Search>>> when = when(weatherLocalDataSource.getRecentSearches());
        when.thenReturn(just);

        weatherRepository = WeatherRepository.getInstance(weatherRemoteDataSource, weatherLocalDataSource);
    }

    @After
    public void destroy() {
        WeatherRepository.destroyInstance();
    }

    @Test
    public void getWeather_requestWeatherFromApi() {
        TestSubscriber<Weather> testSubscriber = new TestSubscriber<>();

        weatherRepository.getWeather(CITY_NAME_SEARCH).subscribe(testSubscriber);

        List<Weather> onNextEvents = testSubscriber.getOnNextEvents();

        verify(weatherRemoteDataSource).getWeather(CITY_NAME_SEARCH);
        verify(weatherLocalDataSource, never()).getWeather(CITY_NAME_SEARCH);
        assertThat(weatherRepository.cachedWeather.size(), is(1));
    }

    @Test
    public void getRecentSearches() {
        TestSubscriber<List<Search>> testSubscriber = new TestSubscriber<>();

        weatherRepository.getRecentSearches().subscribe(testSubscriber);

        List<Search> onNextEvents = testSubscriber.getOnNextEvents().get(0);

        verify(weatherRemoteDataSource, never()).getRecentSearches();
        verify(weatherLocalDataSource).getRecentSearches();
        assertThat(onNextEvents.size(), is(2));
    }

    @Test
    public void saveRecentSearch() {
        weatherRepository.saveRecentSearch(TEST_SEARCH);

        verify(weatherRemoteDataSource, never()).saveRecentSearch(TEST_SEARCH);
        verify(weatherLocalDataSource).saveRecentSearch(TEST_SEARCH);
    }

    @Test
    public void deleteRecentSearch() {
        long timestamp = System.currentTimeMillis();

        weatherRepository.deleteRecentSearch(timestamp);

        verify(weatherRemoteDataSource, never()).deleteRecentSearch(timestamp);
        verify(weatherLocalDataSource).deleteRecentSearch(timestamp);
    }

    @Test
    public void deleteAllRecentSearches() {
        weatherRepository.deleteAllRecentSearches();

        verify(weatherRemoteDataSource, never()).deleteAllRecentSearches();
        verify(weatherLocalDataSource).deleteAllRecentSearches();
    }
}
