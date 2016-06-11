package com.afzaln.kijijiweather.weather;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.OngoingStubbing;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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
import com.afzaln.kijijiweather.util.RxUtils;
import com.afzaln.kijijiweather.weather.WeatherContract.View;
import junit.framework.Assert;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

/**
 * Tests for WeatherPresenter
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(RxUtils.class)
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
        mockStatic(RxUtils.class);
        when(RxUtils.applySchedulers()).thenReturn(objectObservable -> objectObservable
                .observeOn(Schedulers.immediate())
                .subscribeOn(Schedulers.immediate()));

        when(RxUtils.observeInBackground()).thenReturn(objectObservable -> objectObservable
                .observeOn(Schedulers.immediate())
                .subscribeOn(Schedulers.immediate()));

        MockitoAnnotations.initMocks(this);

        when(weatherRepository.getWeather(any(Search.class))).thenReturn(Observable.just(responseWeather));
        OngoingStubbing<Observable<? extends List<Search>>> getRecentSearches = when(weatherRepository.getRecentSearches());
        Observable<ArrayList<Search>> just = Observable.just(TEST_SEARCHES);
        getRecentSearches.thenReturn(just);

        when(locationProvider.getLastLocation()).thenReturn(Observable.just(lastLocation));

        presenter = new WeatherPresenter(weatherRepository, locationProvider);
        presenter.onViewAttached(weatherView, false);
    }

    @Test
    public void doStringWeatherSearch()  {
        presenter.doStringWeatherSeach("Toronto", "ca");
        testScheduler.triggerActions();

        ArgumentCaptor<Boolean> progressCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(weatherView, times(2)).showProgressBar(progressCaptor.capture());
        Assert.assertEquals(Boolean.TRUE, progressCaptor.getAllValues().get(0));
        Assert.assertEquals(Boolean.FALSE, progressCaptor.getAllValues().get(1));

        verify(weatherRepository).getWeather(any(Search.class));
        verify(weatherRepository).getRecentSearches();
        verify(weatherView).showWeather(responseWeather, true);
        verify(weatherView).populateRecentSearches(TEST_SEARCHES);
    }


    @Test
    public void doCoordinatesWeatherSearch() {
        presenter.doCoordinatesWeatherSearch();
        testScheduler.triggerActions();

        ArgumentCaptor<Boolean> progressCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(weatherView, times(2)).showProgressBar(progressCaptor.capture());
        Assert.assertEquals(Boolean.TRUE, progressCaptor.getAllValues().get(0));
        Assert.assertEquals(Boolean.FALSE, progressCaptor.getAllValues().get(1));

        verify(weatherRepository).getWeather(any(Search.class));
        verify(weatherRepository).getRecentSearches();
        verify(weatherView).showWeather(responseWeather, true);
        verify(weatherView).populateRecentSearches(TEST_SEARCHES);
    }

    @Test
    public void deleteRecentSearch()  {
        presenter.deleteRecentSearch(CITY_NAME_SEARCH);
        testScheduler.triggerActions();

        verify(weatherRepository).deleteRecentSearch(CITY_NAME_SEARCH.getTimestamp());
        verify(weatherRepository).getRecentSearches();
        verify(weatherView).populateRecentSearches(TEST_SEARCHES);
    }
}
