package com.eyecuelab.survivalists.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.eyecuelab.survivalists.Constants;
import com.eyecuelab.survivalists.R;
import com.eyecuelab.survivalists.ui.NewCampaignActivity;
import com.firebase.client.Firebase;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;

import java.util.ArrayList;

/**
 * Created by eyecue on 6/2/16.
 */
public class InvitationAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
    private static final String TAG = "InvitationAdapter";

    private Context mContext;
    ArrayList<Participant> mParticipants;
    ArrayList<Invitation> mInvitations;
    private int mInvitationListLayout;
    private int mItemPosition;
    GoogleApiClient mGoogleApiClient;

    public InvitationAdapter(Context context, ArrayList<Participant> participants, ArrayList<Invitation> invitations, int invitationListLayout, GoogleApiClient googleApiClient) {
        mContext = context;
        mParticipants = participants;
        mInvitations = invitations;
        mInvitationListLayout = invitationListLayout;
        mGoogleApiClient = googleApiClient;
    }

    @Override
    public int getCount() {
        return mParticipants.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        RecordHolder holder = new RecordHolder();
        mItemPosition = position;

        if (convertView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(mInvitationListLayout, parent, false);
            holder.txtTitle = (TextView) convertView.findViewById(R.id.playerNameTextView);
            holder.imageItem = (ImageView) convertView.findViewById(R.id.playerAvatarImage);
            holder.toggleButton = (ToggleButton) convertView.findViewById(R.id.invitationToggle);
            convertView.setTag(holder);
        } else {
            holder = (RecordHolder) convertView.getTag();
        }

        try {
            final Participant participant = mParticipants.get(position);
            final Invitation invitation = mInvitations.get(position);
            holder.txtTitle.setText(participant.getDisplayName());
            holder.toggleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Games.TurnBasedMultiplayer.acceptInvitation(mGoogleApiClient, invitation.getInvitationId());
                    Games.TurnBasedMultiplayer.loadMatch(mGoogleApiClient, invitation.getInvitationId()).setResultCallback(new ResultCallback<TurnBasedMultiplayer.LoadMatchResult>() {
                        @Override
                        public void onResult(@NonNull TurnBasedMultiplayer.LoadMatchResult result) {
                            String mCurrentMatchId = result.getMatch().getMatchId();
                            String mCurrentPlayerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
                            TurnBasedMatch match = result.getMatch();

                            //Create Shared Preferences
                            SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                            SharedPreferences.Editor mEditor = mSharedPreferences.edit();

                            mEditor.putString(Constants.PREFERENCES_MATCH_ID, mCurrentMatchId);
                            mEditor.putInt(Constants.PREFERENCES_LAST_SAFEHOUSE_ID, 0);
                            mEditor.putInt(Constants.PREFERENCES_NEXT_SAFEHOUSE_ID, 1);
                            mEditor.apply();

                            Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, mCurrentMatchId, match.getData(), match.getPendingParticipantId()).setResultCallback(new ResultCallbacks<TurnBasedMultiplayer.UpdateMatchResult>() {
                                @Override
                                public void onSuccess(@NonNull TurnBasedMultiplayer.UpdateMatchResult updateMatchResult) {
                                    Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, updateMatchResult.getMatch().getMatchId(), updateMatchResult.getMatch().getData(), updateMatchResult.getMatch().getCreatorId());
                                }
                                @Override
                                public void onFailure(@NonNull Status status) {
                                    Log.e(TAG, status.getStatusMessage() + "");
                                }
                            });

                            //Update firebase to show player joined
                            Firebase mUserFirebaseRef = new Firebase(Constants.FIREBASE_URL_USERS + "/" + mCurrentPlayerId + "/");
                            mUserFirebaseRef.child("teamId").setValue(mCurrentMatchId);
                            mUserFirebaseRef.child("joinedMatch").setValue(true);

                            notifyUi();

                        }
                    });
                }
            });

            Uri imageUri = participant.getIconImageUri();
            ImageManager imageManager = ImageManager.create(mContext);
            imageManager.loadImage(holder.imageItem, imageUri);

        } catch (IndexOutOfBoundsException outOfBounds) {
            Log.v(TAG, outOfBounds.getMessage());
        }

        return convertView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    public void notifyUi() {
        boolean matchMakingComplete = true;
        Intent broadcastIntent = new Intent(NewCampaignActivity.RECEIVE_UPDATE_FROM_INVITATION);
        broadcastIntent.putExtra(Constants.INVITATION_UPDATE_INTENT_EXTRA, matchMakingComplete);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(broadcastIntent);
    }

    static class RecordHolder {
        TextView txtTitle;
        ImageView imageItem;
        ToggleButton toggleButton;
    }
}
