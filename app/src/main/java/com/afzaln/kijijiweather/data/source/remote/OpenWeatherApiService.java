package com.afzaln.kijijiweather.data.source.remote;


import com.afzaln.kijijiweather.data.Weather;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by afzal on 2016-06-04.
 */
public interface OpenWeatherApiService {
    @GET("weather")
    Observable<Weather> getWeather(@Query("q") String cityName);
}
