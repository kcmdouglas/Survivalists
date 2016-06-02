package com.eyecuelab.survivalists.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.eyecuelab.survivalists.Constants;
import com.eyecuelab.survivalists.R;
import com.eyecuelab.survivalists.models.Item;
import com.eyecuelab.survivalists.models.Weapon;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;

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
    private int mStackLevel;
    private ArrayList<Weapon> allWeapons;
    private ArrayList<Item> allItems;

    @Bind(R.id.currentCampaignButton) Button currentCampaignButton;
    @Bind(R.id.startCampaignButton) Button startCampaignButton;
    @Bind(R.id.loginButton) Button loginButton;
    @Bind(R.id.joinCampaignButton) Button joinCampaignButton;
    @Bind(R.id.merchantTest) Button merchantTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        allWeapons = new ArrayList<>();
        allItems = new ArrayList<>();

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
                Intent currentCampaignIntent = new Intent(this, MainActivity.class);
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
        mCurrentPlayerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);

        if (mCurrentMatch == null) {
            String userName = Games.Players.getCurrentPlayer(mGoogleApiClient).getDisplayName();

            //Save to shared preferences
            mEditor.putString(Constants.PREFERENCES_GOOGLE_PLAYER_ID, mCurrentPlayerId);
            mEditor.putString("userId", mCurrentPlayerId);
            mEditor.putString("userName", userName);
            mEditor.commit();

            //Save user info to firebase
            mUserFirebaseRef = new Firebase(Constants.FIREBASE_URL_USERS + "/" + mCurrentPlayerId);
            mUserFirebaseRef.child("displayName").setValue(userName);
            mUserFirebaseRef.child("atSafeHouse").setValue(false);
            mUserFirebaseRef.child("joinedMatch").setValue(false);
        }
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
                .build();
    }

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
