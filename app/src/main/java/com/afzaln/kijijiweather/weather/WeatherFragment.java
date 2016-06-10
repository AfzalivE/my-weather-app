package com.afzaln.kijijiweather.weather;

import java.util.ArrayList;
import java.util.List;

import android.Manifest.permission;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindString;
import butterknife.BindView;
import com.afzaln.kijijiweather.R;
import com.afzaln.kijijiweather.data.Search;
import com.afzaln.kijijiweather.data.Weather;
import com.afzaln.kijijiweather.ui.WeatherInfoView;
import com.afzaln.kijijiweather.util.BaseFragment;
import com.afzaln.kijijiweather.util.PresenterFactory;
import com.afzaln.kijijiweather.util.ResourceUtils;
import com.mypopsy.drawable.SearchArrowDrawable;
import com.mypopsy.widget.FloatingSearchView;
import timber.log.Timber;

/**
 * Created by afzal on 2016-06-04.
 */
public class WeatherFragment extends BaseFragment<WeatherPresenter, WeatherContract.View> implements WeatherContract.View, OnRequestPermissionsResultCallback {
    // For Location Permission
    private static final int PERMISSION_REQUEST_CODE = 1;
    // For Voice search intent
    private static final int VOICE_SEARCH_REQUEST_CODE = 2;
    // To make sure that last weather is not loaded after voice search
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

    @BindView(R.id.weather_icon)
    ImageView weatherIconView;

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

    // Based on the flavour of the app
    @BindString(R.string.country_code)
    String countryCode;

    private SearchItemClickListener searchClickListener = new SearchItemClickListener() {
        @Override
        public void delete(Search search) {
            // delete the desired recent search
            String searchStr = search.getSearchStr();
            weatherPresenter.deleteRecentSearch(search);
            Timber.d("deleted %s", searchStr);
        }

        @Override
        public void search(Search search) {
            // Collapse the search bar and start loading the
            // desired weather
            Timber.d("Searched for %s", search.getSearchStr());
            searchView.setActivated(false);
            searchView.setText(search.getSearchStr());
            showProgressBar(true);
            doWeatherStringSearch(search.getSearchStr());
        }
    };

    private SearchAdapter searchAdapter;

    public static WeatherFragment newInstance() {
        WeatherFragment fragment = new WeatherFragment();
        // This layout is inflated in BaseFragment
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
        return new WeatherPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(WeatherPresenter presenter) {
        this.weatherPresenter = presenter;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        searchAdapter = new SearchAdapter(searchClickListener);
        initSearchView(searchAdapter);
    }

    private void initSearchView(SearchAdapter adapter) {
        searchView.showIcon(true);

        searchView.setAdapter(adapter);
        EditText searchEditText = (EditText) searchView.findViewById(R.id.fsv_search_text);
        // to show the search key on the keyboard and to not expand the EditText in landscape
        searchEditText.setImeOptions(EditorInfo.IME_ACTION_SEARCH | EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        // Collapse the SearchView and perform a search
        searchView.setOnSearchListener(searchStr -> {
            searchView.setActivated(false);
            Timber.d("Searching for %s", searchStr);
            doWeatherStringSearch(searchStr.toString());
            showProgressBar(true);
        });

        // For voice and location options
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
            if (!focused) {
                showProgressBar(false);
            }
        });

        // Set the animated arrow/search icon
        SearchArrowDrawable drawable = new SearchArrowDrawable(getContext());
        Drawable wrap = DrawableCompat.wrap(drawable);
        searchView.setIcon(wrap);
        searchView.setOnIconClickListener(() -> {
            searchView.setActivated(!searchView.isActivated());
        });
    }

    /**
     * Start a speech recognizer intent
     * to search by voice input
     */
    private void doVoiceSearch() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, VOICE_SEARCH_REQUEST_CODE);
    }

    /**
     * Get the result of the voice input and perform a search
     *
     * @param requestCode The code of the intent request
     * @param resultCode Result of the intent
     * @param data Data, in this case, the probable words recognized
     */
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

    /**
     * Prompt the user for permission or to enable Location services.
     * Then conduct a weather search by location once the requirements
     * are fulfilled.
     */
    private void doCoordinatesWeatherSearch() {
        if (ActivityCompat.checkSelfPermission(getActivity(), permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            return;
        }

        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean gpsProviderEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkProviderEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!gpsProviderEnabled && !networkProviderEnabled) {
            AlertDialog.Builder alertDialog = new Builder(getContext());
            alertDialog.setTitle("Location Services disabled");
            alertDialog.setMessage("In order to find the weather at your location, you will need to enable Location Services on your device");
            alertDialog.setPositiveButton("Enable", (dialog, which) -> {
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
            });
            alertDialog.setNegativeButton("Cancel", (dialog, which) -> {
                dialog.dismiss();
            });

            alertDialog.show();

            return;
        }

        weatherPresenter.doCoordinatesWeatherSearch();
    }

    /**
     * Handle permission grant or denial
     * @param requestCode The code of the permission request
     * @param permissions Permissions requested
     * @param grantResults Results of each permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // perform a coordinate weather search
                    weatherPresenter.doCoordinatesWeatherSearch();
                } else {
                    // show dialog that we can't use location without this permission
                    showPermissionDeniedDialog();
                }
                break;
        }
    }

    /**
     * Show dialog that explains the consequences of
     * denying the permission request.
     */
    private void showPermissionDeniedDialog() {
        AlertDialog.Builder alertDialog = new Builder(getContext());
        alertDialog.setTitle("Location permission");
        alertDialog.setMessage("This app needs the location permission to find the weather at your location. You will not have access to this feature without granting the permission");
        alertDialog.setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
        });

        alertDialog.show();
    }

    /**
     * Tell the presenter to do a string search
     *
     * @param searchStr Search str
     */
    private void doWeatherStringSearch(String searchStr) {
        weatherPresenter.doStringWeatherSeach(searchStr, countryCode);
    }

    /**
     * Load the Weather object into the view
     *
     * @param weather The object containing weather data
     * @param animate Whether this update should be animated or not. In case of configuration changes
     */
    @Override
    public void showWeather(Weather weather, boolean animate) {
        Timber.d("Showing weather for: " + weather.name);
        searchView.setText(weather.name);
        searchView.setActivated(false);

        cityView.setText(weather.name + ", " + weather.sys.country);
        weatherTextView.setText(weather.weather[0].description);

        tempView.setText(getString(R.string.temp, weather.main.temp));
        rainView.setValue(getString(R.string.rain, weather.rain.volume));
        cloudsView.setValue(getString(R.string.clouds, weather.clouds.all));
        windView.setValue(getString(R.string.wind, weather.wind.speed));
        pressureView.setValue(getString(R.string.pressure, weather.main.pressure));
        humidityView.setValue(getString(R.string.humidity, weather.main.humidity));

        // Load the correct weather drawable
        String iconName = "ic_" + weather.weather[0].icon;
        int drawableId = ResourceUtils.getDrawableIdByName(iconName);
        if (drawableId != 0) {
            weatherIconView.setImageDrawable(getResources().getDrawable(drawableId));
        }

        if (animate) {
            weatherLayout.setAlpha(0.5f);
            weatherLayout.setTranslationY(60);

            weatherLayout.animate()
                    .alpha(1.0f)
                    .translationY(0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            weatherLayout.setVisibility(View.VISIBLE);
                            emptyLayout.setVisibility(View.GONE);
                        }
                    });
        } else {
            weatherLayout.setVisibility(View.VISIBLE);
            emptyLayout.setVisibility(View.GONE);
        }
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
        searchView.setActivated(false);
        showProgressBar(false);
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void populateRecentSearches(List<Search> searches) {
        Timber.d("Populating recent searches");
        searchAdapter.setSearches(searches);
    }

    @Override
    public void showProgressBar(boolean show) {
        searchView.getMenu().findItem(R.id.menu_progress).setVisible(show);
    }
}
