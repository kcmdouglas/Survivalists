package com.eyecuelab.survivalists.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.eyecuelab.survivalists.Constants;
import com.firebase.client.Firebase;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.example.games.basegameutils.BaseGameActivity;

import java.util.ArrayList;

/**
 * Created by eyecue on 6/2/16.
 */
public class MatchLoadedListener extends BaseGameActivity implements ResultCallback<TurnBasedMultiplayer.LoadMatchResult> {
    private static final String TAG = "MatchLoadedListener";

    @Override
    public void onResult(@NonNull TurnBasedMultiplayer.LoadMatchResult result) {
        Log.v(TAG, result.getMatch().getMatchId() + "");
        takeTurnFromListener(result.getMatch());
    }

    public void takeTurnFromListener(TurnBasedMatch match) {
        TurnBasedMatch mCurrentMatch = match;
        GoogleApiClient mGoogleApiClient = getApiClient();
        String mCurrentMatchId = match.getMatchId();
        String mCurrentPlayerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);

        //Create Shared Preferences
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();

        mEditor.putString(Constants.PREFERENCES_MATCH_ID, mCurrentMatchId);
        mEditor.putInt(Constants.PREFERENCES_LAST_SAFEHOUSE_ID, 0);
        mEditor.putInt(Constants.PREFERENCES_NEXT_SAFEHOUSE_ID, 1);
        mEditor.commit();


        byte[] turnData = new byte[1];

        ArrayList<Participant> allPlayers = match.getParticipants();
        int nextPlayerNumber = Integer.parseInt(match.getLastUpdaterId().substring(2));
        try {
            //Should pass invitation to the next player
            String nextPlayerId = allPlayers.get(nextPlayerNumber).getParticipantId();
            Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, mCurrentMatchId, turnData, nextPlayerId);

            //Grab the next player in case the previous above didn't work
            nextPlayerId = allPlayers.get(nextPlayerNumber + 1).getParticipantId();
            Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, mCurrentMatchId, turnData, nextPlayerId);
        } catch (IndexOutOfBoundsException indexOutOfBonds) {
            Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, mCurrentMatchId, turnData, match.getPendingParticipantId());
        }

        //Update firebase to show player joined
        Firebase mUserFirebaseRef = new Firebase(Constants.FIREBASE_URL_USERS + "/" + mCurrentPlayerId + "/");
        mUserFirebaseRef.child("teamId").setValue(mCurrentMatchId);
        mUserFirebaseRef.child("joinedMatch").setValue(true);
    }

    @Override
    public void onSignInFailed() {}

    @Override
    public void onSignInSucceeded() {}
}
