package com.afzaln.kijijiweather.util;

import android.content.Context;
import android.support.v4.content.Loader;
import android.util.Log;

/**
 * Created by afzal on 2016-06-09.
 */
public class PresenterLoader<P extends BasePresenter> extends Loader<P> {
    private final PresenterFactory<P> factory;
    private P presenter;
    private final String tag;

    /**
     * Stores away the application context associated with context.
     * Since Loaders can be used across multiple activities it's dangerous to
     * store the context directly; always use {@link #getContext()} to retrieve
     * the Loader's Context, don't use the constructor argument directly.
     * The Context returned by {@link #getContext} is safe to use across
     * Activity instances.
     *
     * @param context used to retrieve the application context.
     * @param factory
     */
    public PresenterLoader(Context context, PresenterFactory<P> factory, String tag) {
        super(context);
        this.factory = factory;
        this.tag = tag;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        // if we already own a presenter instance, simply deliver it.
        if (presenter != null) {
            deliverResult(presenter);
            return;
        }

        // Otherwise, force a load
        forceLoad();
    }

    @Override
    protected void onForceLoad() {
        // Create the Presenter using the Factory
        presenter = factory.create(getContext());

        // Deliver the result
        deliverResult(presenter);
    }

    @Override
    public void deliverResult(P presenter) {
        super.deliverResult(presenter);
        Log.i("loader", "deliverResult-" + tag);
    }

    @Override
    protected void onStopLoading() {
        Log.i("loader", "onStopLoading-" + tag);
    }

    @Override
    protected void onReset() {
        Log.i("loader", "onReset-" + tag);
        if (presenter != null) {
            presenter.onDestroyed();
            presenter = null;
        }
    }
}
