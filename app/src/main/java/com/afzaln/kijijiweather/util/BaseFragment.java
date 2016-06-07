package com.afzaln.kijijiweather.util;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import butterknife.ButterKnife;

/**
 * Created by afzal on 2016-06-04.
 */
public class BaseFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int layout = getArguments().getInt("layout");
        View view = inflater.inflate(layout, container, false);

        ButterKnife.bind(this, view);

        if (ViewConfiguration.get(getActivity()).hasPermanentMenuKey()) {
            setHasOptionsMenu(true);
        }

        return view;
    }

    public void setLayout(@LayoutRes int layout) {
        Bundle args = new Bundle();
        args.putInt("layout", layout);
        setArguments(args);
    }
}
