package com.eyecuelab.survivalists.util;

import android.util.Log;

import com.eyecuelab.survivalists.ui.MainActivity;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.request.GameRequest;
import com.google.android.gms.games.request.OnRequestReceivedListener;

/**
 * Created by nathanromike on 5/19/16.
 */
public class InvitationListener extends MainActivity
        implements OnInvitationReceivedListener, OnRequestReceivedListener {
    private static final String TAG = "InvitationListener";

    @Override
    public void onInvitationReceived(final Invitation invitation) {
        Log.v(TAG, "invitation: " + invitation.getInviter().getDisplayName());
    }

    @Override
    public void onInvitationRemoved(String s) {
        Log.v(TAG, "Invitation Deleted!");
    }

    @Override
    public void onRequestReceived(GameRequest gameRequest) {
        Log.v(TAG, "Request: " + gameRequest.getGame().getDisplayName());
    }

    @Override
    public void onRequestRemoved(String s) {
        Log.v(TAG, "Request Deleted!");
    }
}
