package com.eyecuelab.survivalists.ui;

import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.google.gson.Gson;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TitleActivity extends BaseGameActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    private static final String TAG = "TitleActivity";
    private final int SETTINGS_INTENT = 1;

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

        setFullScreen();

        //set content view AFTER ABOVE sequence (to avoid crash)
        setContentView(R.layout.activity_title);

        initializeGoogleApi();
        mGoogleApiClient.connect();

        ButterKnife.bind(this);

        //Create Shared Preferences
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mSharedPreferences.edit();

        mCurrentMatchId = mSharedPreferences.getString("matchId", null);

        currentCampaignButton.setOnClickListener(this);
        startCampaignButton.setOnClickListener(this);
        loginButton.setOnClickListener(this);
        joinCampaignButton.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setFullScreen();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFullScreen();
        mGoogleApiClient.reconnect();
    }

    @Override
    public void onClick(View view) {
        Intent campaignEditorIntent = new Intent(this, NewCampaignActivity.class);

        switch (view.getId()) {
            case R.id.currentCampaignButton:
                Intent currentCampaignIntent = new Intent(this, MainActivity.class);
                currentCampaignIntent.putExtra("mCurrentMatchId", mCurrentMatchId);
                startActivity(currentCampaignIntent);
                break;
            case R.id.startCampaignButton:
                campaignEditorIntent.putExtra("statusTag", Constants.START_CAMPAIGN_INTENT);
                startActivity(campaignEditorIntent);
                break;
            case R.id.loginButton:
                googleButtonHandler();
                break;
            case R.id.joinCampaignButton:
                campaignEditorIntent.putExtra("statusTag", Constants.JOIN_CAMPAIGN_INTENT);
                startActivity(campaignEditorIntent);
                break;
        }
    }

    public void setFullScreen() {
        //Remove notification and navigation bars
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    //Google api logic
    public void initializeGoogleApi() {
        this.mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApiIfAvailable(Games.API, Games.SCOPE_GAMES)
                .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.v(TAG, "Connected to Google Api Client.");
        mCurrentPlayerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);

        if (mCurrentMatchId == null) {
            String userName = Games.Players.getCurrentPlayer(mGoogleApiClient).getDisplayName();

            //Save to shared preferences
            mEditor.putString(Constants.PREFERENCES_GOOGLE_PLAYER_ID, mCurrentPlayerId);
            mEditor.commit();

            //Save user info to firebase
            mUserFirebaseRef = new Firebase(Constants.FIREBASE_URL_USERS + "/" + mCurrentPlayerId);
            mUserFirebaseRef.child("displayName").setValue(userName);
            mUserFirebaseRef.child("atSafeHouse").setValue(false);
            mUserFirebaseRef.child("joinedMatch").setValue(false);
        } else {
            loadMatch(mCurrentMatchId);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.v(TAG, "Google Api failed, error code: " + connectionResult.getErrorCode());
        try {
            connectionResult.startResolutionForResult(this, connectionResult.getErrorCode());
        } catch (IntentSender.SendIntentException sendIntent) {
            sendIntent.getStackTrace();
            Log.v(TAG, "Fatal Google API error");
        }
    }

    public void googleButtonHandler() {
        if (mGoogleApiClient.isConnected()) {
            Intent settingsIntent = Games.getSettingsIntent(mGoogleApiClient);
            startActivityForResult(settingsIntent, SETTINGS_INTENT);
        } else {
            Toast.makeText(this, "Connecting to Google Play Services", Toast.LENGTH_LONG).show();
            mGoogleApiClient.connect();
            mGoogleApiClient.reconnect();
            Log.v(TAG, "Reconnecting");
        }
    }

    public void loadMatch(String matchId) {
        if (matchId != null) {
            Games.TurnBasedMultiplayer.loadMatch(mGoogleApiClient, matchId).setResultCallback(new ResultCallback<TurnBasedMultiplayer.LoadMatchResult>() {
                @Override
                public void onResult(@NonNull TurnBasedMultiplayer.LoadMatchResult result) {
                    mCurrentMatch = result.getMatch();
                }
            });
        }
    }

    //Required to extend BaseGameActivity and make GoogleApiClient available throughout
    @Override
    public void onSignInFailed() {}
    @Override
    public void onSignInSucceeded() {}
}
