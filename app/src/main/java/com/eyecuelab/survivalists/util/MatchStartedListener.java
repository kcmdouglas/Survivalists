package com.eyecuelab.survivalists.util;

import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer.UpdateMatchResult;

/**
 * Created by eyecue on 5/19/16.
 */
public class MatchStartedListener implements ResultCallback<UpdateMatchResult> {
    private static final String TAG = "MatchUpdateListener";

    @Override
    public void onResult(UpdateMatchResult updateMatchResult) {
        Log.v(TAG, "Match Update: " + updateMatchResult.getMatch().getMatchId());
    }
}
