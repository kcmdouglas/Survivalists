package com.eyecuelab.survivalists.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.eyecuelab.survivalists.Constants;
import com.eyecuelab.survivalists.R;
import com.eyecuelab.survivalists.models.User;
import com.eyecuelab.survivalists.models.SafeHouse;
import com.eyecuelab.survivalists.services.BackgroundStepService;
import com.eyecuelab.survivalists.util.CampaignEndAlarmReceiver;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;

import com.google.android.gms.games.Player;
import com.google.android.gms.games.Players;
import com.google.android.gms.games.internal.GamesContract;
import com.google.android.gms.games.internal.game.Acls;
import com.google.android.gms.games.internal.game.GameInstance;
import com.google.android.gms.games.multiplayer.Invitation;

import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.google.gson.Gson;

import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends FragmentActivity
        implements View.OnClickListener, SensorEventListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnTurnBasedMatchUpdateReceivedListener {

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
    @Bind(R.id.stepEditText) EditText manualStepSetter;
    @Bind(R.id.safehouseTextView) TextView safehouseTextView;

    private int stepsInSensor;
    private int previousDayStepCount;
    private int dailySteps = 10;
    private String mCurrentMatchId;
    private String mMatchDuraution;
    private String mLastSafeHouseId;
    private String mNextSafeHouseId;
    private ArrayList<String> invitees;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private SensorManager mSensorManager;
    private GoogleApiClient mGoogleApiClient;
    private TurnBasedMatch mCurrentMatch;
    private String mCurrentPlayerId;
    private Sensor countSensor;
    private SafeHouse mNextSafehouse;

    private User mCurrentUser;

//    Flags to indicate return activity
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


    private Context mContext;
    private StepResetResultReceiver mReceiver;

    Intent mBackgroundStepServiceIntent;
    private BackgroundStepService mBackgroundStepService;

    //Event Variables
    private int mStackLevel;



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
                .addScope(Games.SCOPE_GAMES)
                .build();

        signInButton.setOnClickListener(this);
        signOutButton.setOnClickListener(this);
        findPlayersButton.setOnClickListener(this);
        endMatchButton.setOnClickListener(this);
        testButton.setOnClickListener(this);

        dailyCounter.setText(Integer.toString(dailySteps));
        counter.setText(Integer.toString(stepsInSensor));
        previousDayStepCount = mSharedPreferences.getInt(Constants.PREFERENCES_PREVIOUS_STEPS_KEY, 0);

        mCurrentMatchId = mSharedPreferences.getString("matchId", null);

        //pull next safehouse object from shared preferences
        String safehouseJson = mSharedPreferences.getString("nextSafehouse", null);
        Gson gson = new Gson();
        mNextSafehouse = gson.fromJson(safehouseJson, SafeHouse.class);

        if (mNextSafehouse != null) {
            Toast.makeText(MainActivity.this, "YAYA! " + mNextSafehouse.getHouseName(), Toast.LENGTH_SHORT).show();
        }

        mMatchDuraution = "30";
        mLastSafeHouseId = "0";
        mNextSafeHouseId = "1";

        //initialize background step service to keep persistent pedometer
        mBackgroundStepService = new BackgroundStepService();
        mBackgroundStepServiceIntent = new Intent(mContext, mBackgroundStepService.getClass());
        if(!isBackgroundStepServiceRunning(mBackgroundStepService.getClass()))
        {
            startService(mBackgroundStepServiceIntent);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.reconnect();
        Sensor countSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            mSensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_GAME);
        }
        if (mCurrentMatch != null) {
            matchIdTextView.setText(mCurrentMatch.getMatchId());
        }
        if(mCurrentPlayerId != null) {
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
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

    //STEP SENSOR LOGIC AND FIREBASE CALLS
    @Override
    public void onSensorChanged(SensorEvent event) {
        previousDayStepCount = mSharedPreferences.getInt(Constants.PREFERENCES_PREVIOUS_STEPS_KEY, 0);
        stepsInSensor = (int) event.values[0];
        if(stepsInSensor < previousDayStepCount) {
            dailySteps =+ stepsInSensor;
        } else {
            dailySteps = Math.round(event.values[0] - previousDayStepCount);
        }
        dailyCounter.setText(Integer.toString(dailySteps));
        counter.setText(Integer.toString(stepsInSensor));
        String dailyStepsString = Integer.toString(dailySteps);

        new Thread(new Runnable() {
            @Override
            public void run() {
            }
        });

        initiateDailyCountResetService();

        if((mCurrentPlayerId != null) && (dailySteps % 10 < 1)) {
            Firebase firebaseStepsRef = new Firebase(Constants.FIREBASE_URL_STEPS + "/" + mCurrentPlayerId + "/");
            Map<String, Object> dailySteps = new HashMap<>();
            dailySteps.put("daily_steps", dailyStepsString);
            firebaseStepsRef.updateChildren(dailySteps);
            firebaseStepListener();
        }

        if ((mCurrentMatchId != null) && (mNextSafehouse.reachedSafehouse(dailySteps))) {
            Toast.makeText(MainActivity.this, "You've reached " + mNextSafehouse.getHouseName(), Toast.LENGTH_SHORT).show();
            Toast.makeText(MainActivity.this, mNextSafehouse.getDescription(), Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void firebaseStepListener() {
        Firebase firebaseStepsRef = new Firebase(Constants.FIREBASE_URL_STEPS + "/" + mCurrentPlayerId);
        Query queryRef = firebaseStepsRef.orderByValue();

        queryRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("Firebase Update", dataSnapshot.getKey());
                Log.d("Firebase Update", dataSnapshot.getValue().toString());
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





    //GOOGLE GAMES API LOGIC BEGINS HERE
    //TODO: Move this logic to a separate service class
    @Override
    public void onConnected(Bundle connectionHint) {
        signInButton.setVisibility(View.GONE);
        signOutButton.setVisibility(View.VISIBLE);
        mCurrentPlayerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
        if (mCurrentMatch != null) {
            Games.TurnBasedMultiplayer
                    .loadMatch(mGoogleApiClient, mCurrentMatch.getMatchId());

            matchIdTextView.setText(mCurrentMatchId);
        }

        //Load current match
        loadMatch();

        Games.TurnBasedMultiplayer.registerMatchUpdateListener(mGoogleApiClient, this);

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
            Firebase teamFirebaseRef = new Firebase(Constants.FIREBASE_URL_TEAM + "/" + mCurrentMatchId);
            teamFirebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mLastSafeHouseId = dataSnapshot.child("lastSafehouseId").getValue().toString();
                    mNextSafeHouseId = dataSnapshot.child("nextSafehouseId").getValue().toString();
                    safehouseTextView.setText(mNextSafeHouseId);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {}
            });
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
        if (view == signInButton) {
            mSignInCLicked = true;
            mGoogleApiClient.reconnect();
            signInButton.setVisibility(View.GONE);
            signOutButton.setVisibility(View.VISIBLE);
        } else if (view == signOutButton) {
            mExplicitSignOut = true;
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                Games.signOut(mGoogleApiClient);
                mGoogleApiClient.disconnect();
            }
            mSignInCLicked = false;
            signInButton.setVisibility(View.VISIBLE);
            signOutButton.setVisibility(View.GONE);
        } else if (view == findPlayersButton) {
            findPlayers();
        } else if (view == endMatchButton) {
            endMatch();
        } else if (view == testButton) {
            testMethod();
        }
    }

    public void saveSafehouse() {
        Firebase safehouseFirebaseRef = new Firebase(Constants.FIREBASE_URL_SAFEHOUSES + "/" + mNextSafeHouseId);
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
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void testMethod() {
//        String stepsInThings = manualStepSetter.getText().toString();
//        if (stepsInThings != "") {
//            dailySteps = Integer.parseInt(stepsInThings);
//        }
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
                    .registerMatchUpdateListener(mGoogleApiClient, MainActivity.this);
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
        byte[] matchData = new byte[1];
        String nextPlayer;

        mCurrentMatchId = mCurrentMatch.getMatchId();
        String creatorId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
        ArrayList<String> wholeParty = invitees;
        if (wholeParty != null) {
            wholeParty.add(creatorId);
        }

        if (mCurrentMatch.getLastUpdaterId().equals("p_1")) {
            nextPlayer = "p_2";
        } else {
            nextPlayer = "p_1";
        }

        Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, mCurrentMatchId, matchData, nextPlayer);

        mEditor.putString("matchId", mCurrentMatchId);
        mEditor.commit();

        Firebase teamFirebaseRef = new Firebase(Constants.FIREBASE_URL_TEAM + "/" + "")
                .child(mCurrentMatchId);
        teamFirebaseRef.child("matchStart")
                .setValue(mCurrentMatch.getCreationTimestamp());
        teamFirebaseRef.child("matchDuration")
                .setValue(mMatchDuraution);
        teamFirebaseRef.child("lastSafehouseId")
                .setValue(mLastSafeHouseId);
        teamFirebaseRef.child("nextSafehouseId")
                .setValue(mNextSafeHouseId);
        Firebase playerFirebase = teamFirebaseRef
                .child("players");
        for (int i = 0; i < wholeParty.size(); i++) {
            playerFirebase
                    .child("p_" + (i + 1))
                    .setValue(wholeParty.get(i));
        }
        firebaseListening();

        mUserFirebaseRef.child("teamId").setValue(mCurrentMatchId);
        matchIdTextView.setText(mCurrentMatchId);
        saveSafehouse();
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
            //Create a callback when the match is initiated
            ResultCallback<TurnBasedMultiplayer.InitiateMatchResult> initiateMatchResultResultCallback = new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
                @Override
                public void onResult(TurnBasedMultiplayer.InitiateMatchResult result) {
                    processResult(result);
                }
            };
            //Build match
            Games.TurnBasedMultiplayer
                    .createMatch(mGoogleApiClient, turnBasedMatchConfig)
                    .setResultCallback(initiateMatchResultResultCallback);

            Games.TurnBasedMultiplayer.registerMatchUpdateListener(mGoogleApiClient, MainActivity.this);

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
                    Log.d("Firebase Update", "New Match: " + dataSnapshot.getKey());
                    Log.d("Firebase Update", "Players: " + dataSnapshot.getValue());
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

    @Override
    public void onTurnBasedMatchReceived(TurnBasedMatch turnBasedMatch) {}

    @Override
    public void onTurnBasedMatchRemoved(String s) {}


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


        previousDayStepCount = 0;

    }

    //Sets alarm for daily step count
    public void initiateDailyCountResetService() {
        //Bundles the number of steps in the sensor
        Intent intent = new Intent(this, StepResetAlarmReceiver.class);
        Bundle bundle = new Bundle();
        intent.putExtra("receiver", mReceiver);
        bundle.putInt("endOfDaySteps", stepsInSensor);
        bundle.putInt("dailySteps", dailySteps);
        bundle.putString("currentPlayerID", mCurrentPlayerId);

        intent.putExtras(bundle);
        //Sets a recurring alarm just before midnight daily to trigger BroadcastReceiver
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        PendingIntent pi = PendingIntent.getBroadcast(this, StepResetAlarmReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);

    }

    public void showEventDialog() {
        mStackLevel++;

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("event");
        if(prev != null) {
            ft.remove(prev);
        }

        ft.addToBackStack(null);

        DialogFragment frag = EventDialogFragment.newInstance(mStackLevel);
        frag.show(ft, "fragment_event_dialog");
    }
}