package com.example.pradh.demoapp;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.amazonaws.amplify.generated.graphql.GetRecipeQuery;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class displayRecipeActivity extends AppCompatActivity {

    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAdapter tabsAdapter;
    private String mRecipeId;
    private GetRecipeQuery.Data mRecipe;
    private List<String> Ingredients;
    private List<String> Steps;
    private final String TAG = "displayRecipeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_recipe);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        if(extras!=null){mRecipeId = extras.getString("recipePos");}

        Log.i(TAG, "Received recipe at displayActivity"+mRecipeId);
        getRecipe(mRecipeId);

        myViewPager = findViewById(R.id.viewpager);
        tabsAdapter = new TabsAdapter(getSupportFragmentManager());


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

    public  void getRecipe(String recipeId) {
        ClientFactory.appSyncClient().query(GetRecipeQuery.builder().id(recipeId).build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST)
                .enqueue(queryCallback);
    }

    public GraphQLCall.Callback<GetRecipeQuery.Data> queryCallback = new GraphQLCall.Callback<GetRecipeQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<GetRecipeQuery.Data> response) {
            mRecipe = response.data();
            Log.i(TAG, "Display Retrieved Recipe Name: " +mRecipe.getRecipe().recipeName());
            Log.i(TAG, "Display Retrieved Recipe Name: " +mRecipe.getRecipe().description());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mRecipe.getRecipe().steps()!=null)tabsAdapter.setSteps(new ArrayList(mRecipe.getRecipe().steps()));
                    if(mRecipe.getRecipe().ingredients()!=null)tabsAdapter.setIngredients(new ArrayList(mRecipe.getRecipe().ingredients()));
                    myViewPager.setAdapter(tabsAdapter);
                }
            });
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, e.toString());
        }
    };

}
