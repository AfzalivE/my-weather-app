package com.afzaln.kijijiweather.weather;

import java.util.ArrayList;
import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.afzaln.kijijiweather.R;
import com.afzaln.kijijiweather.data.Search;
import com.afzaln.kijijiweather.weather.SearchAdapter.SearchVH;

/**
 * Created by afzal on 2016-06-06.
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchVH> {

    private final SearchItemClickListener listener;
    List<Search> searches = new ArrayList<>();

    SearchAdapter(SearchItemClickListener listener) {
        this.listener = listener;
        setHasStableIds(true);
    }

    @Override
    public SearchVH onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.recent_search_item, parent, false);

        SearchVH searchVH = new SearchVH(view, listener);

        return searchVH;
    }

    @Override
    public void onBindViewHolder(SearchVH holder, int position) {
        holder.bind(searches.get(position));
    }

    @Override
    public int getItemCount() {
        return searches.size();
    }

    public void setSearches(List<Search> searches) {
        this.searches = searches;
        notifyDataSetChanged();
    }

    public static class SearchVH extends RecyclerView.ViewHolder {

        Search search;
        @BindView(R.id.text)
        TextView searchTextView;
        @BindView(R.id.icon_end)
        ImageView deleteButtonView;

        private final SearchItemClickListener listener;
        OnClickListener searchClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.search(search);
            }
        };

        OnClickListener deleteClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.delete(search);
            }
        };


        public SearchVH(View itemView, SearchItemClickListener listener) {
            super(itemView);
            this.listener = listener;
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(searchClickListener);
            deleteButtonView.setOnClickListener(deleteClickListener);
        }

        public void bind(Search search) {
            this.search = search;
            searchTextView.setText(search.getSearchStr());
        }
    }
}
