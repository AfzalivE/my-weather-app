package com.afzaln.kijijiweather.weather;

import android.content.Context;
import android.widget.ProgressBar;

import com.mypopsy.widget.internal.ViewUtils;

public class MenuProgressBar extends ProgressBar {

    public MenuProgressBar(Context context) {
        super(context);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(ViewUtils.dpToPx(48), ViewUtils.dpToPx(24));
    }
}