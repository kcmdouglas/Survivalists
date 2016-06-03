package com.eyecuelab.survivalists.util;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.eyecuelab.survivalists.Constants;
import com.eyecuelab.survivalists.ui.NewCampaignActivity;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;

/**
 * Created by nathanromike on 5/19/16.
 */
public class MatchUpdateListener extends NewCampaignActivity implements OnTurnBasedMatchUpdateReceivedListener {
    private static final String TAG = "MatchUpdateListener";

    @Override
    public void onTurnBasedMatchReceived(TurnBasedMatch turnBasedMatch) {
        try {
            Log.v(TAG, turnBasedMatch.getLastUpdaterId());
            String lastToUpdate = turnBasedMatch.getLastUpdaterId();

            boolean playerHasTakenTurn = true;
            Intent updateUiIntent = new Intent(NewCampaignActivity.RECEIVE_UPDATE_FROM_MATCH);
            updateUiIntent.putExtra(Constants.MATCH_UPDATE_INTENT_EXTRA, playerHasTakenTurn);
            updateUiIntent.putExtra(Constants.MATCH_UPDATE_INTENT_EXTRA_PLAYER, lastToUpdate);
            LocalBroadcastManager.getInstance(this).sendBroadcast(updateUiIntent);

        } catch (NullPointerException nullPointer) {
            Log.v(TAG, nullPointer.getMessage());
        }
    }

    @Override
    public void onTurnBasedMatchRemoved(String s) {
        Log.v(TAG, "Match Deleted");
    }
}
