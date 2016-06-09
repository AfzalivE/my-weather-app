package com.afzaln.kijijiweather.data.source.remote;

import java.util.List;

import com.afzaln.kijijiweather.data.Search;
import com.afzaln.kijijiweather.data.Weather;
import com.afzaln.kijijiweather.data.Weather.Main;
import com.afzaln.kijijiweather.data.Weather.RainSnow;
import com.afzaln.kijijiweather.data.source.WeatherDataSource;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Observable.Transformer;

/**
 * Created by afzal on 2016-06-04.
 */
public class WeatherRemoteDataSource implements WeatherDataSource {
    private static String OPENWEATHER_BASE_URL = "http://api.openweathermap.org/data/2.5/";
    private static String OPENWEATHER_API_KEY = "95d190a434083879a6398aafd54d9e73";

    private static WeatherRemoteDataSource INSTANCE;
    private final Interceptor openWeatherApiKeyInterceptor = chain -> {
        Request request = chain.request();
        HttpUrl url = request.url().newBuilder().addQueryParameter("APPID", OPENWEATHER_API_KEY).build();
        request = request.newBuilder().url(url).build();
        return chain.proceed(request);
    };

    private final OpenWeatherApiService weatherApiService;

    public static WeatherRemoteDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new WeatherRemoteDataSource();
        }
        return INSTANCE;
    }

    private WeatherRemoteDataSource() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(Level.BODY);

        Retrofit weatherRetrofit = buildRetrofit(OPENWEATHER_BASE_URL, loggingInterceptor, openWeatherApiKeyInterceptor);
        weatherApiService = weatherRetrofit.create(OpenWeatherApiService.class);
    }

    private Retrofit buildRetrofit(String baseUrl, Interceptor loggingInterceptor, Interceptor apiKeyInterceptor) {
        OkHttpClient okhttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(apiKeyInterceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okhttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        return retrofit;
    }

    @Override
    public Observable<Weather> getWeather(Search search) {
        Observable<Response<Weather>> responseObservable;

        if (search.getSearchType() == Search.SEARCH_TYPE_COORDINATES) {
            responseObservable = weatherApiService.getWeatherByCoordinates(search.getLat(), search.getLon());
        } else if (search.getSearchType() == Search.SEARCH_TYPE_ZIPCODE) {
            responseObservable= weatherApiService.getWeatherByZipCode(search.getZipCode());
        } else {
            responseObservable = weatherApiService.getWeatherByCityName(search.getSearchStr());
        }

        return responseObservable
                .map(response -> {
                    Weather weather = response.body();
                    if (weather.coord == null) {
                        if (weather.message.contains("Not found city")) {
                            weather.message = "City not found, please try again.";
                        }
                        throw new NullPointerException(weather.message);
                    }
                    return weather;
                })
                .compose(formatWeather());

    }

    public Transformer<Weather, Weather> formatWeather() {
        return weather -> weather.map(weather1 -> {
            if (weather1.rain == null) {
                weather1.rain = new RainSnow();
            }
            if (weather1.snow == null) {
                weather1.snow = new RainSnow();
            }
            if (weather1.main == null) {
                weather1.main = new Main();
            }

            // kelvin conversion and round to nearest integer
            weather1.main.temp = Math.round(weather1.main.temp - 273);

            return weather1;
        });
    }

    // We don't fetch recent searches from remote so don't do anything here
    @Override
    public void saveRecentSearch(Search search) {}

    @Override
    public Observable<? extends List<Search>> getRecentSearches() {return null;}

    @Override
    public void deleteRecentSearch(long timestamp) {}

    @Override
    public void deleteAllRecentSearches() {

    }
}
