package com.example.pradh.demoapp;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class displayRecipeActivity extends AppCompatActivity {

    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAdapter tabsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_recipe);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        myViewPager = findViewById(R.id.viewpager);
        tabsAdapter = new TabsAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(tabsAdapter);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        myTabLayout = (TabLayout) findViewById(R.id.tabs);
        myTabLayout.setupWithViewPager(myViewPager);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

}
