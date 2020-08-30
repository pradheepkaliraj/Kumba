package com.example.pradh.demoapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.GetRecipeQuery;
import com.amazonaws.amplify.generated.graphql.UpdateRecipeMutation;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.ArrayList;

import javax.annotation.Nonnull;

import type.UpdateRecipeInput;

public class updateRecipeActivity extends AppCompatActivity {

    private  String currentRecipeId;
    public GetRecipeQuery.Data mRecipe;
    private ArrayList Steps;
    private ArrayList Ingredients = new ArrayList<String>();
    private static final String TAG = "updateRecipeActivity";
    private TextView recipeName;
    private TextView recipeDesc;
    private ListView recipeIngredients;
    private ArrayAdapter<String> ingredientsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_recipe);
        recipeName = findViewById(R.id.upd_txt_name);
        recipeDesc = findViewById(R.id.upd_txt_description);
        recipeIngredients = findViewById(R.id.upd_ingredients_list);

        /**setting adapters for the list views*/
        ingredientsAdapter = new ArrayAdapter(this, R.layout.update_ingredients_listview, R.id.upd_ingredients_list_item, Ingredients);
        recipeIngredients.setAdapter(ingredientsAdapter);

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

        ImageButton btnAdd = findViewById(R.id.addImageButton);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            EditText ingrAdd = findViewById(R.id.new_list_Ingredient);
            @Override
            public void onClick(View v) {
                Ingredients.add(ingrAdd.getText().toString()); //add text from edit text to list
                ingredientsAdapter.clear(); //clear existing list
                ingredientsAdapter.addAll(Ingredients); //add new list to adapter
                ingredientsAdapter.notifyDataSetChanged();
                ingrAdd.getText().clear();
            }
        });

        Bundle extras = getIntent().getExtras();
        if(extras!=null){currentRecipeId = extras.getString("add_key");}
        //Get the recipe to be updated
        getRecipe(currentRecipeId);

    } //end of onCreate

    public  void getRecipe(String recipeId) {
        ClientFactory.appSyncClient().query(GetRecipeQuery.builder().id(recipeId).build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST)
                .enqueue(queryCallback);
    }

    public GraphQLCall.Callback<GetRecipeQuery.Data> queryCallback = new GraphQLCall.Callback<GetRecipeQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<GetRecipeQuery.Data> response) {
            mRecipe = response.data();
            Log.i(TAG, "Retrieved Recipe Name: " +mRecipe.getRecipe().recipeName());
            Log.i(TAG, "Retrieved Recipe Name: " +mRecipe.getRecipe().description());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Steps = new ArrayList(mRecipe.getRecipe().steps());
                    Ingredients = new ArrayList(mRecipe.getRecipe().ingredients());
                    ingredientsAdapter.addAll(Ingredients);
                    recipeName.setText(mRecipe.getRecipe().recipeName());
                    recipeDesc.setText(mRecipe.getRecipe().description());
                }
            });
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, e.toString());
        }
    };


    private void updateRecipe() {
        final String recipeName = ((EditText) findViewById(R.id.upd_txt_name)).getText().toString();
        final String Description = ((EditText) findViewById(R.id.upd_txt_description)).getText().toString();
        final ArrayList updIngredients = new ArrayList();
        int ingrSize = ingredientsAdapter.getCount();
        for (int i=0; i<ingrSize;i++){
            View view= recipeIngredients.getChildAt(i);
            EditText editText=view.findViewById(R.id.upd_ingredients_list_item);
            updIngredients.add(i,editText.getText().toString());}
        Toast.makeText(updateRecipeActivity.this, "Final   :"+updIngredients.size(),
                Toast.LENGTH_SHORT).show();
        UpdateRecipeInput input = UpdateRecipeInput.builder()
                .id(currentRecipeId)
                .recipeName(recipeName)
                .description(Description)
                .ingredients(updIngredients).build();
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

