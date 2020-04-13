package com.example.pradh.demoapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.ListRecipesQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pradh on 2/15/2020.
 */

public class StaggeredRecyclerViewAdapter extends RecyclerView.Adapter<StaggeredRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "StaggeredRecyclerViewAd";
    private List<ListRecipesQuery.Item> mData = new ArrayList<>();
    private LayoutInflater mInflater;
    private Context mContext;

    public StaggeredRecyclerViewAdapter( Context mContext) {
        this.mInflater = LayoutInflater.from(mContext);
        this.mContext = mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_stag, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");

        holder.bindData(mData.get(position));

        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, mData.get(position).recipeName(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    // resets the list with a new set of data
    // allow outside resetting of our data set.
    public void setItems(List<ListRecipesQuery.Item> items) {
        mData = items;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView description;


        public ViewHolder(View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.txt_name);
            this.description = itemView.findViewById(R.id.txt_description);
        }

        void bindData(ListRecipesQuery.Item item) {
            name.setText(item.recipeName());
            description.setText(item.description());
        }
    }
}
