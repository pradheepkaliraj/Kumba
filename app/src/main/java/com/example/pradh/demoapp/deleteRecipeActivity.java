package com.example.pradh.demoapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.DeleteRecipeMutation;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import javax.annotation.Nonnull;

import type.DeleteRecipeInput;

public class deleteRecipeActivity extends AppCompatActivity {

    String recipeInfo = "pk_null";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if(extras!=null){recipeInfo = extras.getString("main_key");}
        Toast.makeText(deleteRecipeActivity.this, "deleteRecipeActivity        :"+recipeInfo,
                Toast.LENGTH_LONG).show();
        //finish();
        findRecipe();
        deleteRecipe();
    }

    private void findRecipe() {

    }

    private void deleteRecipe() {
        DeleteRecipeInput  input = DeleteRecipeInput.builder()
                .id(recipeInfo)
                .build();
        DeleteRecipeMutation deleteRecipeMutation  = DeleteRecipeMutation.builder().input(input).build();
        ClientFactory.appSyncClient().mutate(deleteRecipeMutation).enqueue(mutateCallback);
        //deleteRecipeOffline(input);
    }

    // Mutation callback code
    private GraphQLCall.Callback<DeleteRecipeMutation.Data> mutateCallback = new GraphQLCall.Callback<DeleteRecipeMutation.Data>() {
        @Override
        public void onResponse(@Nonnull final Response<DeleteRecipeMutation.Data> response) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(deleteRecipeActivity.this, "Deleted Recipe", Toast.LENGTH_SHORT).show();
                    deleteRecipeActivity.this.finish();
                }
            });
        }

        @Override
        public void onFailure(@Nonnull final ApolloException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("", "Failed to perform delete Recipe", e);
                    Toast.makeText(deleteRecipeActivity.this, "Failed to delete pet", Toast.LENGTH_SHORT).show();
                    deleteRecipeActivity.this.finish();
                }
            });
        }
    };
    /**private void deleteRecipeOffline(DeleteRecipeInput input) {
        final DeleteRecipeMutation.DeleteRecipe expected =
                new DeleteRecipeMutation.DeleteRecipe("Recipe",
                        input.id(),
                        input.recipename)

    }*/


}
