package com.eyecuelab.survivalists.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.preference.PreferenceActivity;
import android.support.v4.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.eyecuelab.survivalists.Constants;
import com.eyecuelab.survivalists.R;
import com.eyecuelab.survivalists.models.Character;
import com.eyecuelab.survivalists.models.User;
import com.eyecuelab.survivalists.models.SafeHouse;
import com.eyecuelab.survivalists.services.BackgroundStepService;
import com.eyecuelab.survivalists.services.GooglePlayGamesService;
import com.eyecuelab.survivalists.util.CampaignEndAlarmReceiver;
import com.eyecuelab.survivalists.util.InvitationListener;
import com.eyecuelab.survivalists.util.MatchInitiatedListener;
import com.eyecuelab.survivalists.util.MatchUpdateListener;
import com.eyecuelab.survivalists.util.StepResetAlarmReceiver;
import com.eyecuelab.survivalists.util.StepResetResultReceiver;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;

import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.google.gson.Gson;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private int previousDayStepCount;
    private int dailySteps;
    private String mCurrentMatchId;
    private String mMatchDuraution;
    private int mLastSafeHouseId;
    private int mNextSafeHouseId;
    private ArrayList<String> invitees;
    private byte[] turnData;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private SensorManager mSensorManager;
    private GoogleApiClient mGoogleApiClient;
    private TurnBasedMatch mCurrentMatch;
    private String mCurrentPlayerId;
    private Sensor countSensor;
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
    private Firebase mFirebaseRef;
    private Firebase mUserFirebaseRef;
    private PreferenceActivity mPreferenceActivity;


    private Context mContext;
    private StepResetResultReceiver mReceiver;

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
    ArrayList<Character> mCharacters;

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

        //Initialize Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        //set content view AFTER ABOVE sequence (to avoid crash)
        setContentView(R.layout.activity_main);
        mContext = this;
        ButterKnife.bind(this);

        //Initialize SensorManager
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //Create Shared Preferences
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mSharedPreferences.edit();


        //Google Play Games client and correlating buttons
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API)
                .build();

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

        mCharacters = new ArrayList<>();
        Character characterA = new Character("characterA", 22, 100, null, 0);
        Character characterB = new Character("characterB", 80, 100, null, 1);
        Character characterC = new Character("characterC", 44, 100, null, 2);
        Character characterD = new Character("characterD", 120, 100, null, 3);
        mCharacters.add(characterA);
        mCharacters.add(characterB);
        mCharacters.add(characterC);
        mCharacters.add(characterD);

        mNextSafeHouseId = mSharedPreferences.getInt(Constants.PREFERENCES_NEXT_SAFEHOUSE_ID, 1);
        mLastSafeHouseId = mSharedPreferences.getInt(Constants.PREFERENCES_LAST_SAFEHOUSE_ID, 0);

        String safehouseJson = mSharedPreferences.getString("nextSafehouse", null);
        Gson gson = new Gson();
        mNextSafehouse = gson.fromJson(safehouseJson, SafeHouse.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.reconnect();
        if (mCurrentMatch != null) {
            matchIdTextView.setText(mCurrentMatch.getMatchId());
        }
        if(mCurrentPlayerId != null) {
        }


        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

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

        userIdTextView.setText(mCurrentPlayerId);
        String greeting = "Hello " + userName;
        userNameTextView.setText(greeting);

        //Save user info to firebase
        mUserFirebaseRef = new Firebase(Constants.FIREBASE_URL_USERS + "/" + mCurrentPlayerId);
        mUserFirebaseRef.child("displayName")
                .setValue(userName);
        mUserFirebaseRef.child("atSafeHouse")
                .setValue(false);

        if (mCurrentMatch == null) {
            mUserFirebaseRef.child("joinedMatch")
                    .setValue(false);
        } else {
            mUserFirebaseRef.child("joinedMatch")
                    .setValue(true);

//            Firebase teamFirebaseRef = new Firebase(Constants.FIREBASE_URL_TEAM + "/" + mCurrentMatchId +"");
//            teamFirebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    int lastSafehouseId = Integer.valueOf(dataSnapshot.child("lastSafehouseId").getValue().toString());
//                    int nextSafehouseId = Integer.valueOf(dataSnapshot.child("nextSafehouseId").getValue().toString());
//                    mEditor.putInt(Constants.PREFERENCES_LAST_SAFEHOUSE_ID, lastSafehouseId);
//
//                    safehouseTextView.setText(mNextSafeHouseId);
//                }
//
//                @Override
//                public void onCancelled(FirebaseError firebaseError) {}
//            });
        }
        firebaseListening();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Connection suspended, reconnecting", Toast.LENGTH_LONG).show();
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
                takeFirstTurn();
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
            public void onCancelled(FirebaseError firebaseError) {

            }
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
                    ArrayList<String> playersId = mCurrentMatch.getParticipantIds();
                    if (mCurrentMatch != null) {
                        playersTextView.setText(mCurrentMatch.getParticipant(playersId.get(1)).getDisplayName());
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
                mEditor.putString("matchId", "Please create match");
                mEditor.commit();
                mCurrentMatch = null;
                matchIdTextView.setText("");
                playersTextView.setText("");
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
    }

    public void takeFirstTurn() {
        String nextPlayer;
        turnData = mCurrentMatch.getData();

        if (turnData == null) {
            turnData = new byte[1];
            mCurrentMatchId = mCurrentMatch.getMatchId();
            String creatorId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
            ArrayList<String> wholeParty = invitees;
            if (wholeParty != null) {
                wholeParty.add(creatorId);
            }

            //Take as many turns as there are players to invite all players at once
            for (int i = 0; i < mCurrentMatch.getParticipantIds().size(); i++) {
                nextPlayer = mCurrentMatch.getParticipantIds().get(i);
                Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, mCurrentMatchId, turnData, nextPlayer);
            }

            mEditor.putString("matchId", mCurrentMatchId);
            mEditor.putInt("lastSafehouseId", 0);
            mEditor.putInt("nextSafehouseId", 1);
            mEditor.commit();

            Firebase teamFirebaseRef = new Firebase(Constants.FIREBASE_URL_TEAM + "/" + "")
                    .child(mCurrentMatchId);
            teamFirebaseRef.child("matchStart")
                    .setValue(mCurrentMatch.getCreationTimestamp());
            teamFirebaseRef.child("matchDuration")
                    .setValue(mMatchDuraution);
            teamFirebaseRef.child("lastSafehouseId")
                    .setValue(0);
            teamFirebaseRef.child("nextSafehouseId")
                    .setValue(1);
            Firebase playerFirebase = teamFirebaseRef
                    .child("players");

            if (wholeParty != null) {
                for (int i = 0; i < wholeParty.size(); i++) {
                    playerFirebase
                            .child("p_" + (i + 1))
                            .setValue(wholeParty.get(i));
                }
            }

            mUserFirebaseRef.child("teamId").setValue(mCurrentMatchId);
            matchIdTextView.setText(mCurrentMatchId);
            createCampaign();
            saveSafehouse();
        }
    }

    @Override
    public void onActivityResult(final int request, int response, Intent data) {
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
                    .setAutoMatchCriteria(automatchCriteria)
                    .build();

            //Build match
            Games.TurnBasedMultiplayer
                    .createMatch(mGoogleApiClient, turnBasedMatchConfig)
                    .setResultCallback(new MatchInitiatedListener());

            Games.TurnBasedMultiplayer.registerMatchUpdateListener(mGoogleApiClient, new MatchUpdateListener());

        } else if (request == RC_WAITING_ROOM) {
            //user returning from join match
            mCurrentMatch = data.getParcelableExtra(Multiplayer.EXTRA_TURN_BASED_MATCH);
            ArrayList<String> playersId = mCurrentMatch.getParticipantIds();

            if (mCurrentMatch != null) {
                playersTextView.setText(mCurrentMatch.getParticipant(playersId.get(1)).getDisplayName());
            }
            takeFirstTurn();
        }

    }

    private void processResult(TurnBasedMultiplayer.InitiateMatchResult result) {
        Toast.makeText(MainActivity.this, "YAY!", Toast.LENGTH_LONG).show();
        mCurrentMatch = result.getMatch();
        mCurrentMatchId = mCurrentMatch.getMatchId();
        takeFirstTurn();
    }

    private void firebaseListening() {
        Firebase firebaseRef = new Firebase(Constants.FIREBASE_URL_TEAM + "/" + "");
        Query queryRef = firebaseRef.orderByValue();

        queryRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("Firebase Update", "MatchId: " + dataSnapshot.getKey());
                Log.d("Firebase Update", "MatchInfo: " + dataSnapshot.getValue());
            }

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
    public void createCampaign() {
        //TODO: Set alarm for x Days
        //Set the Campaign Length here: (Default is 6pm on the 15th day after campaign begins)
        Calendar campaignCalendar = Calendar.getInstance();
        campaignCalendar.set(Calendar.DATE, 15);
        campaignCalendar.set(Calendar.HOUR_OF_DAY, 18);

        Intent intent = new Intent(this, CampaignEndAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), 1, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, campaignCalendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, campaignCalendar.getTimeInMillis(), pendingIntent);
        }
        //TODO: Create endCampaign method

        mEditor.putInt(Constants.PREFERENCES_PREVIOUS_STEPS_KEY, 0).commit();
        assignRandomCharacters();
    }

    private void assignRandomCharacters() {
        ArrayList<Character> remainingCharacters;
        turnData = mCurrentMatch.getData();
        Firebase characterFirebaseRef = new Firebase(Constants.FIREBASE_URL_TEAM + "/" + mCurrentMatchId + "/characters");

        if (turnData == null) {
            //User has initiated the match/ assign random # between 0 - 3
            int randomNumber = (int) Math.round(Math.random() * 3);

            //Assign random character and save to firebase
            mCurrentCharacter = mCharacters.get(randomNumber);
            characterFirebaseRef.child(mCurrentPlayerId).setValue(mCurrentCharacter.getCharacterId());

            remainingCharacters = mCharacters;
            remainingCharacters.remove(randomNumber);
            int unasignedPlayersCount = invitees.size();

            while (unasignedPlayersCount > 0) {
                for (int i = 0; i < unasignedPlayersCount; i++) {
                    randomNumber = (int) Math.round(Math.random() * invitees.size() - 1);

                    Log.v(TAG, randomNumber + "");
                    Log.v(TAG, unasignedPlayersCount + "");

                    Character assignedCharacter = remainingCharacters.get(randomNumber);

                    String playerBeingAssignId = invitees.get(randomNumber);

                    //save assigned character Ids to firebase
                    Map<String, Object> updateMap = new HashMap<>();
                    updateMap.put(playerBeingAssignId, assignedCharacter.getCharacterId());
                    characterFirebaseRef.updateChildren(updateMap);

                    //remove assigned character and update counter
                    remainingCharacters.remove(assignedCharacter);
                    unasignedPlayersCount -= 1;
                }
            }

        } else {
            //Someone else started the match/ pull character from firebase
//            characterFirebaseRef.child(mCurrentPlayerId).addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    mCurrentCharacter = mCharacters.get(Integer.parseInt(dataSnapshot.getValue().toString()));
//                }
//
//                @Override
//                public void onCancelled(FirebaseError firebaseError) {}
//            });
        }
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

        if(key.equals(Constants.PREFERENCES_STEPS_IN_SENSOR_KEY)) {
            stepsInSensor = mSharedPreferences.getInt(Constants.PREFERENCES_STEPS_IN_SENSOR_KEY, 0);
            dailySteps = mSharedPreferences.getInt(Constants.PREFERENCES_DAILY_STEPS, 0);
            dailyCounter.setText(Integer.toString(dailySteps));
            counter.setText(Integer.toString(stepsInSensor));
            initializeEventDialogFragments();
            checkSafehouseDistance();
        }

    }

    public void checkSafehouseDistance() {
        //pull next safehouse object from shared preferences
        if(mNextSafehouse.reachedSafehouse(dailySteps))
        {
            mPriorSafehouse = mNextSafehouse;
            mLastSafeHouseId = mNextSafehouse.getHouseId();
            mNextSafeHouseId = mLastSafeHouseId + 1;
            mEditor.putInt("lastSafehouseId", mLastSafeHouseId);
            mEditor.putInt("nextSafehouseId", mNextSafeHouseId);
            mEditor.commit();
            safehouseTextView.setText(Integer.toString(mNextSafeHouseId));
            saveSafehouse();

            showEventDialog(2);
        }
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
}