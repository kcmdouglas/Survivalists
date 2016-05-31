package com.eyecuelab.survivalists.util;

import android.support.annotation.NonNull;
import android.util.Log;

import com.eyecuelab.survivalists.ui.NewCampaignActivity;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;

/**
 * Created by nathanromike on 5/19/16.
 */
public class MatchInitiatedListener extends NewCampaignActivity implements ResultCallback<TurnBasedMultiplayer.InitiateMatchResult> {
    private static final String TAG = "MatchInitiatedListener";

    @Override
    public void onResult(TurnBasedMultiplayer.InitiateMatchResult initiateMatchResult) {
        Log.v(TAG, initiateMatchResult.getMatch().getMatchId() + "");
    }
}
