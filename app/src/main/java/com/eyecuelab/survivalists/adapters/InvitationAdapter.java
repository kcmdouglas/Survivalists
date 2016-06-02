package com.eyecuelab.survivalists.adapters;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.eyecuelab.survivalists.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
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
                    final byte[] gameData = new byte[1];
                    Games.TurnBasedMultiplayer.loadMatch(mGoogleApiClient, invitation.getInvitationId()).setResultCallback(new ResultCallback<TurnBasedMultiplayer.LoadMatchResult>() {
                        @Override
                        public void onResult(@NonNull TurnBasedMultiplayer.LoadMatchResult result) {
                            TurnBasedMatch match = result.getMatch();
                            String nextPlayer = match.getPendingParticipantId();
                            String lastUpdated = match.getLastUpdaterId();
                            int nextPlayerNumber = Integer.parseInt(match.getLastUpdaterId().substring(2));
                            ArrayList<Participant> allPlayers = match.getParticipants();

                            Log.v(TAG,  "Pending player " + nextPlayer + " Last updated " + lastUpdated);

                            try {
                                //Should pass invitation to the next player
                                String nextPlayerId = allPlayers.get(nextPlayerNumber).getParticipantId();
                                Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, match.getMatchId(), gameData, nextPlayerId);

                                //Grab the next player in case the previous above didn't work
                                nextPlayerId = allPlayers.get(nextPlayerNumber + 1).getParticipantId();
                                Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, match.getMatchId(), gameData, nextPlayerId);
                            } catch (IndexOutOfBoundsException indexOutOfBonds) {
                                Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, match.getMatchId(), gameData, match.getPendingParticipantId());
                            }
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

    static class RecordHolder {
        TextView txtTitle;
        ImageView imageItem;
        ToggleButton toggleButton;
    }
}
