package com.afzaln.kijijiweather.weather;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import com.afzaln.kijijiweather.R;
import com.afzaln.kijijiweather.data.Search;
import com.afzaln.kijijiweather.data.source.WeatherRepository;
import static com.google.common.base.Preconditions.checkArgument;

import com.afzaln.kijijiweather.Injection;
import static org.hamcrest.Matchers.allOf;

/**
 * Created by afzal on 2016-06-09.
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
                    search.setSearchStr("test");
                    search.setLatLon(0, 0);
                    weatherRepository.saveRecentSearch(search);

                }
            };

    /**
     * A custom {@link Matcher} which matches an item in a {@link ListView} by its text.
     * <p>
     * View constraints:
     * <ul>
     * <li>View must be a child of a {@link ListView}
     * <ul>
     *
     * @param itemText the text to match
     *
     * @return Matcher that matches text in the given view
     */
    private Matcher<View> withItemText(final String itemText) {
        checkArgument(!TextUtils.isEmpty(itemText), "itemText cannot be null or empty");
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View item) {
                return allOf(
                        isDescendantOfA(isAssignableFrom(ListView.class)),
                        withText(itemText)).matches(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is isDescendantOfA LV with text " + itemText);
            }
        };
    }

    @Test
    public void clickSearchView_opensRecentList() {
        // Click on the search view
        onView(withId(R.id.search_view)).perform(click());

        // Check if an element from the recent_search_items.xml is visible
        onView(withId(R.id.icon_end)).check(matches(isDisplayed()));
//        onView(withId(R.id.text)).check(matches(isDisplayed()));
//        onView(withId(R.id.icon_end)).check(matches(isDisplayed()));
    }

    @Test
    public void clickDeleteIcon_removesItemFromList() {

    }

    @Test
    public void clickVoiceIcon_opensGoogleSpeechActivity() {

    }

    @Test
    public void clickLocationIcon() {

    }

}
