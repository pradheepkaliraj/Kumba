package com.example.pradh.demoapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.CreateRecipeMutation;
import com.amazonaws.amplify.generated.graphql.ListRecipesQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import type.CreateRecipeInput;

public class addRecipeActivity extends AppCompatActivity {

    private String TAG = "addRecipeActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        Button btnAddItem = findViewById(R.id.btn_save);
        btnAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });

        Button btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRecipeActivity.this.finish();
            }
        });
    }

    Date date = new Date(System.currentTimeMillis());

    private void save() {
        final String name = ((EditText) findViewById(R.id.editTxt_name)).getText().toString();
        final String description = ((EditText) findViewById(R.id.editText_description)).getText().toString();

        CreateRecipeInput input = CreateRecipeInput.builder()
                .recipeName(name)
                .description(description)
                .photo("null")
                .owner(AWSMobileClient.getInstance().getUsername())
                .build();
        CreateRecipeMutation addRecipeMutation = CreateRecipeMutation.builder().input(input).build();

        ClientFactory.appSyncClient().mutate(addRecipeMutation).enqueue(mutateCallback);

        // Enables offline support via an optimistic update
        // Add to event list while offline or before request returns
        addRecipeOffline(input);
    }

    private void addRecipeOffline(CreateRecipeInput input) {
        final CreateRecipeMutation.CreateRecipe expected =
                new CreateRecipeMutation.CreateRecipe(
                        "Recipe",
                        UUID.randomUUID().toString(), //Universally Unique IDentifier (UUID)
                        input.recipeName(),
                        input.description(),
                        input.photo(),
                        input.owner());

        final AWSAppSyncClient awsAppSyncClient = ClientFactory.appSyncClient();
        final ListRecipesQuery listEventsQuery = ListRecipesQuery.builder().build();

        /**
         * Signals the appsync client to <b>only</b> fetch the data from the normalized cache. If it's not present in
         * the normalized cache or if an exception occurs while trying to fetch it from the normalized cache, an empty {@link
         * com.apollographql.apollo.api.Response} is sent back with the {@link com.apollographql.apollo.api.Operation} info
         * wrapped inside.
         */
            awsAppSyncClient.query(listEventsQuery)
                    .responseFetcher(AppSyncResponseFetchers.CACHE_ONLY)
                    .enqueue(new GraphQLCall.Callback<ListRecipesQuery.Data>() {
                        @Override
                        public void onResponse(@Nonnull Response<ListRecipesQuery.Data> response) {
                            List<ListRecipesQuery.Item> items = new ArrayList<>();
                            if (response.data() != null) { //adding all existing recipes in cache
                                items.addAll(response.data().listRecipes().items());
                            }
                            items.add(new ListRecipesQuery.Item(expected.__typename(), //adding expected to the item
                                    expected.id(),
                                    expected.recipeName(),
                                    expected.description(),
                                    expected.owner(),
                                    expected.photo()));
                            ListRecipesQuery.Data data = new ListRecipesQuery.Data(new ListRecipesQuery.ListRecipes("ModelRecipeconnection", items, null));
                            awsAppSyncClient.getStore().write(listEventsQuery, data).enqueue(null);
                            Log.d(TAG, "successfullywritten data while offline");
                            finishIfOffline();
                        }


                        @Override
                        public void onFailure(@Nonnull ApolloException e) {
                            Log.e(TAG, "Failed to update event query list.", e);
                        }
                    });

    }

    // Close the add activity when offline otherwise allow callback to close
    private void finishIfOffline() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = activeNetwork !=null &&
                activeNetwork.isConnectedOrConnecting();

        if(!isConnected){
            Log.d(TAG, "OFFLINE, returning to Main Activity");
            finish();
        }
    }

    //closes activity after run is executed
    private GraphQLCall.Callback<CreateRecipeMutation.Data> mutateCallback = new GraphQLCall.Callback<CreateRecipeMutation.Data>(){

        @Override
        public void onResponse(@Nonnull Response<CreateRecipeMutation.Data> response) {
           runOnUiThread(new Runnable() {
               @Override
               public void run() {
                   Toast.makeText(addRecipeActivity.this, "Added Recipe", Toast.LENGTH_SHORT).show();
                   addRecipeActivity.this.finish();
               }
           });
        }

        @Override
        public void onFailure(@Nonnull final ApolloException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("", "Failed to perform AddRecipeMutation", e);
                    Toast.makeText(addRecipeActivity.this, "Failed to add Recipe", Toast.LENGTH_SHORT).show();
                    addRecipeActivity.this.finish();
                }
            });
        }
    };
}
