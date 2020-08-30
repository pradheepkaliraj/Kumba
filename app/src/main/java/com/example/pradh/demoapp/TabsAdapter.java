package com.example.pradh.demoapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Created by pradh on 2/24/2020.
 */

public class TabsAdapter extends FragmentPagerAdapter {

    private final String[] tabs  = {"Ingredients","Steps"};
    private ArrayList<String> Ingredients;
    private ArrayList<String> Steps;

    public void setIngredients(ArrayList<String> ingredients) {
        Ingredients = ingredients;
    }

    public void setSteps(ArrayList<String> steps) {
        Steps = steps;
    }

    public TabsAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: {
                Bundle IngredientsBundle = new Bundle();
                IngredientsBundle.putStringArrayList("INGR",  Ingredients);
                ingredientsFragment ingrFrag = new ingredientsFragment();
                ingrFrag.setArguments(IngredientsBundle);
                return ingrFrag;
            }
            case 1: {
                Bundle stepsBundle = new Bundle();
                stepsFragment stepsFrag = new stepsFragment();
                stepsBundle.putStringArrayList("STEP",  Steps);
                stepsFrag.setArguments(stepsBundle);
                return stepsFrag;
            }
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
