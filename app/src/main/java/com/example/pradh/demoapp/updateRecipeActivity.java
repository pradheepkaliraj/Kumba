package com.example.pradh.demoapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.GetRecipeQuery;
import com.amazonaws.amplify.generated.graphql.UpdateRecipeMutation;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.List;

import javax.annotation.Nonnull;

import type.UpdateRecipeInput;

public class updateRecipeActivity extends AppCompatActivity {

    private  String currentRecipeId;
    private GetRecipeQuery.Data mRecipe;
    private List<String> Steps;
    private List<String> Ingredients;
    private static final String TAG = "updateRecipeActivity";
    TextView recipeName;
    TextView recipeDesc;
    ListView recipeIngredients;
    ListView recipeSteps;
    ArrayAdapter<String> stepsAdapter;
    ArrayAdapter<String> ingredientsAdapter;



    //String[] ingredients =  new String[] {"salt", "sugar", "spice",  "and everything nice", "also chemicalx"};
    //String[] steps =  new String[] {"get pot", "add water", "add ingred",  "and heat", "watch it burn"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_recipe);

        recipeName = findViewById(R.id.upd_txt_name);
        recipeDesc = findViewById(R.id.upd_txt_description);
        recipeIngredients = findViewById(R.id.upd_ingredients_list);
        recipeSteps = findViewById(R.id.upd_steps_list);

        /**setting adapters for the list views*/
        //ingredientsAdapter = new ArrayAdapter<String>(this, R.layout.activity_update_recipe, Ingredients);
        //stepsAdapter = new ArrayAdapter<String>(this, R.layout.activity_update_recipe, Steps);
        //recipeIngredients.setAdapter(ingredientsAdapter);
        //recipeSteps.setAdapter(stepsAdapter);

        Button btnCancel = findViewById(R.id.btn_upd_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRecipeActivity.this.finish();
            }
        });

        Button btnSave = findViewById(R.id.btn_upd_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRecipe();
            }
        });

        Bundle extras = getIntent().getExtras();
        if(extras!=null){currentRecipeId = extras.getString("add_key");}
        //Get the recipe to be updated
        getRecipe(currentRecipeId);



    }
    private void getRecipe(String recipeId) {
        ClientFactory.appSyncClient().query(GetRecipeQuery.builder().id(recipeId).build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST)
                .enqueue(queryCallback);
    }

    private GraphQLCall.Callback<GetRecipeQuery.Data> queryCallback = new GraphQLCall.Callback<GetRecipeQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<GetRecipeQuery.Data> response) {
            mRecipe = response.data();
            Log.i(TAG, "Retrieved Recipe Name: " +mRecipe.getRecipe().recipeName());
            Log.i(TAG, "Retrieved Recipe Name: " +mRecipe.getRecipe().description());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Steps = mRecipe.getRecipe().steps();
                    Ingredients = mRecipe.getRecipe().ingredients();
                    //ingredientsAdapter.addAll(Ingredients);
                    //ingredientsAdapter.notifyDataSetChanged();
                    //stepsAdapter.addAll(Steps);
                    //ingredientsAdapter.notifyDataSetChanged();
                    recipeName.setText(mRecipe.getRecipe().recipeName());
                    recipeDesc.setText(mRecipe.getRecipe().description());
                    Toast.makeText(updateRecipeActivity.this, "GOT   :"+mRecipe.getRecipe().recipeName(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, e.toString());
        }
    };

     /*   GetRecipeQuery input = builder().id(currentRecipeId).build(); }*/


    private void updateRecipe() {
        final String recipeName = ((EditText) findViewById(R.id.upd_txt_name)).getText().toString();
        final String Description = ((EditText) findViewById(R.id.upd_txt_description)).getText().toString();
        //final List<String> Steps = (List<String>) findViewById(R.id.upd_steps_list);
        //final List<String> Ingredients = ((List<String>) findViewById(R.id.upd_ingredients_list));

        UpdateRecipeInput input = UpdateRecipeInput.builder()
                .id(currentRecipeId)
                .recipeName(recipeName)
                .description(Description).build();
        UpdateRecipeMutation updateRecipeMutation = UpdateRecipeMutation.builder().input(input).build();
        ClientFactory.appSyncClient().mutate(updateRecipeMutation).enqueue(updateCallback);
    }

    private GraphQLCall.Callback<UpdateRecipeMutation.Data> updateCallback = new GraphQLCall.Callback<UpdateRecipeMutation.Data>() {

        @Override
        public void onResponse(@Nonnull Response<UpdateRecipeMutation.Data> response) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(updateRecipeActivity.this, "Updated Recipe", Toast.LENGTH_SHORT).show();
                    updateRecipeActivity.this.finish();
                }
            });
        }

        @Override
        public void onFailure(@Nonnull final ApolloException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("", "Failed to update Recipe", e);
                    Toast.makeText(updateRecipeActivity.this, "Failed to update Recipe", Toast.LENGTH_SHORT).show();
                    updateRecipeActivity.this.finish();
                }
            });
        }
    };
}

