package com.afzaln.kijijiweather.weather;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

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
    private WeatherContract.Presenter mPresenter;

    @BindView(R.id.search_view)
    protected FloatingSearchView mSearchView;
    private SearchItemClickListener searchClickListener = new SearchItemClickListener() {
        @Override
        public void delete(Search search) {
            Timber.d("deleted " + search.getSearchStr());
        }

        @Override
        public void search(Search search) {
            Timber.d("Searched for %s", search.getSearchStr());
            mSearchView.setActivated(false);
            mSearchView.setText(search.getSearchStr());
            showProgressBar(true);
        }
    };


    public static WeatherFragment newInstance() {
        WeatherFragment fragment = new WeatherFragment();
        fragment.setLayout(R.layout.weather_fragment);

        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.subscribe();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.unsubscribe();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SearchAdapter adapter = new SearchAdapter(searchClickListener);
        ArrayList<Search> searches = new ArrayList<>();
        searches.add(new Search("Toronto, Canada"));
        searches.add(new Search("Portland, United States"));
        searches.add(new Search("London, United Kingdom"));
        adapter.setSearches(searches);

        initSearchView(adapter);
    }

    private void initSearchView(SearchAdapter adapter) {
        mSearchView.showIcon(shouldShowNavigationIcon());

        mSearchView.setAdapter(adapter);
        EditText searchEditText = (EditText) mSearchView.findViewById(R.id.fsv_search_text);
        searchEditText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        mSearchView.setOnSearchListener(charSequence -> {
            mSearchView.setActivated(false);
            Timber.d("Searched for " + charSequence);
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

    @Override
    public void setLoadingIndicator(boolean show) {

    }

    @Override
    public void showWeather(Weather weather) {

    }

    @Override
    public void showNoWeather() {

    }

    @Override
    public void showError(String message) {

    }

    private void showProgressBar(boolean show) {
        mSearchView.getMenu().findItem(R.id.menu_progress).setVisible(show);
    }

    private boolean shouldShowNavigationIcon() {
        return mSearchView.isActivated();
    }

    @Override
    public void setPresenter(@NonNull WeatherContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }
}
