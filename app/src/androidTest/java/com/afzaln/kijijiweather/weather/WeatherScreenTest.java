package com.afzaln.kijijiweather.weather;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import com.afzaln.kijijiweather.Injection;
import com.afzaln.kijijiweather.R;
import com.afzaln.kijijiweather.TestUtils.RecyclerViewItemCountAssertion;
import com.afzaln.kijijiweather.data.Search;
import com.afzaln.kijijiweather.data.source.WeatherRepository;
import static org.hamcrest.Matchers.allOf;

/**
 * Instrumentation tests for WeatherActivity
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class WeatherScreenTest {
    /**
     * {@link ActivityTestRule} is a JUnit {@link Rule @Rule} to launch your activity under test.
     * <p>
     * Rules are interceptors which are executed for each test method and are important building
     * blocks of Junit tests.
     */
    @Rule
    public ActivityTestRule<WeatherActivity> mTasksActivityTestRule =
            new ActivityTestRule<WeatherActivity>(WeatherActivity.class) {

                /**
                 * To avoid a long list of searches and the need to scroll through the list to find a
                 * task, we call {@link WeatherDataSource#deleteAllRecentSearches()} before each test.
                 */
                @Override
                protected void beforeActivityLaunched() {
                    super.beforeActivityLaunched();
                    // Doing this in @Before generates a race condition.
                    WeatherRepository weatherRepository = Injection.provideWeatherRepository(InstrumentationRegistry.getTargetContext());
                    weatherRepository.deleteAllRecentSearches();
                    Search search = new Search();
                    search.setSearchStr("Earth");
                    search.setLatLon(0, 0);
                    weatherRepository.saveRecentSearch(search);

                }
            };

    @Test
    public void clickSearchView_opensRecentList() throws InterruptedException {
        // Click on the search view
        onView(withId(R.id.fsv_search_container)).perform(click());
        // Check if an element from the recent_search_items.xml is visible
        ViewInteraction viewInteraction = onView(allOf(withId(R.id.fsv_suggestions_list)));
        viewInteraction.check(matches(isDisplayed()));
        onView(allOf(withId(R.id.icon_start))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.icon_end))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.text))).check(matches(isDisplayed()));
    }

    @Test
    public void clickDeleteIcon_removesItemFromList() throws InterruptedException {
        // Click on the search view
        onView(withId(R.id.fsv_search_container)).perform(click());

        ViewInteraction viewInteraction = onView(allOf(withId(R.id.icon_end), isDisplayed()));
        viewInteraction.perform(click());

        onView(withId(R.id.fsv_suggestions_list)).check(new RecyclerViewItemCountAssertion(0));
    }

    @Test
    public void clickRecentSearch_changesCityName() throws InterruptedException {
        // Click on the search view
        onView(withId(R.id.fsv_search_container)).perform(click());

        // Check if an element from the recent_search_items.xml is visible
        ViewInteraction viewInteraction = onView(allOf(withId(R.id.text), isDisplayed()));

        viewInteraction.perform(click());

        Thread.sleep(1000); // should use EspressoIdling instead of this
        onView(withId(R.id.city)).check(matches(withText("Earth, none")));
    }

    @Test
    public void enterSearchString_changesCityName() throws InterruptedException {
        // Click on the search view
        onView(withId(R.id.fsv_search_text)).perform(typeText("London"), pressImeActionButton());
        Thread.sleep(1000);
        onView(withText("London, GB")).check(matches(isDisplayed()));
    }
}
