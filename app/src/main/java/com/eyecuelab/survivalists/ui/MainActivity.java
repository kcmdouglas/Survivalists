package com.eyecuelab.survivalists.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.eyecuelab.survivalists.Constants;
import com.eyecuelab.survivalists.R;
import com.eyecuelab.survivalists.SurvivalistsApplication;
import com.eyecuelab.survivalists.models.Character;
import com.eyecuelab.survivalists.models.User;
import com.eyecuelab.survivalists.models.SafeHouse;
import com.eyecuelab.survivalists.services.BackgroundStepService;
import com.eyecuelab.survivalists.util.CampaignEndAlarmReceiver;
import com.eyecuelab.survivalists.util.InvitationListener;
import com.eyecuelab.survivalists.util.MatchInitiatedListener;
import com.eyecuelab.survivalists.util.MatchStartedListener;
import com.eyecuelab.survivalists.util.MatchUpdateListener;
import com.eyecuelab.survivalists.util.StepResetAlarmReceiver;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.FirebaseException;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;

import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.ParticipantResult;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import org.parceler.Parcels;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends FragmentActivity
        implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "MainActivity";

    @Bind(R.id.stepTextView) TextView counter;
    @Bind(R.id.dailyStepsTextView) TextView dailyCounter;
    @Bind(R.id.sign_in_button) SignInButton signInButton;
    @Bind(R.id.sign_out_button) Button signOutButton;
    @Bind(R.id.findPlayersButton) Button findPlayersButton;
    @Bind(R.id.endMatchButton) Button endMatchButton;
    @Bind(R.id.playerTextView) TextView playersTextView;
    @Bind(R.id.testButton) Button testButton;
    @Bind(R.id.matchIdTextView) TextView matchIdTextView;
    @Bind(R.id.userIdTextView) TextView userIdTextView;
    @Bind(R.id.userNameTextView) TextView userNameTextView;
    @Bind(R.id.safehouseTextView) TextView safehouseTextView;
    @Bind(R.id.characterButton) Button characterButton;
    @Bind(R.id.testButton2) Button testButtonTwo;

    private int stepsInSensor;
    private int dailySteps;
    private String mCurrentMatchId;
    private String mMatchDuration;
    private int mLastSafeHouseId;
    private int mNextSafeHouseId;
    private ArrayList<String> invitees;
    private byte[] turnData;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private GoogleApiClient mGoogleApiClient;
    private TurnBasedMatch mCurrentMatch;
    private String mCurrentPlayerId;
    private SafeHouse mPriorSafehouse;
    private SafeHouse mNextSafehouse;
    private Character mCurrentCharacter;

    private User mCurrentUser;

    //Flags to indicate return activity
    private static final int RC_SIGN_IN =  100;
    private static final int RC_SELECT_PLAYERS = 200;
    private static final int RC_WAITING_ROOM = 300;

    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInCLicked = false;
    private boolean mExplicitSignOut = false;
    private boolean mInSignInFlow = false;
    private Firebase mUserFirebaseRef;

    private Context mContext;

    Intent mBackgroundStepServiceIntent;
    private BackgroundStepService mBackgroundStepService;

    //Event Variables
    private int mStackLevel;
    private boolean eventOneInitiated;
    private boolean eventTwoInitiated;
    private boolean eventThreeInitiated;
    private boolean eventFourInitiated;
    private boolean eventFiveInitiated;

    private boolean isRecurringAlarmSet;
    private ArrayList<Character> mCharacters;

    @Override
    protected void onStart() {
        super.onStart();
        if (!mInSignInFlow && !mExplicitSignOut) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mCharacters = new ArrayList<>();
//        //Initialize Facebook SDK
//        FacebookSdk.sdkInitialize(getApplicationContext());
//        AppEventsLogger.activateApp(this);

        //set content view AFTER ABOVE sequence (to avoid crash)
        setContentView(R.layout.activity_main);
        mContext = this;
        ButterKnife.bind(this);

        //Create Shared Preferences
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mSharedPreferences.edit();

        initializeGoogleApi();

        signInButton.setOnClickListener(this);
        signOutButton.setOnClickListener(this);
        findPlayersButton.setOnClickListener(this);
        endMatchButton.setOnClickListener(this);
        testButton.setOnClickListener(this);
        characterButton.setOnClickListener(this);
        testButtonTwo.setOnClickListener(this);

        mCurrentMatchId = mSharedPreferences.getString("matchId", null);

        //Set counter text based on current shared preferences--these are updated in the shared preferences onChange listener
        dailySteps = mSharedPreferences.getInt(Constants.PREFERENCES_DAILY_STEPS, 0);
        dailyCounter.setText(Integer.toString(dailySteps));
        stepsInSensor = mSharedPreferences.getInt(Constants.PREFERENCES_STEPS_IN_SENSOR_KEY, 0);
        counter.setText(Integer.toString(stepsInSensor));

        eventOneInitiated = mSharedPreferences.getBoolean(Constants.PREFERENCES_INITIATE_EVENT_1, false);
        eventTwoInitiated = mSharedPreferences.getBoolean(Constants.PREFERENCES_INITIATE_EVENT_2, false);
        eventThreeInitiated = mSharedPreferences.getBoolean(Constants.PREFERENCES_INITIATE_EVENT_3, false);
        eventFourInitiated = mSharedPreferences.getBoolean(Constants.PREFERENCES_INITIATE_EVENT_4, false);
        eventFiveInitiated = mSharedPreferences.getBoolean(Constants.PREFERENCES_INITIATE_EVENT_5, false);

        //TODO: Move daily alarm setting to the startGame function
        //Set recurring alarm
        if(!isRecurringAlarmSet) {
            isRecurringAlarmSet = true;
            initiateDailyCountResetService();
        }

        //Initialize BackgroundStepService to run database injections and constant step updates
        mBackgroundStepService = new BackgroundStepService(mContext);
        mBackgroundStepServiceIntent = new Intent(mContext, mBackgroundStepService.getClass());
        if(!isBackgroundStepServiceRunning(mBackgroundStepService.getClass()))
        {
            startService(mBackgroundStepServiceIntent);
        }

        //TODO: Move to the startGame function

        mNextSafeHouseId = mSharedPreferences.getInt(Constants.PREFERENCES_NEXT_SAFEHOUSE_ID, 1);
        mLastSafeHouseId = mSharedPreferences.getInt(Constants.PREFERENCES_LAST_SAFEHOUSE_ID, 0);

        String safehouseJson = mSharedPreferences.getString("nextSafehouse", null);
        Gson gson = new Gson();
        mNextSafehouse = gson.fromJson(safehouseJson, SafeHouse.class);
        safehouseTextView.setText(Integer.toString(mNextSafeHouseId));



        if(mCharacters.size() == 0) {
            characterButton.setEnabled(false);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.reconnect();
        if (mCurrentMatch != null) {
            matchIdTextView.setText(mCurrentMatch.getMatchId());
        }
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
        if(mCharacters.size()==0) {
            characterButton.setEnabled(false);
        } else {
            characterButton.setEnabled(true);
        }
        if(mCurrentMatch != null && mCharacters == null) {
            instantiatePlayerCharacters();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        stopService(mBackgroundStepServiceIntent);
        super.onDestroy();
    }

    //Checks if background step service is running
    private boolean isBackgroundStepServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if(serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    //GOOGLE GAMES API LOGIC BEGINS HERE
    //TODO: Move this logic to a separate service class
    @Override
    public void onConnected(Bundle connectionHint) {
        signInButton.setVisibility(View.GONE);
        signOutButton.setVisibility(View.VISIBLE);
        mCurrentPlayerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
        mEditor.putString(Constants.PREFERENCES_GOOGLE_PLAYER_ID, mCurrentPlayerId);
        if (mCurrentMatch != null) {
            Games.TurnBasedMultiplayer
                    .loadMatch(mGoogleApiClient, mCurrentMatch.getMatchId());

            matchIdTextView.setText(mCurrentMatchId);
        }

        Games.setViewForPopups(mGoogleApiClient, getCurrentFocus());

        //Load current match
        loadMatch();

        Games.TurnBasedMultiplayer.registerMatchUpdateListener(mGoogleApiClient, new MatchUpdateListener());
        Games.Invitations.registerInvitationListener(mGoogleApiClient, new InvitationListener());

        String userName = Games.Players.getCurrentPlayer(mGoogleApiClient).getDisplayName();

        //Save to shared preferences
        mEditor.putString("userId", mCurrentPlayerId);
        mEditor.putString("userName", userName);
        mEditor.commit();

        saveUserInfoToFirebase();

        userIdTextView.setText(mCurrentPlayerId);
        String greeting = "Hello " + userName;
        userNameTextView.setText(greeting);

        //Save user info to firebase
        mUserFirebaseRef = new Firebase(Constants.FIREBASE_URL_USERS + "/" + mCurrentPlayerId);
        mUserFirebaseRef.child("displayName").setValue(userName);
        mUserFirebaseRef.child("atSafeHouse").setValue(false);

        if (mCurrentMatch == null) {
            mUserFirebaseRef.child("joinedMatch").setValue(false);
        } else {
            mUserFirebaseRef.child("joinedMatch").setValue(true);
        }
        if(mCurrentMatchId != null) {
            mUserFirebaseRef.child("character").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    long ageLong = (long) dataSnapshot.child("age").getValue();
                    int age = (int) ageLong;
                    String description = dataSnapshot.child("description").getValue().toString();
                    long characterIdLong = (long) dataSnapshot.child("characterId").getValue();
                    int characterId = (int) characterIdLong;
                    long healthLong = (long) dataSnapshot.child("health").getValue();
                    int health = (int) healthLong;
                    long fullnessLevelLong = (long) dataSnapshot.child("fullnessLevel").getValue();
                    int fullnessLevel = (int) fullnessLevelLong;
                    String characterUrl = dataSnapshot.child("characterPictureUrl").getValue().toString();
                    mCurrentCharacter = new Character(name, description, age, health, fullnessLevel, characterUrl, characterId);
                    Log.d("Current Character ID: ", mCurrentCharacter.getCharacterId() + "");

                    Gson gson = new Gson();
                    String currentCharacter = gson.toJson(mCurrentCharacter);
                    mEditor.putString(Constants.PREFERENCES_CHARACTER, currentCharacter);
                    mEditor.commit();
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });

            instantiatePlayerCharacters();
        }


    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingConnectionFailure) {
            return;
        }
        if (mSignInCLicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInCLicked = false;
            mResolvingConnectionFailure = true;
        }
        if (!BaseGameUtils.resolveConnectionFailure(this,
                mGoogleApiClient, connectionResult, RC_SIGN_IN, "Sign in error!")) {
            mResolvingConnectionFailure = false;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                mSignInCLicked = true;
                mGoogleApiClient.reconnect();
                signInButton.setVisibility(View.GONE);
                signOutButton.setVisibility(View.VISIBLE);
                break;
            case R.id.sign_out_button:
                mExplicitSignOut = true;
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    Games.signOut(mGoogleApiClient);
                    mGoogleApiClient.disconnect();
                }
                mSignInCLicked = false;
                signInButton.setVisibility(View.VISIBLE);
                signOutButton.setVisibility(View.GONE);
                break;
            case R.id.findPlayersButton:
                findPlayers();
                break;
            case R.id.endMatchButton:
                endMatch();
                break;
            case R.id.testButton:
                testMethod();
                break;
            case R.id.characterButton:
                Intent intent  = new Intent(mContext, CharacterDetailActivity.class);
                intent.putExtra("position", 0);
                intent.putExtra("characters", Parcels.wrap(mCharacters));
                mContext.startActivity(intent);
                break;
            case R.id.testButton2:
                takeTurn();
        }
    }

    public void initializeGoogleApi() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API)
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

    public void saveSafehouse() {
        Firebase safehouseFirebaseRef = new Firebase(Constants.FIREBASE_URL_SAFEHOUSES + "/" + mNextSafeHouseId +"/");
        safehouseFirebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String houseName = dataSnapshot.child("houseName").getValue().toString();
                String description = dataSnapshot.child("description").getValue().toString();
                int stepsRequired = Integer.parseInt(dataSnapshot.child("stepsRequired").getValue().toString());

                // Build the next safehouse object and save it to shared preferences
                SafeHouse nextSafeHouse = new SafeHouse(mNextSafeHouseId, houseName, description, stepsRequired);
                Gson gson = new Gson();
                String nextSafehouseJson = gson.toJson(nextSafeHouse);
                mEditor.putString("nextSafehouse", nextSafehouseJson);
                mEditor.commit();
                String safehouseJson = mSharedPreferences.getString("nextSafehouse", null);
                Gson safehouseGson = new Gson();
                mNextSafehouse = safehouseGson.fromJson(safehouseJson, SafeHouse.class);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });
    }

    public void testMethod() {
        Intent intent = Games.TurnBasedMultiplayer.getInboxIntent(mGoogleApiClient);
        startActivityForResult(intent, RC_WAITING_ROOM);
    }

    public void loadMatch() {
        mCurrentMatchId = mSharedPreferences.getString("matchId", null);

        ResultCallback<TurnBasedMultiplayer.LoadMatchResult> loadMatchResultResultCallback = new ResultCallback<TurnBasedMultiplayer.LoadMatchResult>() {
            @Override
            public void onResult(TurnBasedMultiplayer.LoadMatchResult result) {
                mCurrentMatch = result.getMatch();
                if (mCurrentMatch != null) {
                    if (mCurrentMatch != null) {
                        ArrayList<String> participantNames = new ArrayList<>();
                        ArrayList<String> participantIds = mCurrentMatch.getParticipantIds();
                        for (int i = 0; i < participantIds.size(); i++) {
                            participantNames.add(mCurrentMatch.getParticipant(participantIds.get(i)).getDisplayName());
                        }
                        playersTextView.setText(participantNames.toString());
                    }
                }
            }
        };

        if (mCurrentMatchId != null) {
            Games.TurnBasedMultiplayer
                    .loadMatch(mGoogleApiClient, mCurrentMatchId)
                    .setResultCallback(loadMatchResultResultCallback);

            matchIdTextView.setText(mCurrentMatchId);
        }
    }

    public void joinMatch() {
        if (mCurrentMatch == null) {
            // TODO: Get rid of this select current match intent!
            Intent joinMatchIntent = Games.TurnBasedMultiplayer.getInboxIntent(mGoogleApiClient);
            startActivityForResult(joinMatchIntent, RC_WAITING_ROOM);
            Games.TurnBasedMultiplayer
                    .registerMatchUpdateListener(mGoogleApiClient, new MatchUpdateListener());
        } else {
            Toast.makeText(this, "You are already connected to match " + mCurrentMatchId, Toast.LENGTH_LONG).show();
        }
    }

    public void findPlayers() {
        if (mGoogleApiClient.isConnected() && mCurrentMatch == null) {
            final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 4;
            Intent intent = Games.TurnBasedMultiplayer.getSelectOpponentsIntent(mGoogleApiClient, MIN_OPPONENTS, MAX_OPPONENTS, true);
            startActivityForResult(intent, RC_SELECT_PLAYERS);
        } else if (mCurrentMatch == null) {
            Toast.makeText(this, "Sign in to find players", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "One match at a time!", Toast.LENGTH_LONG).show();
        }
    }

    public void endMatch() {
        ResultCallback<TurnBasedMultiplayer.CancelMatchResult> cancelMatchResultResultCallback = new ResultCallback<TurnBasedMultiplayer.CancelMatchResult>() {
            @Override
            public void onResult(TurnBasedMultiplayer.CancelMatchResult result) {
                Toast.makeText(MainActivity.this, "You Killed The Match!", Toast.LENGTH_SHORT).show();
            }
        };

        if (mGoogleApiClient.isConnected() && mCurrentMatch != null) {
            Games.TurnBasedMultiplayer
                    .dismissMatch(mGoogleApiClient, mCurrentMatchId);
            Games.TurnBasedMultiplayer
                    .cancelMatch(mGoogleApiClient, mCurrentMatchId)
                    .setResultCallback(cancelMatchResultResultCallback);
        } else {
            Toast.makeText(this, "Not connected to match", Toast.LENGTH_SHORT).show();
        }

        mEditor.putString("matchId", null);
        mEditor.commit();
        mCurrentMatch = null;
        mCurrentMatchId = null;
        matchIdTextView.setText("");
        playersTextView.setText("");
    }

    public void invitationReceived(Invitation invitation) {
        if (mGoogleApiClient != null) {
            Games.TurnBasedMultiplayer.acceptInvitation(mGoogleApiClient, invitation.getInvitationId());
        }
    }

    public void takeTurn() {
        try {
            turnData = mCurrentMatch.getData();
        } catch (NullPointerException nullPointer) {
            nullPointer.getStackTrace();
            Log.v(TAG, "Take first turn without turnData");
        }

        if (turnData == null) {
            mCurrentMatchId = mCurrentMatch.getMatchId();
            String creatorId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
            ArrayList<String> wholeParty = invitees;
            if (wholeParty != null) {
                wholeParty.add(creatorId);
            }

            mEditor.putString("matchId", mCurrentMatchId);
            mEditor.putInt("lastSafehouseId", 0);
            mEditor.putInt("nextSafehouseId", 1);
            mEditor.commit();

            Firebase teamFirebaseRef = new Firebase(Constants.FIREBASE_URL_TEAM + "/" + "")
                    .child(mCurrentMatchId);
            teamFirebaseRef.child("matchStart").setValue(mCurrentMatch.getCreationTimestamp());
            teamFirebaseRef.child("matchDuration").setValue(mMatchDuration);
            teamFirebaseRef.child("lastSafehouseId").setValue(0);
            teamFirebaseRef.child("nextSafehouseId").setValue(1);
            Firebase playerFirebase = teamFirebaseRef.child("players");
            if (wholeParty != null) {
                for (int i = 0; i < wholeParty.size(); i++) {
                    playerFirebase
                            .child("p_" + (i + 1))
                            .setValue(wholeParty.get(i));
                }
            }

            mUserFirebaseRef.child("teamId").setValue(mCurrentMatchId);
            matchIdTextView.setText(mCurrentMatchId);
            createCampaign(15);
            saveSafehouse();
            turnData = new byte[1];
            //Take as many turns as there are players, to invite all players at once
            for (int i = 0; i < mCurrentMatch.getParticipantIds().size(); i++) {
                String nextPlayer = mCurrentMatch.getParticipantIds().get(i);
                Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, mCurrentMatchId, turnData, nextPlayer);
            }
            assignRandomCharacters();
        }
        turnData = new byte[1];

        ArrayList<Participant> allPlayers = mCurrentMatch.getParticipants();
        int nextPlayerNumber = Integer.parseInt(mCurrentMatch.getLastUpdaterId().substring(2));
        try {
            //Should pass invitation to the next player
            String nextPlayerId = allPlayers.get(nextPlayerNumber).getParticipantId();
            Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, mCurrentMatchId, turnData, nextPlayerId);

            //Grab the next player in case the previous above didn't work
            nextPlayerId = allPlayers.get(nextPlayerNumber + 1).getParticipantId();
            Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, mCurrentMatchId, turnData, nextPlayerId);
        } catch (IndexOutOfBoundsException indexOutOfBonds) {
            Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, mCurrentMatchId, turnData, mCurrentMatch.getPendingParticipantId());
        }
        Games.TurnBasedMultiplayer.registerMatchUpdateListener(mGoogleApiClient, new MatchUpdateListener());
    }

    @Override
    public void onActivityResult (final int request, int response, Intent data){
        super.onActivityResult(request, response, data);

        if (response != Activity.RESULT_OK) {
            //user canceled or something went wrong
            return;
        }

        if (request == RC_SELECT_PLAYERS) {
            //user returning from select players

            joinMatch();

            //TODO: implement automatically selecting the match which was just created.
            invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
            playersTextView.setText(invitees.toString());

            Bundle automatchCriteria = null;
            //checking if user chose auto match opponents
            int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
            int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

            if (minAutoMatchPlayers > 0) {
                automatchCriteria = RoomConfig.createAutoMatchCriteria(minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            } else {
                automatchCriteria = null;
            }
            //Match configuration
            TurnBasedMatchConfig turnBasedMatchConfig = TurnBasedMatchConfig.builder()
                    .setAutoMatchCriteria(automatchCriteria)
                    .addInvitedPlayers(invitees)
                    .build();

            //Build match
            Games.TurnBasedMultiplayer
                    .createMatch(mGoogleApiClient, turnBasedMatchConfig)
                    .setResultCallback(new MatchInitiatedListener());

            Games.TurnBasedMultiplayer.registerMatchUpdateListener(mGoogleApiClient, new MatchUpdateListener());

        } else if (request == RC_WAITING_ROOM) {
            //user returning from join match
            mCurrentMatch = data.getParcelableExtra(Multiplayer.EXTRA_TURN_BASED_MATCH);
            mCurrentMatchId = mCurrentMatch.getMatchId();

            ArrayList<String> playersId = mCurrentMatch.getParticipantIds();
            mEditor.putString("matchId", mCurrentMatchId);
            mEditor.commit();

            playersTextView.setText(playersId.toString());
            matchIdTextView.setText(mCurrentMatchId);

            takeTurn();
        }

    }

    private void firebaseListening() {
        Firebase firebaseRef = new Firebase(Constants.FIREBASE_URL_TEAM + "/" + "");
        Query queryRef = firebaseRef.orderByValue();

        queryRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });

        queryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {}

            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });
    }

    //CAMPAIGN LOGIC BEGINS HERE

    private void assignRandomCharacters() {
        final Firebase characterSkeletonRef = new Firebase(Constants.FIREBASE_URL+ "/");
        final ArrayList<Character> selectionList = new ArrayList<>();

        characterSkeletonRef.child("characters").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String name = child.child("name").getValue().toString();
                    long ageLong = (long) child.child("age").getValue();
                    int age = (int) ageLong;
                    String description = child.child("description").getValue().toString();
                    long characterIdLong = (long) child.child("characterId").getValue();
                    int characterId = (int) characterIdLong;
                    long healthLong = (long) child.child("health").getValue();
                    int health = (int) healthLong;
                    long fullnessLevelLong = (long) child.child("fullnessLevel").getValue();
                    int fullnessLevel = (int) fullnessLevelLong;
                    String characterUrl = child.child("characterPictureUrl").getValue().toString();
                    Character character = new Character(name, description, age, health, fullnessLevel, characterUrl, characterId);
                    selectionList.add(character);

                    Firebase characterFirebaseRef = new Firebase(Constants.FIREBASE_URL_TEAM + "/" + mCurrentMatchId + "/characters");
                    turnData = mCurrentMatch.getData();
                    Collections.shuffle(selectionList);
                    if (turnData == null && invitees != null) {
                        for (int i = 0; i < invitees.size(); i++) {
                            try {
                                Character assignedCharacter = selectionList.get(i);
                                String playerBeingAssignId = invitees.get(i);

                                //save assigned character Ids to firebase
                                characterFirebaseRef.child(playerBeingAssignId)
                                        .setValue((selectionList.get(i).getCharacterId()));

                                Firebase userRef = new Firebase(Constants.FIREBASE_URL_USERS + "/" + playerBeingAssignId + "/");
                                userRef.child("character").setValue(assignedCharacter);

                                //add assigned characters to persistent array for character pages
                            } catch (IndexOutOfBoundsException indexOutOfBounds) {
                                indexOutOfBounds.getStackTrace();
                            }
                        }

                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    private void initializeEventDialogFragments() {
        int eventOneSteps = mSharedPreferences.getInt(Constants.PREFERENCES_EVENT_1_STEPS, -1);
        int eventTwoSteps = mSharedPreferences.getInt(Constants.PREFERENCES_EVENT_2_STEPS, -1);
        int eventThreeSteps = mSharedPreferences.getInt(Constants.PREFERENCES_EVENT_3_STEPS, -1);
        int eventFourSteps = mSharedPreferences.getInt(Constants.PREFERENCES_EVENT_4_STEPS, -1);
        int eventFiveSteps = mSharedPreferences.getInt(Constants.PREFERENCES_EVENT_5_STEPS, -1);

        if ((eventOneSteps > -1) && (eventOneSteps <= dailySteps) && !(eventOneInitiated)) {
            eventOneInitiated = true;
            mEditor.putBoolean(Constants.PREFERENCES_INITIATE_EVENT_1, true).apply();
            Log.v("Event One:", "Initiated");
            showEventDialog(1);
        }

        if ((eventTwoSteps > -1) && (eventTwoSteps <= dailySteps) && !(eventTwoInitiated)) {
            eventTwoInitiated = true;
            mEditor.putBoolean(Constants.PREFERENCES_INITIATE_EVENT_2, true).apply();
            Log.v("Event Two:", "Initiated");
            showEventDialog(1);

        }

        if ((eventThreeSteps > -1) && (eventThreeSteps <= dailySteps) && !(eventThreeInitiated)) {
            eventThreeInitiated = true;
            mEditor.putBoolean(Constants.PREFERENCES_INITIATE_EVENT_3, true).apply();
            Log.v("Event Three:", "Initiated");
            showEventDialog(1);
        }

        if ((eventFourSteps > -1) && (eventFourSteps <= dailySteps) && !(eventFourInitiated)) {
            eventFourInitiated = true;
            mEditor.putBoolean(Constants.PREFERENCES_INITIATE_EVENT_4, true).apply();
            Log.v("Event Four:", "Initiated");
            showEventDialog(1);
        }

        if ((eventFiveSteps > -1) && (eventFiveSteps <= dailySteps) && !(eventFiveInitiated)) {
            eventFiveInitiated = true;
            mEditor.putBoolean(Constants.PREFERENCES_INITIATE_EVENT_5, true).apply();
            Log.v("Event Five:", "Initiated");
            showEventDialog(1);
        }

    }

    public void showEventDialog(int type) {
        mStackLevel++;
        if (type==1) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment prev = getSupportFragmentManager().findFragmentByTag("event");
            if(prev != null) {
                ft.remove(prev);
            }

            ft.addToBackStack(null);

            DialogFragment frag = EventDialogFragment.newInstance(mStackLevel);
            frag.show(ft, "fragment_event_dialog");
        } else if (type==2) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment prev = getSupportFragmentManager().findFragmentByTag("safehouse");
            if(prev != null) {
                ft.remove(prev);
            }

            ft.addToBackStack(null);

            DialogFragment frag = SafehouseDialogFragment.newInstance(mStackLevel, mPriorSafehouse);
            frag.show(ft, "fragment_safehouse_dialog");
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(Constants.PREFERENCES_STEPS_IN_SENSOR_KEY) && (mCurrentMatch != null)) {
            stepsInSensor = mSharedPreferences.getInt(Constants.PREFERENCES_STEPS_IN_SENSOR_KEY, 0);
            dailySteps = mSharedPreferences.getInt(Constants.PREFERENCES_DAILY_STEPS, 0);
            dailyCounter.setText(Integer.toString(dailySteps));
            counter.setText(Integer.toString(stepsInSensor));
            initializeEventDialogFragments();
            checkSafehouseDistance();
        }

        //TODO: Add listener for isCampaignEnded boolean to trigger end of game screen
    }

    //TODO: Move most checkSafehouseDistance to BackgroundStepService EXCEPT Dialog triggers
    public void checkSafehouseDistance() {
        //pull next safehouse object from shared preferences
//        if(mNextSafehouse.reachedSafehouse(dailySteps))
//        {
//            mPriorSafehouse = mNextSafehouse;
//            mLastSafeHouseId = mNextSafehouse.getHouseId();
//            mNextSafeHouseId = mLastSafeHouseId + 1;
//            mEditor.putInt("lastSafehouseId", mLastSafeHouseId);
//            mEditor.putInt("nextSafehouseId", mNextSafeHouseId);
//            mEditor.commit();
//            safehouseTextView.setText(Integer.toString(mNextSafeHouseId));
//            saveSafehouse();
//            showEventDialog(2);
//        }
    }

    public void initiateDailyCountResetService() {
        Intent intent = new Intent(this, StepResetAlarmReceiver.class);
        //Sets a recurring alarm just before midnight daily to trigger BroadcastReceiver
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        PendingIntent pi = PendingIntent.getBroadcast(this, StepResetAlarmReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
    }

    public void createCampaign(int campaignLength) {
        Calendar campaignCalendar = Calendar.getInstance();
        campaignCalendar.set(Calendar.HOUR, 18);
        campaignCalendar.add(Calendar.DATE, campaignLength);
        Intent intent = new Intent(this, CampaignEndAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, CampaignEndAlarmReceiver.REQUEST_CODE, intent, 0);
        AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, campaignCalendar.getTimeInMillis(), pendingIntent);
        Log.d("CreateCampaign", "Campaign Created");


//        mEditor.putInt(Constants.PREFERENCES_PREVIOUS_STEPS_KEY, 0).commit();
//        assignRandomCharacters();
    }

    public void showMessage(final String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle("Walker Tracker").setMessage(message);
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void instantiatePlayerCharacters() {
        final ArrayList<String> playerIDs = new ArrayList<>();
        Firebase playerIDRef = new Firebase(Constants.FIREBASE_URL_TEAM + "/" + mCurrentMatchId +"/players/");
        playerIDRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String playerId = child.getValue().toString();
                    Log.d("Player string", playerId);
                    Firebase teamCharactersRef = new Firebase(Constants.FIREBASE_URL_USERS + "/" + playerId + "/character/");
                    teamCharactersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String name = dataSnapshot.child("name").getValue().toString();
                            Log.d("Adding character...", name);
                            long ageLong = (long) dataSnapshot.child("age").getValue();
                            int age = (int) ageLong;
                            String description = dataSnapshot.child("description").getValue().toString();
                            long characterIdLong = (long) dataSnapshot.child("characterId").getValue();
                            int characterId = (int) characterIdLong;
                            long healthLong = (long) dataSnapshot.child("health").getValue();
                            int health = (int) healthLong;
                            long fullnessLevelLong = (long) dataSnapshot.child("fullnessLevel").getValue();
                            int fullnessLevel = (int) fullnessLevelLong;
                            String characterUrl = dataSnapshot.child("characterPictureUrl").getValue().toString();
                            Log.d("Instantiated char ID", characterId + "");
                            Character character = new Character(name, description, age, health, fullnessLevel, characterUrl, characterId);
                            mCharacters.add(character);
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });



        characterButton.setEnabled(true);
    }
}