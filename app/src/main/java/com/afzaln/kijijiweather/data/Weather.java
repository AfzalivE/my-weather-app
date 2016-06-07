package com.afzaln.kijijiweather.data;

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
    public long dt;
    public Sys sys;
    public long id;
    public String name;
    public long cod;

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
        public long pressure;
        public long humidity;
        public double temp_min;
        public double temp_max;
    }

    public static final class Wind {
        public double speed;
        public long deg;
    }

    public static final class Clouds {
        public long all;
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