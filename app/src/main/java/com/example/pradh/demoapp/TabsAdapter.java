package com.example.pradh.demoapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by pradh on 2/24/2020.
 */

public class TabsAdapter extends FragmentPagerAdapter {

    private final String[] tabs  = {"Ingredients","Steps"};

    public TabsAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new ingredientsFragment();
            case 1:
                return new stepsFragment();
        }return null;
    }

    @Override
    public int getCount() {
        return 2; //there will be 2 tabs
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabs[position];
    }
}
