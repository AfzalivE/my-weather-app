package com.afzaln.kijijiweather.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.afzaln.kijijiweather.R;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by afzal on 2016-06-08.
 */
public class WeatherInfoView extends LinearLayout {
    public WeatherInfoView(Context context) {
        super(context);
    }

    @BindView(R.id.info_icon)
    ImageView iconView;

    @BindView(R.id.info_label)
    TextView labelView;

    @BindView(R.id.info_value)
    TextView valueView;

    public WeatherInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WeatherInfoView, 0, 0);
        Drawable icon = a.getDrawable(R.styleable.WeatherInfoView_wiv_icon);
        String label = a.getString(R.styleable.WeatherInfoView_wiv_labelText);
        String value = a.getString(R.styleable.WeatherInfoView_wiv_value);

        a.recycle();

        inflate(context, R.layout.info_layout, this);
        ButterKnife.bind(this);

        iconView.setImageDrawable(icon);
        labelView.setText(label);
        valueView.setText(value);
    }

    public void setValue(@NonNull String value) {
        checkNotNull(value);
        valueView.setText(value);
    }

    public void setLabel(@NonNull String label) {
        checkNotNull(label);
        labelView.setText(label);
    }

    public void setIcon(@NonNull Drawable icon) {
        checkNotNull(icon);
        iconView.setImageDrawable(icon);
    }
}
