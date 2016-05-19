package com.eyecuelab.survivalists.util;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;

/**
 * Created by nathanromike on 5/19/16.
 */
public class MatchInitiatedListener implements ResultCallback<TurnBasedMultiplayer.InitiateMatchResult> {
    private static final String TAG = "MatchInitiatedListener";

    @Override
    public void onResult(TurnBasedMultiplayer.InitiateMatchResult initiateMatchResult) {
        Log.v(TAG, "Match Initiated: " + initiateMatchResult.getMatch().getMatchId());
    }
}
