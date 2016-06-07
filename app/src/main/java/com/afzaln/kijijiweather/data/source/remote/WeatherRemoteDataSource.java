package com.afzaln.kijijiweather.data.source.remote;

import java.util.List;

import com.afzaln.kijijiweather.data.Search;
import com.afzaln.kijijiweather.data.Weather;
import com.afzaln.kijijiweather.data.source.WeatherDataSource;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

/**
 * Created by afzal on 2016-06-04.
 */
public class WeatherRemoteDataSource implements WeatherDataSource {
    private static String BASE_URL = "http://api.openweathermap.org/data/2.5/";
    private static String API_KEY = "95d190a434083879a6398aafd54d9e73";
    private static WeatherRemoteDataSource INSTANCE;
    private final Interceptor mApiKeyInterceptor = chain -> {
        Request request = chain.request();
        HttpUrl url = request.url().newBuilder().addQueryParameter("APPID", API_KEY).build();
        request = request.newBuilder().url(url).build();
        return chain.proceed(request);
    };

    private final OpenWeatherApiService mWeatherApiService;

    public static WeatherRemoteDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new WeatherRemoteDataSource();
        }
        return INSTANCE;
    }

    private WeatherRemoteDataSource() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(Level.NONE);

        OkHttpClient okhttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(mApiKeyInterceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okhttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        mWeatherApiService = retrofit.create(OpenWeatherApiService.class);
    }

    @Override
    public Observable<Weather> getWeather(String cityName) {
        return mWeatherApiService.getWeather(cityName);
    }

    // We don't fetch recent searches from remote so don't do anything here
    @Override
    public void saveRecentSearch(String search) {}

    @Override
    public Observable<? extends List<Search>> getRecentSearches() {return null;}

    @Override
    public void deleteRecentSearch(long timestamp) {}

    @Override
    public void deleteAllRecentSearches() {

    }
}
