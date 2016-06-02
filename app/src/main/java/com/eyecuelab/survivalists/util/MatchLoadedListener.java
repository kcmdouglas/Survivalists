package com.eyecuelab.survivalists.util;

import android.support.annotation.NonNull;
import android.util.Log;

import com.eyecuelab.survivalists.ui.NewCampaignActivity;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
/**
 * Created by eyecue on 6/2/16.
 */
public class MatchLoadedListener extends NewCampaignActivity implements ResultCallback<TurnBasedMultiplayer.LoadMatchResult> {
    private static final String TAG = "MatchLoadedListener";

    @Override
    public void onResult(@NonNull TurnBasedMultiplayer.LoadMatchResult result) {
        Log.v(TAG, result.getMatch().getMatchId() + "");
    }
}
