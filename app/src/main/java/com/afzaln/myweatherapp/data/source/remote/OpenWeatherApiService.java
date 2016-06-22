package com.afzaln.myweatherapp.data.source.remote;


import com.afzaln.myweatherapp.data.Weather;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by afzal on 2016-06-04.
 */
public interface OpenWeatherApiService {
    @GET("weather")
    Observable<Response<Weather>> getWeatherByCityName(@Query("q") String cityName);

    @GET("weather")
    Observable<Response<Weather>> getWeatherByCoordinates(@Query("lat") double lat, @Query("lon") double lon);

    @GET("weather")
    Observable<Response<Weather>> getWeatherByZipCode(@Query("zip") String zipCode);
}
