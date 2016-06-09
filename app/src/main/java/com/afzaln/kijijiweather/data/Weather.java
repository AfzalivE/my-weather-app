package com.afzaln.kijijiweather.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by afzal on 2016-06-04.
 */
public final class Weather {
    public Coord coord;
    public WeatherData weather[];
    public String base;
    public Main main;
    public Wind wind;
    public Clouds clouds;
    public RainSnow rain;
    public RainSnow snow;
    public long dt;
    public Sys sys;
    public long id;
    public String name;
    public long cod;
    public String message; // for server error messages

    public static final class Coord {
        public double lon;
        public double lat;
    }

    public static final class WeatherData {
        public long id;
        public String main;
        public String description;
        public String icon;
    }

    public static final class Main {
        public double temp;
        public double pressure;
        public long humidity;
        public double temp_min;
        public double temp_max;
    }

    public static final class Wind {
        public double speed;
        public double deg;
    }

    public static final class Clouds {
        public long all;
    }

    public static final class RainSnow {
        @SerializedName("3h")
        public double volume;
    }

    public static final class Sys {
        public long type;
        public long id;
        public double message;
        public String country;
        public long sunrise;
        public long sunset;
    }
}