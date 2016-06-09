package com.afzaln.kijijiweather.weather;

import java.util.List;

import android.Manifest.permission;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import com.afzaln.kijijiweather.R;
import com.afzaln.kijijiweather.data.Search;
import com.afzaln.kijijiweather.data.Weather;
import com.afzaln.kijijiweather.ui.WeatherInfoView;
import com.afzaln.kijijiweather.util.BaseFragment;
import static com.google.common.base.Preconditions.checkNotNull;
import com.mypopsy.widget.FloatingSearchView;
import timber.log.Timber;

/**
 * Created by afzal on 2016-06-04.
 */
public class WeatherFragment extends BaseFragment implements WeatherContract.View, OnRequestPermissionsResultCallback {
    private static final int PERMISSION_REQUEST_CODE = 1;

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

    @BindView(R.id.rain)
    WeatherInfoView rainView;

    @BindView(R.id.clouds)
    WeatherInfoView cloudsView;

    @BindView(R.id.wind)
    WeatherInfoView windView;

    @BindView(R.id.pressure)
    WeatherInfoView pressureView;

    @BindView(R.id.humidity)
    WeatherInfoView humidityView;

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
            doWeatherStringSearch(search.getSearchStr());
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
            doWeatherStringSearch(searchStr.toString());
            showProgressBar(true);
        });

        mSearchView.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.voice:
                    Timber.d("Voice input");
                    break;
                case R.id.location:
                    Timber.d("location input");
                    doCoordinatesWeatherSearch();
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

    private void doCoordinatesWeatherSearch() {
        if (ActivityCompat.checkSelfPermission(getActivity(), permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            return;
        }

        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean gpsProviderEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkProviderEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!gpsProviderEnabled && !networkProviderEnabled) {
            // TODO show alert dialog to enable location services
            Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(myIntent);

            return;
        }

        weatherPresenter.doCoordinatesWeatherSearch();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    weatherPresenter.doCoordinatesWeatherSearch();
                } else {
                    showPermissionDeniedDialog();
                }
                break;
        }
    }

    private void showPermissionDeniedDialog() {
        throw new UnsupportedOperationException("Not implemented");
    }

    private void doWeatherStringSearch(String searchStr) {
        weatherPresenter.doStringWeatherSeach(searchStr);
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

        cityView.setText(weather.name + ", " + weather.sys.country);
        weatherTextView.setText(weather.weather[0].description);

        tempView.setText(getString(R.string.temp, weather.main.temp));
        rainView.setValue(getString(R.string.rain, weather.rain.volume));
        cloudsView.setValue(getString(R.string.clouds, weather.clouds.all));
        windView.setValue(getString(R.string.wind, weather.wind.speed));
        pressureView.setValue(getString(R.string.pressure, weather.main.pressure));
        humidityView.setValue(getString(R.string.humidity, weather.main.humidity));

    }

    @Override
    public void showEmptyWeather() {
        Timber.d("Showing empty weather");
        emptyLayout.setVisibility(View.VISIBLE);
        weatherLayout.setVisibility(View.GONE);
    }

    @Override
    public void showError(String message) {
        Timber.d("Error occurred: " + message);
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
