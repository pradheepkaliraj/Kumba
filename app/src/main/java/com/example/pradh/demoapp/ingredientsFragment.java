package com.example.pradh.demoapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/**
 * Created by pradh on 2/24/2020.
 */

public class ingredientsFragment extends android.support.v4.app.ListFragment {
    String[] ingredients = new String[] { "Nepal","Bhutan" };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.list_ingredients,container,false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),  android.R.layout.simple_list_item_1, ingredients);
        setListAdapter(adapter);
    }
}
