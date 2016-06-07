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
import com.afzaln.kijijiweather.data.Weather;
import rx.Observable;
import rx.observers.TestSubscriber;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by afzal on 2016-06-04.
 */
public class WeatherRepositoryTest {

    private static final String CITY_NAME = "Toronto";

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
        when(weatherRemoteDataSource.getWeather(CITY_NAME)).thenReturn(Observable.just(new Weather()));
        Observable<ArrayList<Search>> just = Observable.just(listOf("test1", "test2"));
        OngoingStubbing<Observable<? extends List<Search>>> when = when(weatherLocalDataSource.getRecentSearches());
        when.thenReturn(just);

        weatherRepository = WeatherRepository.getInstance(weatherRemoteDataSource, weatherLocalDataSource);
    }

    public static ArrayList<Search> listOf(String... searchStrs) {
        ArrayList<Search> searches = new ArrayList<>();
        for (String searchStr : searchStrs) {
            Search search = new Search();
            search.setTimestamp(System.currentTimeMillis());
            search.setSearchStr(searchStr);
            searches.add(search);
        }

        return searches;
    }

    @After
    public void destroy() {
        WeatherRepository.destroyInstance();
    }

    @Test
    public void getWeather_requestWeatherFromApi() {
        TestSubscriber<Weather> testSubscriber = new TestSubscriber<>();

        weatherRepository.getWeather(CITY_NAME).subscribe(testSubscriber);

        List<Weather> onNextEvents = testSubscriber.getOnNextEvents();

        verify(weatherRemoteDataSource).getWeather(CITY_NAME);
        verify(weatherLocalDataSource, never()).getWeather(CITY_NAME);
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
        weatherRepository.saveRecentSearch("test1");

        verify(weatherRemoteDataSource, never()).saveRecentSearch("test1");
        verify(weatherLocalDataSource).saveRecentSearch("test1");
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
