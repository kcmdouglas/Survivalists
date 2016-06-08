package com.eyecuelab.survivalists.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.eyecuelab.survivalists.Constants;
import com.eyecuelab.survivalists.R;
import com.eyecuelab.survivalists.ui.NewCampaignActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.multiplayer.Participant;

import java.util.ArrayList;

/**
 * Created by eyecue on 6/7/16.
 */
public class InvitePlayerAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

    private Context mContext;
    ArrayList<Player> mPlayers;
    private int mInvitationListLayout;
    GoogleApiClient mGoogleApiClient;

    public InvitePlayerAdapter(Context context, ArrayList<Player> players, int invitationListLayout, GoogleApiClient googleApiClient) {
        mContext = context;
        mPlayers = players;
        mInvitationListLayout = invitationListLayout;
        mGoogleApiClient = googleApiClient;
    }

    @Override
    public int getCount() {
        return mPlayers.size();
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
    public View getView(int position, View convertView, final ViewGroup parent) {
        RecordHolder holder = new RecordHolder();

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
            final Player player = mPlayers.get(position);
            holder.txtTitle.setText(player.getDisplayName());
            Uri imageUri = player.getIconImageUri();
            ImageManager imageManager = ImageManager.create(mContext);
            imageManager.loadImage(holder.imageItem, imageUri);
            holder.toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Toast.makeText(buttonView.getContext(), player.getDisplayName() + " added to list", Toast.LENGTH_SHORT).show();
                        addPlayerToList(player.getPlayerId());
                    } else {
                        Toast.makeText(buttonView.getContext(), player.getDisplayName() + " removed from list", Toast.LENGTH_SHORT).show();
                        removePlayerFromList(player.getPlayerId());
                    }
                }
            });


//                    .setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Toast.makeText(v.getContext(), player.getDisplayName() + " added to list", Toast.LENGTH_SHORT).show();
//                    addPlayerToList(player.getPlayerId());
//                }
//            });
        } catch (NullPointerException np) {
            np.getStackTrace();
        }
        return convertView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    public void addPlayerToList(String playerId) {
        Intent broadcastIntent = new Intent(NewCampaignActivity.PLAYER_ADDED_TO_LIST);
        broadcastIntent.putExtra(Constants.PLAYER_ADDED_TO_LIST_INTENT, playerId);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(broadcastIntent);
    }

    public void removePlayerFromList(String playerId) {
        Intent broadcastIntent = new Intent(NewCampaignActivity.PLAYER_REMOVED_FROM_LIST);
        broadcastIntent.putExtra(Constants.PLAYER_REMOVED_FROM_LIST_INTENT, playerId);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(broadcastIntent);
    }

    static class RecordHolder {
        TextView txtTitle;
        ImageView imageItem;
        ToggleButton toggleButton;
    }
}
