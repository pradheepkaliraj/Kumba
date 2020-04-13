package com.example.pradh.demoapp;

import android.content.Context;
import android.util.Log;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.sigv4.CognitoUserPoolsAuthProvider;

/**
 * Created by pradh on 2/15/2020.
 * AWSAppSyncClient to perform API calls
 * This ClientFactory class supplies an AppSync client, which we can leverage to perform data access activities.
 */

public class ClientFactory {
    private static  volatile AWSAppSyncClient client; //Java volatile keyword guarantees visibility of changes to variables across threads. This may sound a bit abstract, so let me elaborate

    public static synchronized void init (final Context context){
        if(client == null){
            final AWSConfiguration awsConfiguration = new AWSConfiguration(context);
            client = AWSAppSyncClient.builder()
                    .context(context)
                    .awsConfiguration(awsConfiguration)
                    .cognitoUserPoolsAuthProvider(new CognitoUserPoolsAuthProvider() {
                        @Override
                        public String getLatestAuthToken() {
                            try{
                                return AWSMobileClient.getInstance().getTokens().getIdToken().getTokenString();
                            } catch (Exception e){
                                Log.e("APPSYNC_ERR", e.getLocalizedMessage());
                                return e.getLocalizedMessage();
                            }
                        }
                    }).build();
        }
    }

    public static synchronized AWSAppSyncClient appSyncClient(){
        return client;
    }

}
