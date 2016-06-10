package com.afzaln.kijijiweather.data;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.List;

import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.afzaln.kijijiweather.data.source.WeatherDataSource;
import com.afzaln.kijijiweather.data.source.local.WeatherLocalDataSource;
import rx.observers.TestSubscriber;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by afzal on 2016-06-05.
 */
@RunWith(AndroidJUnit4.class)
public class WeatherLocalDataSourceTest {
    private WeatherDataSource weatherLocalDataSource;

    @Before
    public void setup() {
        weatherLocalDataSource = WeatherLocalDataSource.getInstance(InstrumentationRegistry.getTargetContext());
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        weatherLocalDataSource.deleteAllRecentSearches();
    }

    @Test
    public void getRecentSearches_empty() {
        TestSubscriber<List<Search>> testSubscriber = new TestSubscriber<>();
        weatherLocalDataSource.getRecentSearches().subscribe(testSubscriber);

        List<Search> onNextEvents = testSubscriber.getOnNextEvents().get(0);

        // Search list is empty because no recent searches
        assertThat(onNextEvents.isEmpty(), is(true));
    }

    @Test
    public void saveRecentSearch() {
        populateSearches(1);
        TestSubscriber<List<Search>> testSubscriber = new TestSubscriber<>();
        weatherLocalDataSource.getRecentSearches().subscribe(testSubscriber);

        List<Search> onNextEvents = testSubscriber.getOnNextEvents().get(0);

        assertThat(onNextEvents.size(), is(1));
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void saveRecentSearch_noLatLonSet() {
        Search search = new Search();
        search.setSearchStr("Random string");
        exception.expect(IllegalArgumentException.class);
        weatherLocalDataSource.saveRecentSearch(search);
    }

    @Test
    public void getRecentSearches_populated() {
        populateSearches(10);
        TestSubscriber<List<Search>> testSubscriber = new TestSubscriber<>();
        weatherLocalDataSource.getRecentSearches().subscribe(testSubscriber);

        List<Search> onNextEvents = testSubscriber.getOnNextEvents().get(0);

        assertThat(onNextEvents.size(), is(10));
    }

    @Test
    public void deleteRecentSearch() {
        populateSearches(1);
        TestSubscriber<List<Search>> testSubscriber = new TestSubscriber<>();
        weatherLocalDataSource.getRecentSearches().subscribe(testSubscriber);
        List<Search> onNextEvents = testSubscriber.getOnNextEvents().get(0);

        long timestamp = onNextEvents.get(0).getTimestamp();
        weatherLocalDataSource.deleteRecentSearch(timestamp);

        TestSubscriber<List<Search>> testSubscriber2 = new TestSubscriber<>();
        weatherLocalDataSource.getRecentSearches().subscribe(testSubscriber2);
        List<Search> onNextEvents2 = testSubscriber2.getOnNextEvents().get(0);

        assertThat(onNextEvents2.size(), is(0));
    }

    @Test
    public void deleteAllRecentSearches() {
        populateSearches(10);
        TestSubscriber<List<Search>> testSubscriber = new TestSubscriber<>();
        weatherLocalDataSource.getRecentSearches().subscribe(testSubscriber);
        List<Search> onNextEvents = testSubscriber.getOnNextEvents().get(0);

        weatherLocalDataSource.deleteAllRecentSearches();

        TestSubscriber<List<Search>> testSubscriber2 = new TestSubscriber<>();
        weatherLocalDataSource.getRecentSearches().subscribe(testSubscriber2);
        List<Search> onNextEvents2 = testSubscriber2.getOnNextEvents().get(0);

        assertThat(onNextEvents2.size(), is(0));
    }

    public void populateSearches(int num) {
        for (int i = 0, iSize = num; i < iSize; i++) {
            Search search = new Search();
            search.setLatLon(Math.random(), Math.random());
            weatherLocalDataSource.saveRecentSearch(search);
        }
    }
}
