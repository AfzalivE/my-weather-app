package com.afzaln.kijijiweather.weather;

import com.afzaln.kijijiweather.data.Search;

/**
 * Created by afzal on 2016-06-06.
 */
public interface SearchItemClickListener {
    void delete(Search search);
    void search(Search search);
}
