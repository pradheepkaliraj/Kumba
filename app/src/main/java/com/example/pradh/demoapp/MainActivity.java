package com.example.pradh.demoapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.ListRecipesQuery;
import com.amazonaws.amplify.generated.graphql.OnCreateRecipeSubscription;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.UserStateListener;
import com.amazonaws.mobileconnectors.appsync.AppSyncSubscriptionCall;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.ArrayList;

import javax.annotation.Nonnull;

public class MainActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    StaggeredRecyclerViewAdapter mAdapter;
    StaggeredGridLayoutManager mManager;


    private ArrayList<ListRecipesQuery.Item> mRecipe;
    private final String TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.recycler_view);
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar); //Sets Text and 3 dot button

        mManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        if(mRecyclerView!=null) {
            if (mManager != null) mRecyclerView.setLayoutManager(mManager);
            else Log.e(TAG, "mManager is null");

            mAdapter = new StaggeredRecyclerViewAdapter(this);
            if (mAdapter != null) mRecyclerView.setAdapter(mAdapter);
            else Log.e(TAG, "mAdapter is null");
        }   else Log.e(TAG, "mRecyclerView is null");




        ClientFactory.init(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.btn_addRecipe);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addRecipeIntent = new Intent(MainActivity.this, addRecipeActivity.class);
                MainActivity.this.startActivity(addRecipeIntent);
            }
        });


        /**listener function to fix expired token issue*/
        AWSMobileClient.getInstance().addUserStateListener(new UserStateListener() {
            @Override
            public void onUserStateChanged(UserStateDetails userStateDetails) {
                switch (userStateDetails.getUserState()){
                    case SIGNED_OUT:
                        // user clicked signout button and signedout
                        Log.i("userState", "user signed out");
                        Intent intent_user_so = new Intent(getApplicationContext(), AuthenticationActivity.class);
                        startActivity(intent_user_so);
                        break;
                    case SIGNED_OUT_USER_POOLS_TOKENS_INVALID:
                        AWSMobileClient.getInstance().refresh();
                        Log.i("userState", "need to login again.SIGNED_OUT_USER_POOLS_TOKENS_INVALID");
                        Intent intent_so = new Intent(getApplicationContext(), AuthenticationActivity.class);
                        startActivity(intent_so);
                        break;
                    case SIGNED_OUT_FEDERATED_TOKENS_INVALID:
                        AWSMobileClient.getInstance().signOut();
                        Log.i("userState", "need to login again SIGNED_OUT_FEDERATED_TOKENS_INVALID.");
                        Intent intent_fo = new Intent(getApplicationContext(), AuthenticationActivity.class);
                        startActivity(intent_fo);
                        break;
                    default:
                        Log.i("addUserStateListener", "Default state "+userStateDetails.getUserState().toString() );
                }
            }
        });


        /**
         * ADDING click functionality to the recycler items
         */
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                mRecyclerView, new RecyclerTouchListener.ClickListener() {

            @Override
            public void onClick(View view, final int position) {
                Toast.makeText(MainActivity.this, "Single Click on position        :"+position,
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), displayRecipeActivity.class);
                //intent.putExtra(EXTRA_MESSAGE, myCountry);
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {
                if(mRecipe.get(position).owner().equals(AWSMobileClient.getInstance().getUsername())){
                    showMutateDialog(position);}
                else{
                    Toast.makeText(MainActivity.this, "Only the Creators can edit the Recipe : "+mRecipe.get(position).owner(),
                            Toast.LENGTH_LONG).show();
                }

            }
        }));
    }

    /**
     * Opens dialog with Update - Delete options
     * Edit - 0
     * Delete - 1
     */
    private void showMutateDialog(final int position) {
        CharSequence options[] = new CharSequence[]{"Update", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Would you like to update or delete recipe?");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 1) {
                    showDeleteDialog(position);
                } else{
                    Log.i(TAG, "should go to ingredients activity for "+position);
                    Intent intent = new Intent(getApplicationContext(), updateRecipeActivity.class);
                    intent.putExtra("add_key",  mRecipe.get(position).id());
                    startActivityForResult(intent,1);
                }
            }
        }); //todo add update
        builder.show();
    }

    private void showDeleteDialog(final int position) {
        CharSequence options[] = new CharSequence[]{"Yes", "No"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you sure you want to delete recipe?");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Intent intent = new Intent(getApplicationContext(), deleteRecipeActivity.class);
                    intent.putExtra("main_key", mRecipe.get(position).id());
                    startActivityForResult(intent,1);
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Query list data when we return to the screen
        query();
        subscribe();
    }

    public void query(){
        ClientFactory.appSyncClient().query(ListRecipesQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST)
                .enqueue(queryCallback);
    }

    private GraphQLCall.Callback<ListRecipesQuery.Data> queryCallback = new GraphQLCall.Callback<ListRecipesQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListRecipesQuery.Data> response) {

            mRecipe = new ArrayList<>(response.data().listRecipes().items());

            Log.i(TAG, "Retrieved list items: " + mRecipe.toString());
            Log.i(TAG, "Retrieved list size: " + mRecipe.size());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.setItems(mRecipe);
                    mAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, e.toString());
        }
    };

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_signout) {
            Toast.makeText(MainActivity.this, "Single Click on Sign Out        :"+id,
                    Toast.LENGTH_SHORT).show();
            AWSMobileClient.getInstance().signOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Subscription call //AWSMobileClient.getInstance().getUsername()
    private AppSyncSubscriptionCall subscriptionWatcher;
    private void subscribe(){
        OnCreateRecipeSubscription subscription;
        subscription = OnCreateRecipeSubscription.builder()
                .build();
        subscriptionWatcher = ClientFactory.appSyncClient().subscribe(subscription);
        subscriptionWatcher.execute(subCallback);
    }


    private AppSyncSubscriptionCall.Callback subCallback = new AppSyncSubscriptionCall.Callback() {
        @Override
        public void onResponse(@Nonnull Response response) {
            Log.i("Response", "Received subscription notification: " + response.data().toString());

            //Update UI with newly added item
            OnCreateRecipeSubscription.OnCreateRecipe data = ((OnCreateRecipeSubscription.Data)response.data()).onCreateRecipe();
            final ListRecipesQuery.Item addedItem = new ListRecipesQuery.Item(data.__typename(), data.id(), data.recipeName(), data.description(), data.photo(), data.owner(), data.ingredients(),data.steps());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRecipe.add(addedItem);
                    mAdapter.notifyItemInserted(mRecipe.size() - 1);
                }
            });
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("Error", e.toString());
        }

        @Override
        public void onCompleted() {
            Log.i("Completed", "Subscription completed");
        }

    };

    @Override
    protected void onStop() {
        super.onStop();
        subscriptionWatcher.cancel();
    }




}
