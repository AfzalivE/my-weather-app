package com.afzaln.kijijiweather.weather;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.OngoingStubbing;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Location;

import com.afzaln.kijijiweather.data.Search;
import static com.afzaln.kijijiweather.data.TestUtils.listOf;
import com.afzaln.kijijiweather.data.Weather;
import com.afzaln.kijijiweather.data.Weather.Coord;
import com.afzaln.kijijiweather.data.source.WeatherRepository;
import com.afzaln.kijijiweather.data.source.location.LocationProvider;
import com.afzaln.kijijiweather.weather.WeatherContract.View;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import rx.Observable;
import rx.schedulers.TestScheduler;

/**
 * Created by afzal on 2016-06-10.
 */
public class WeatherPresenterTest {
    TestScheduler testScheduler = new TestScheduler();

    static Search CITY_NAME_SEARCH = new Search();
    static Weather responseWeather = new Weather();

    static Location lastLocation = new Location("test");

    static {
        CITY_NAME_SEARCH.setSearchStr("Toronto");
        CITY_NAME_SEARCH.setLatLon(0, 0);
        responseWeather.name = "test";
        responseWeather.coord = new Coord();
        responseWeather.coord.lat = 0;
        responseWeather.coord.lon = 0;

        lastLocation.setLatitude(0);
        lastLocation.setLongitude(0);
    }

    @Mock
    private WeatherRepository weatherRepository;

    @Mock
    private Context context;

    @Mock
    View weatherView;

    @Mock
    private LocationProvider locationProvider;
    private WeatherPresenter presenter;
    private ArrayList<Search> TEST_SEARCHES = listOf("test1", "test2");

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(weatherRepository.getWeather(CITY_NAME_SEARCH)).thenReturn(Observable.just(responseWeather).subscribeOn(testScheduler));

        OngoingStubbing<Observable<? extends List<Search>>> getRecentSearches = when(weatherRepository.getRecentSearches());
        Observable<ArrayList<Search>> just = Observable.just(TEST_SEARCHES).subscribeOn(testScheduler);
        getRecentSearches.thenReturn(just);

        when(locationProvider.getLastLocation()).thenReturn(Observable.just(lastLocation));
        presenter = new WeatherPresenter("test", weatherRepository, locationProvider);
        presenter.onViewAttached(weatherView, false);
    }

    @Test
    public void doStringWeatherSearch()  {
        presenter.doStringWeatherSeach("Toronto", "ca");
        testScheduler.triggerActions();

        verify(weatherView).showWeather(responseWeather, true);
    }


    @Test
    public void doCoordinatesWeatherSearch() {
        presenter.doCoordinatesWeatherSearch();
        testScheduler.triggerActions();

        verify(weatherView).showWeather(responseWeather, true);
    }

    @Test
    public void deleteRecentSearch()  {
        presenter.deleteRecentSearch(CITY_NAME_SEARCH);
        testScheduler.triggerActions();

        verify(weatherView).populateRecentSearches(TEST_SEARCHES);
    }
}
