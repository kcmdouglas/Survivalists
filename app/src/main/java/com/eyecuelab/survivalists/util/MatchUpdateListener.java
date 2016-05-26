package com.eyecuelab.survivalists.util;

import android.util.Log;

import com.eyecuelab.survivalists.ui.MainActivity;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;

/**
 * Created by nathanromike on 5/19/16.
 */
public class MatchUpdateListener implements OnTurnBasedMatchUpdateReceivedListener {
    private static final String TAG = "MatchUpdateListener";

    @Override
    public void onTurnBasedMatchReceived(TurnBasedMatch turnBasedMatch) {
        try {
            Log.v(TAG, turnBasedMatch.getData().toString());
        } catch (NullPointerException nullPointer) {
            Log.v(TAG, nullPointer.getMessage());
        }
    }

    @Override
    public void onTurnBasedMatchRemoved(String s) {
        Log.v(TAG, "Match Deleted");
    }
}
