package com.afzaln.kijijiweather.data.source.location;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

/**
 * Created by afzal on 2016-06-09.
 */
public class LocationProvider implements ConnectionCallbacks, OnConnectionFailedListener {

    private static LocationProvider INSTANCE;
    private final GoogleApiClient googleApiClient;

    private Subscriber<? super Location> subscriber;

    public static LocationProvider getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new LocationProvider(context);
        }

        return INSTANCE;
    }

    private LocationProvider(Context context) {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public Observable<Location> getLastLocation() {
        return Observable.<Location>create(subscriber -> {
            Timber.d("Observable creation: " + Thread.currentThread().getName());
            this.subscriber = subscriber;
            if (!googleApiClient.isConnected()) {
                googleApiClient.connect();
            } else {
                getLocation();
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getLocation();
    }

    private void getLocation() {
        // there is a permission check in WeatherFragment for this
        if (ContextCompat.checkSelfPermission(googleApiClient.getContext(), permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Timber.e("Location permission was not granted");
            return;
        }
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (null != currentLocation) {
            if (subscriber != null && !subscriber.isUnsubscribed()) {
                Timber.d("Observable onNext: %s", Thread.currentThread().getName());
                subscriber.onNext(currentLocation);
            }
        } else {
            LocationCallback locationListener = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (subscriber != null && !subscriber.isUnsubscribed()) {
                        Timber.d("Observable onNext: %s", Thread.currentThread().getName());
                        subscriber.onNext(locationResult.getLastLocation());
                    }
                    LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
                }
            };

            PendingResult<Status> statusPendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, LocationRequest.create(), locationListener, Looper.myLooper());
            statusPendingResult.setResultCallback(status -> Timber.d(String.valueOf(status.isSuccess())));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        subscriber = null;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        subscriber = null;
    }
}
