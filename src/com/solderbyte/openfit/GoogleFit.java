package com.solderbyte.openfit;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;


public class GoogleFit extends Activity {
    private static final String LOG_TAG = "OpenFit:GoogleFit";

    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;

    private GoogleApiClient mClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        if(savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

        buildFitnessClient();
    }

    private void buildFitnessClient() {
        mClient = new GoogleApiClient.Builder(this)
        .addApi(Fitness.HISTORY_API)
        .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
        .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                Log.d(LOG_TAG, "Connected!!!");
            }

            @Override
            public void onConnectionSuspended(int i) {
                if(i == ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                    Log.d(LOG_TAG, "Connection lost.  Cause: Network Lost.");
                } 
                else if(i == ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                    Log.d(LOG_TAG, "Connection lost.  Reason: Service Disconnected");
                }
            }
        }).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult result) {
                Log.d(LOG_TAG, "Connection failed. Cause: " + result.toString());
                if(!result.hasResolution()) {
                    GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), GoogleFit.this, 0).show();
                    return;
                }
                if(!authInProgress) {
                    try {
                        Log.i(LOG_TAG, "Attempting to resolve failed connection");
                        authInProgress = true;
                        result.startResolutionForResult(GoogleFit.this, REQUEST_OAUTH);
                    }
                    catch(IntentSender.SendIntentException e) {
                        Log.e(LOG_TAG, "Exception while starting resolution activity", e);
                    }
                }
            }
        }).build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "Connecting...");
        mClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mClient.isConnected()) {
            mClient.disconnect();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_OAUTH) {
            authInProgress = false;
            if(resultCode == RESULT_OK) {
                if(!mClient.isConnecting() && !mClient.isConnected()) {
                    mClient.connect();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }
}
