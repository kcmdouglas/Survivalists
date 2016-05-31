package com.eyecuelab.survivalists.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.eyecuelab.survivalists.Constants;
import com.eyecuelab.survivalists.R;
import com.eyecuelab.survivalists.models.Character;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.google.gson.Gson;

import org.parceler.Parcels;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TitleActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    private static final String TAG = "TitleActivity";

    private GoogleApiClient mGoogleApiClient;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private Firebase mUserFirebaseRef;
    private TurnBasedMatch mCurrentMatch;

    private String mCurrentPlayerId;
    private String mCurrentMatchId;

    @Bind(R.id.currentCampaignButton) Button currentCampaignButton;
    @Bind(R.id.startCampaignButton) Button startCampaignButton;
    @Bind(R.id.loginButton) Button loginButton;
    @Bind(R.id.joinCampaignButton) Button joinCampaignButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);

        //Remove notification and navigation bars
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        //set content view AFTER ABOVE sequence (to avoid crash)
        setContentView(R.layout.activity_title);

        ButterKnife.bind(this);

        //Create Shared Preferences
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mSharedPreferences.edit();

        mCurrentMatchId = mSharedPreferences.getString("matchId", null);

        initializeGoogleApi();

        currentCampaignButton.setOnClickListener(this);
        startCampaignButton.setOnClickListener(this);
        loginButton.setOnClickListener(this);
        joinCampaignButton.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.reconnect();
        }
    }

    @Override
    public void onClick(View view) {
        Intent campaignEditorIntent = new Intent(this, NewCampaignActivity.class);

        switch (view.getId()) {
            case R.id.currentCampaignButton:
                Intent currentCampaignIntent = new Intent(this, NotebookActivity.class);
                startActivity(currentCampaignIntent);
                break;
            case R.id.startCampaignButton:
                campaignEditorIntent.putExtra("statusTag", 1);
                startActivity(campaignEditorIntent);
                break;
            case R.id.loginButton:
                googleButtonHandle();
                break;
            case R.id.joinCampaignButton:
                campaignEditorIntent.putExtra("statusTag", 2);
                startActivity(campaignEditorIntent);
                break;
        }
    }


    //Google api logic
    @Override
    public void onConnected(Bundle connectionHint) {
        mCurrentPlayerId = Games.Players.getCurrentPlayerId(mGoogleApiClient).toString();
        String userName = Games.Players.getCurrentPlayer(mGoogleApiClient).getDisplayName();

        //Save to shared preferences
        mEditor.putString(Constants.PREFERENCES_GOOGLE_PLAYER_ID, mCurrentPlayerId);
        mEditor.putString("userId", mCurrentPlayerId);
        mEditor.putString("userName", userName);
        mEditor.commit();

        saveUserInfoToFirebase();

        //Save user info to firebase
        mUserFirebaseRef = new Firebase(Constants.FIREBASE_URL_USERS + "/" + mCurrentPlayerId);
        mUserFirebaseRef.child("displayName").setValue(userName);
        mUserFirebaseRef.child("atSafeHouse").setValue(false);

        if (mCurrentMatch == null) {
            mUserFirebaseRef.child("joinedMatch").setValue(false);
        }
        //TODO: Move this to the main activity
//        if(mCurrentMatchId != null) {
//            mUserFirebaseRef.child("character").addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    String name = dataSnapshot.child("name").getValue().toString();
//                    long ageLong = (long) dataSnapshot.child("age").getValue();
//                    int age = (int) ageLong;
//                    String description = dataSnapshot.child("description").getValue().toString();
//                    long characterIdLong = (long) dataSnapshot.child("characterId").getValue();
//                    int characterId = (int) characterIdLong;
//                    long healthLong = (long) dataSnapshot.child("health").getValue();
//                    int health = (int) healthLong;
//                    long fullnessLevelLong = (long) dataSnapshot.child("fullnessLevel").getValue();
//                    int fullnessLevel = (int) fullnessLevelLong;
//                    String characterUrl = dataSnapshot.child("characterPictureUrl").getValue().toString();
//                    mCurrentCharacter = new Character(name, description, age, health, fullnessLevel, characterUrl, characterId);
//                    Log.d("Current Character ID: ", mCurrentCharacter.getCharacterId() + "");
//
//                    Gson gson = new Gson();
//                    String currentCharacter = gson.toJson(mCurrentCharacter);
//                    mEditor.putString(Constants.PREFERENCES_CHARACTER, currentCharacter);
//                    mEditor.commit();
//                }
//
//                @Override
//                public void onCancelled(FirebaseError firebaseError) {
//
//                }
//            });
//            instantiatePlayerIDs();
//            mUserFirebaseRef.child("joinedMatch").setValue(true);
//        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection to Google failed, try again.", Toast.LENGTH_LONG).show();
    }

    public void initializeGoogleApi() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API)
                .setGravityForPopups(Gravity.TOP)
                .build();
    }

    public void saveUserInfoToFirebase() {
        String userName = Games.Players.getCurrentPlayer(mGoogleApiClient).getDisplayName();
        mUserFirebaseRef = new Firebase(Constants.FIREBASE_URL_USERS + "/" + mCurrentPlayerId + "/");

        if (mCurrentMatch != null) {
            mUserFirebaseRef.child("displayName").setValue(userName);
            mUserFirebaseRef.child("atSafeHouse").setValue(false);
            //Update in match boolean
            mUserFirebaseRef.child("joinedMatch").setValue(true);
        } else {
            mUserFirebaseRef.child("joinedMatch").setValue(false);
        }
    }

    //TODO: This might be needed here
//    public void instantiatePlayerIDs() {
//        mPlayerIDs = new ArrayList<>();
//        Firebase playerIDRef = new Firebase(Constants.FIREBASE_URL_TEAM + "/" + mCurrentMatchId + "/players/");
//        playerIDRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot child : dataSnapshot.getChildren()) {
//                    String playerId = child.getValue().toString();
//                    if (!(playerId.equals(mCurrentPlayerId))) {
//                        mPlayerIDs.add(playerId);
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//
//            }
//        });
//
//        mEditor.putString(Constants.PREFERENCES_TEAM_IDs, TextUtils.join(",", mPlayerIDs));
//        mEditor.commit();
//
//    }

    public void googleButtonHandle() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
            Log.v(TAG, "Disconnected");
        } else {
            mGoogleApiClient.connect();
            mGoogleApiClient.reconnect();
            Log.v(TAG, "Reconnecting");
        }
    }
}
