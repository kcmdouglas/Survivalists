package com.eyecuelab.survivalists.services;

import android.content.Context;
import android.view.View;

import com.eyecuelab.survivalists.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;

/**
 * Created by eyecue on 5/16/16.
 */
public class GooglePlayGamesService {

    private static GoogleApiClient mGoogleApiClient;

    public static void connectToGooglePlay(Context thisContext, GoogleApiClient.ConnectionCallbacks connectionCallbacks) {

        //Google Play Games client and correlating buttons
        mGoogleApiClient = new GoogleApiClient.Builder(thisContext.getApplicationContext())
                .addConnectionCallbacks(connectionCallbacks)
                .addApi(Games.API)
                .build();
    }

    public static void reconnectToGooglePlay() {
        mGoogleApiClient.reconnect();
    }

    public static GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public static void disconnectGoogleApi() {
        mGoogleApiClient.disconnect();
    }
}
