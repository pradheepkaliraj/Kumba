package com.example.pradh.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserStateDetails;


public class AuthenticationActivity extends AppCompatActivity {
    private final String TAG = "authentication activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails result) {
                Log.i(TAG, result.getUserState().toString());
                switch (result.getUserState()) {
                    case SIGNED_IN:
                        findUserName();
                        Intent i = new Intent(AuthenticationActivity.this, MainActivity.class);
                        startActivity(i);
                        break;
                    case SIGNED_OUT:
                        ShowSignIn();
                        break;
                    default:
                        AWSMobileClient.getInstance().signOut();
                        ShowSignIn();
                        break;
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, e.toString());
            }
        });

    }

    private void findUserName() {
        Log.i(TAG, "Signed in user: "+AWSMobileClient.getInstance().getUsername());
    }


    private void ShowSignIn() {
        try {
            AWSMobileClient.getInstance().showSignIn(this, SignInUIOptions.builder().nextActivity(MainActivity.class).build());
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }


}