package com.afzaln.kijijiweather.weather;

import java.util.ArrayList;
import java.util.List;

import android.Manifest.permission;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import com.afzaln.kijijiweather.R;
import com.afzaln.kijijiweather.data.Search;
import com.afzaln.kijijiweather.data.Weather;
import com.afzaln.kijijiweather.ui.WeatherInfoView;
import com.afzaln.kijijiweather.util.BaseFragment;
import com.afzaln.kijijiweather.util.PresenterFactory;
import static com.google.common.base.Preconditions.checkNotNull;
import com.mypopsy.widget.FloatingSearchView;
import timber.log.Timber;

/**
 * Created by afzal on 2016-06-04.
 */
public class WeatherFragment extends BaseFragment<WeatherPresenter, WeatherContract.View> implements WeatherContract.View, OnRequestPermissionsResultCallback {
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int VOICE_SEARCH_REQUEST_CODE = 2;
    private boolean onActivityResultCalled;

    private WeatherContract.Presenter<WeatherContract.View> weatherPresenter;

    @BindView(R.id.root)
    FrameLayout rootView;

    @BindView(R.id.search_view)
    FloatingSearchView searchView;

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
            searchView.setActivated(false);
            searchView.setText(search.getSearchStr());
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
        if (!onActivityResultCalled) {
            super.onResume();
        } else {
            // don't autoload the last weather
            // because this is after onActivityResult called
            // from the voice input
            super.onResume(false);
        }
    }

    @Override
    protected String tag() {
        return WeatherFragment.class.getName();
    }

    @Override
    protected PresenterFactory<WeatherPresenter> getPresenterFactory() {
        return new WeatherPresenterFactory(tag());
    }

    @Override
    protected void onPresenterPrepared(WeatherPresenter presenter) {
        this.weatherPresenter = presenter;
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
        searchView.showIcon(shouldShowNavigationIcon());

        searchView.setAdapter(adapter);
        EditText searchEditText = (EditText) searchView.findViewById(R.id.fsv_search_text);
        searchEditText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        searchView.setOnSearchListener(searchStr -> {
            searchView.setActivated(false);
            Timber.d("Searching for " + searchStr);
            doWeatherStringSearch(searchStr.toString());
            showProgressBar(true);
        });

        searchView.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.voice:
                    Timber.d("Voice input");
                    doVoiceSearch();
                    break;
                case R.id.location:
                    Timber.d("location input");
                    doCoordinatesWeatherSearch();
                    break;
            }
            return true;
        });

        searchView.setOnSearchFocusChangedListener(focused -> {
            boolean textEmpty = searchView.getText().length() == 0;

            if (!focused) {
                showProgressBar(false);
            }
            searchView.showLogo(!focused && textEmpty);

            if (focused) {
                searchView.showIcon(true);
            } else {
                searchView.showIcon(shouldShowNavigationIcon());
            }
        });

        searchView.setOnIconClickListener(() -> {
            searchView.setActivated(!searchView.isActivated());
        });
    }

    private void doVoiceSearch() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, VOICE_SEARCH_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        onActivityResultCalled = true;
        if (requestCode == VOICE_SEARCH_REQUEST_CODE && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results.size() > 0) {
                doWeatherStringSearch(results.get(0));
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
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
            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
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

    private AnimationSet getAnimationSet() {
        AlphaAnimation alpha = new AlphaAnimation(0.8f, 1.0f);
        alpha.setFillAfter(true);
        TranslateAnimation slide = new TranslateAnimation(0f, 0f, 100f, 0);
        slide.setFillAfter(true);

        AnimationSet set = new AnimationSet(true);
        set.setDuration(350);
        set.setInterpolator(new DecelerateInterpolator());
        set.addAnimation(alpha);
        set.addAnimation(slide);

        return set;
    }

    @Override
    public void showWeather(Weather weather) {
        Timber.d("Showing weather for: " + weather.name);

        cityView.setText(weather.name + ", " + weather.sys.country);
        weatherTextView.setText(weather.weather[0].description);

        tempView.setText(getString(R.string.temp, weather.main.temp));
        rainView.setValue(getString(R.string.rain, weather.rain.volume));
        cloudsView.setValue(getString(R.string.clouds, weather.clouds.all));
        windView.setValue(getString(R.string.wind, weather.wind.speed));
        pressureView.setValue(getString(R.string.pressure, weather.main.pressure));
        humidityView.setValue(getString(R.string.humidity, weather.main.humidity));

//        AnimationSet animationSet = getAnimationSet();
//        weatherLayout.startAnimation(animationSet);
//        animationSet.setAnimationListener(new AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
                emptyLayout.setVisibility(View.GONE);
                weatherLayout.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
    }

    @Override
    public void showEmptyWeather() {
        Timber.d("Showing empty weather");
//        AnimationSet animationSet = getAnimationSet();
//        emptyLayout.startAnimation(animationSet);
//        animationSet.setAnimationListener(new AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
                emptyLayout.setVisibility(View.VISIBLE);
                weatherLayout.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
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
        searchView.getMenu().findItem(R.id.menu_progress).setVisible(show);
    }

    private boolean shouldShowNavigationIcon() {
        return searchView.isActivated();
    }

    @Override
    public void setPresenter(@NonNull WeatherContract.Presenter presenter) {
        weatherPresenter = checkNotNull(presenter);
    }
}
