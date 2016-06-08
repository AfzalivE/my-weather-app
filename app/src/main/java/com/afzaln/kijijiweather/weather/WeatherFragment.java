package com.afzaln.kijijiweather.weather;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import com.afzaln.kijijiweather.R;
import com.afzaln.kijijiweather.data.Search;
import com.afzaln.kijijiweather.data.Weather;
import com.afzaln.kijijiweather.util.BaseFragment;
import com.mypopsy.widget.FloatingSearchView;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by afzal on 2016-06-04.
 */
public class WeatherFragment extends BaseFragment implements WeatherContract.View {
    private WeatherContract.Presenter weatherPresenter;

    @BindView(R.id.search_view)
    FloatingSearchView mSearchView;

    @BindView(R.id.empty_layout)
    LinearLayout emptyLayout;

    @BindView(R.id.weather_layout)
    LinearLayout weatherLayout;

    @BindView(R.id.city)
    TextView cityView;

    @BindView(R.id.temp)
    TextView tempView;

    @BindView(R.id.weather_text)
    TextView weatherTextView;

    private SearchItemClickListener searchClickListener = new SearchItemClickListener() {
        @Override
        public void delete(Search search) {
            deleteSearch(search);
        }

        @Override
        public void search(Search search) {
            Timber.d("Searched for %s", search.getSearchStr());
            mSearchView.setActivated(false);
            mSearchView.setText(search.getSearchStr());
            showProgressBar(true);
            executeSearch(search.getSearchStr(), true);
        }
    };

    private void deleteSearch(Search search) {
        String searchStr = search.getSearchStr();
        weatherPresenter.deleteRecentSearch(search);
        Timber.d("deleted " + searchStr);
    }

    private SearchAdapter searchAdapter;

    public static WeatherFragment newInstance() {
        WeatherFragment fragment = new WeatherFragment();
        fragment.setLayout(R.layout.weather_fragment);

        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        weatherPresenter.subscribe();
    }

    @Override
    public void onPause() {
        super.onPause();
        weatherPresenter.unsubscribe();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchAdapter = new SearchAdapter(searchClickListener);
//        ArrayList<Search> searches = new ArrayList<>();
//        searches.add(new Search("Toronto, Canada"));
//        searches.add(new Search("Portland, United States"));
//        searches.add(new Search("London, United Kingdom"));
//        searchAdapter.setSearches(searches);

        initSearchView(searchAdapter);
    }

    private void initSearchView(SearchAdapter adapter) {
        mSearchView.showIcon(shouldShowNavigationIcon());

        mSearchView.setAdapter(adapter);
        EditText searchEditText = (EditText) mSearchView.findViewById(R.id.fsv_search_text);
        searchEditText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        mSearchView.setOnSearchListener(searchStr -> {
            mSearchView.setActivated(false);
            Timber.d("Searching for " + searchStr);
            executeSearch(searchStr.toString(), false);
            showProgressBar(true);
        });

        mSearchView.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.voice:
                    Timber.d("Voice input");
                    break;
                case R.id.location:
                    Timber.d("location input");
                    break;
            }
            return true;
        });

        mSearchView.setOnSearchFocusChangedListener(focused -> {
            boolean textEmpty = mSearchView.getText().length() == 0;

            if (!focused) {
                showProgressBar(false);
            }
            mSearchView.showLogo(!focused && textEmpty);

            if (focused) {
                mSearchView.showIcon(true);
            } else {
                mSearchView.showIcon(shouldShowNavigationIcon());
            }
        });

        mSearchView.setOnIconClickListener(() -> {
            mSearchView.setActivated(!mSearchView.isActivated());
        });
    }

    private void executeSearch(String searchStr, boolean isFromRecentSearch) {
        weatherPresenter.doWeatherSearch(searchStr, isFromRecentSearch);
    }

    @Override
    public void setLoadingIndicator(boolean show) {
        showProgressBar(show);
    }

    @Override
    public void showWeather(Weather weather) {
        Timber.d("Showing weather for: " + weather.name);
        emptyLayout.setVisibility(View.GONE);
        weatherLayout.setVisibility(View.VISIBLE);

        cityView.setText(weather.name);
        tempView.setText(String.format("%d C", String.valueOf(weather.main.temp)));
        weatherTextView.setText(weather.weather[0].description);

    }

    @Override
    public void showEmptyWeather() {
        Timber.d("Showing empty weather");
        emptyLayout.setVisibility(View.VISIBLE);
        weatherLayout.setVisibility(View.GONE);
    }

    @Override
    public void showError(String message) {

    }

    @Override
    public void populateRecentSearches(List<Search> searches) {
        Timber.d("Populating recent searches");
        searchAdapter.setSearches(searches);
    }

    private void showProgressBar(boolean show) {
        mSearchView.getMenu().findItem(R.id.menu_progress).setVisible(show);
    }

    private boolean shouldShowNavigationIcon() {
        return mSearchView.isActivated();
    }

    @Override
    public void setPresenter(@NonNull WeatherContract.Presenter presenter) {
        weatherPresenter = checkNotNull(presenter);
    }
}
