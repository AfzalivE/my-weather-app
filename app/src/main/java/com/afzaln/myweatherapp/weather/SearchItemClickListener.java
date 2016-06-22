package com.afzaln.myweatherapp.weather;

import com.afzaln.myweatherapp.data.Search;

/**
 * Interface for delete and search item clicks
 */
public interface SearchItemClickListener {
    void delete(Search search);
    void search(Search search);
}
