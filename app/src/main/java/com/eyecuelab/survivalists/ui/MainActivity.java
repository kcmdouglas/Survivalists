package com.eyecuelab.survivalists.ui;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.eyecuelab.survivalists.Constants;
import com.eyecuelab.survivalists.R;
import com.eyecuelab.survivalists.util.CampaignEndAlarmReceiver;
import com.eyecuelab.survivalists.util.StepResetAlarmReceiver;
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
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.Players;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.example.games.basegameutils.BaseGameUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, SensorEventListener, GoogleApiClient.ConnectionCallbacks,
                   GoogleApiClient.OnConnectionFailedListener, OnInvitationReceivedListener,
                   OnTurnBasedMatchUpdateReceivedListener {

    @Bind(R.id.stepTextView) TextView counter;
    @Bind(R.id.dailyStepsTextView) TextView dailyCounter;
    @Bind(R.id.sign_in_button) SignInButton signInButton;
    @Bind(R.id.sign_out_button) Button signOutButton;
    @Bind(R.id.findPlayersButton) Button findPlayersButton;
    @Bind(R.id.joinMatchButton) Button joinMatchButton;
    @Bind(R.id.playerTextView) TextView playersTextView;
    @Bind(R.id.testButton) Button testButton;
    @Bind(R.id.matchIdTextView) TextView matchIdTextView;

    private int stepsInSensor;
    private int previousDayStepCount;
    private int dailySteps;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private SensorManager mSensorManager;
    private GoogleApiClient mGoogleApiClient;
    private TurnBasedMatch mCurrentMatch;
    private Sensor countSensor;

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

    private Context mContext;

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
        mSharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();


        //Google Play Games client and correlating buttons
        mGoogleApiClient = new GoogleApiClient.Builder(this, this, this)
                .addApi(Games.API)
                .build();
        signInButton.setOnClickListener(this);
        signOutButton.setOnClickListener(this);
        findPlayersButton.setOnClickListener(this);
        joinMatchButton.setOnClickListener(this);
        testButton.setOnClickListener(this);

        //This sets the BroadcastReceiver in this activity so the broadcast sent by StepResetAlarmReceiver BroadcastReceiver is handled properly
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle receivedSteps = intent.getExtras();
                previousDayStepCount = receivedSteps.getInt("resetPreviousDayStep");
                dailySteps = receivedSteps.getInt("resetDailySteps");
            }
        };

        //This registers the receiver--the receiver is never unregistered, which ensures that this will happen daily
        registerReceiver(broadcastReceiver, new IntentFilter("resetBroadcast"));
        dailyCounter.setText(Integer.toString(dailySteps));
        counter.setText(Integer.toString(stepsInSensor));

        //Set Firebase reference
        mFirebaseRef = new Firebase(Constants.FIREBASE_URL);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.reconnect();
        Sensor countSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            mSensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        }
        if (mCurrentMatch != null) {
            matchIdTextView.setText(mCurrentMatch.getMatchId());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        stepsInSensor = (int) event.values[0];
        dailySteps = Math.round(event.values[0] - previousDayStepCount);
        dailyCounter.setText(Integer.toString(dailySteps));
        counter.setText(Integer.toString(stepsInSensor));

        initiateDailyCountResetService();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}


    //GOOGLE GAMES API LOGIC BEGINS HERE
    @Override
    public void onConnected(Bundle connectionHint) {
        signInButton.setVisibility(View.GONE);
        signOutButton.setVisibility(View.VISIBLE);
        Games.Invitations.registerInvitationListener(mGoogleApiClient, this);
        Games.TurnBasedMultiplayer.registerMatchUpdateListener(mGoogleApiClient, this);
        if (mCurrentMatch != null) {
            Games.TurnBasedMultiplayer
                    .loadMatch(mGoogleApiClient, mCurrentMatch.getMatchId());


        }

//        load current match
        if (connectionHint != null) {
            mCurrentMatch = connectionHint.getParcelable(Multiplayer.EXTRA_TURN_BASED_MATCH);
        }
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
        } else if (view == joinMatchButton) {
            joinMatch();
        } else if (view == testButton) {
            testMethod();
        }
    }

    public void joinMatch() {
        Intent invitationIntent = Games.TurnBasedMultiplayer.getInboxIntent(mGoogleApiClient);
        startActivityForResult(invitationIntent, RC_WAITING_ROOM);
    }

    public void findPlayers() {
        if (mGoogleApiClient.isConnected()) {
            final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 2;
            Intent intent = Games.TurnBasedMultiplayer.getSelectOpponentsIntent(mGoogleApiClient, MIN_OPPONENTS, MAX_OPPONENTS, true);
            startActivityForResult(intent, RC_SELECT_PLAYERS);
        } else {
            Toast.makeText(this, "Sign in to find players", Toast.LENGTH_LONG).show();
        }
    }

    public void testMethod() {
        if (mCurrentMatch != null) {
            Toast.makeText(this, "Testing...", Toast.LENGTH_LONG).show();
            byte[] matchData = new byte[1234];
            String nextPlayer;

            if (mCurrentMatch.getLastUpdaterId().equals("p_1")) {
                nextPlayer = "p_2";
            } else {
                nextPlayer = "p_1";
            }

//            OnInvitationReceivedListener onInvitationReceivedListener = new OnInvitationReceivedListener() {
//                @Override
//                public void onInvitationReceived(Invitation invitation) {
//                    Toast.makeText(MainActivity.this, "Received " + invitation.getInviter().getDisplayName(), Toast.LENGTH_LONG).show();
//                }
//
//                @Override
//                public void onInvitationRemoved(String s) {
//
//                }
//            };
//
//            OnTurnBasedMatchUpdateReceivedListener onTurnBasedMatchUpdateReceivedListener = new OnTurnBasedMatchUpdateReceivedListener() {
//                @Override
//                public void onTurnBasedMatchReceived(TurnBasedMatch turnBasedMatch) {
//                    Toast.makeText(MainActivity.this, "Received " + turnBasedMatch.getLastUpdaterId(), Toast.LENGTH_LONG).show();
//                }
//
//                @Override
//                public void onTurnBasedMatchRemoved(String s) {
//
//                }
//            };
//            Games.Invitations.registerInvitationListener(mGoogleApiClient, onInvitationReceivedListener);
//            Games.TurnBasedMultiplayer.registerMatchUpdateListener(mGoogleApiClient, onTurnBasedMatchUpdateReceivedListener);

            Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, mCurrentMatch.getMatchId(), matchData, nextPlayer);
        } else {
            Toast.makeText(this, "mCurrent match is null", Toast.LENGTH_LONG).show();
        }
    }

    public void takeFirstTurn() {
        byte[] matchData = new byte[1234];
        String nextPlayer;

        if (mCurrentMatch.getLastUpdaterId().equals("p_1")) {
            nextPlayer = "p_2";
        } else {
            nextPlayer = "p_1";
        }
        Games.TurnBasedMultiplayer.takeTurn(mGoogleApiClient, mCurrentMatch.getMatchId(), matchData, nextPlayer);
    }

    @Override
    public void onInvitationReceived(Invitation invitation) {
        Toast.makeText(this, "You have been invited to join " + invitation.getInviter().getDisplayName(), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onInvitationRemoved(String s) {}

    @Override
    public void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);

        if (response != Activity.RESULT_OK) {
//            user canceled or something went wrong
            return;
        }

        if (request == RC_SELECT_PLAYERS) {
//            user returning from select players
            final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
            playersTextView.setText(invitees.toString());

            Bundle automatchCriteria = null;

            int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
            int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

            if (minAutoMatchPlayers > 0) {
                automatchCriteria = RoomConfig.createAutoMatchCriteria(minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            } else {
                automatchCriteria = null;
            }

            TurnBasedMatchConfig turnBasedMatchConfig = TurnBasedMatchConfig.builder()
                    .addInvitedPlayers(invitees)
                    .setAutoMatchCriteria(automatchCriteria)
                    .build();

            Games.TurnBasedMultiplayer
                    .createMatch(mGoogleApiClient, turnBasedMatchConfig)
                    .setResultCallback(new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {
                        @Override
                        public void onResult(@NonNull TurnBasedMultiplayer.InitiateMatchResult initiateMatchResult) {
                            processResult(initiateMatchResult);
                            takeFirstTurn();
                        }
                    });

        } else if (request == RC_WAITING_ROOM) {
//            user returning from join match
            mCurrentMatch = data.getParcelableExtra(Multiplayer.EXTRA_TURN_BASED_MATCH);
            String playersId = mCurrentMatch.getPendingParticipantId();

            Log.d("Creator Id", mCurrentMatch.getCreatorId());
            Log.d("Participant Ids", mCurrentMatch.getParticipantIds().toString());
            Log.d("Pending participant Id", mCurrentMatch.getPendingParticipantId());

            playersTextView.setText(playersId);
        }

    }


    private void processResult(TurnBasedMultiplayer.CancelMatchResult result) {
        String matchId = result.getMatchId();
        Toast.makeText(this, "This match is canceled: " + matchId, Toast.LENGTH_LONG).show();
    }

    private void processResult(TurnBasedMultiplayer.InitiateMatchResult result) {
        TurnBasedMatch match = result.getMatch();

//        Verifying if new match:
        if (match.getData() == null) {
            String matchId = match.getMatchId();
            String googlePlayerId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
            String matchPlayerId = match.getParticipantId(googlePlayerId);
            ArrayList<String> playerIds = match.getParticipantIds();

            Firebase firebaseRef = new Firebase(Constants.FIREBASE_URL_TEAM + "/" + "");
            firebaseListening();
            firebaseRef.child(matchId).setValue(playerIds);

            Log.d("Google Player Id", googlePlayerId);
            Log.d("Match Player Id", matchPlayerId);
        }
    }

    private void firebaseListening() {
        Firebase firebaseRef = new Firebase(Constants.FIREBASE_URL_TEAM + "/" + "");
        Query queryRef = firebaseRef.orderByValue();

        queryRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
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

    @Override
    public void onTurnBasedMatchReceived(TurnBasedMatch turnBasedMatch) {
        Toast.makeText(this, "Match received: " + turnBasedMatch.getMatchId(), Toast.LENGTH_LONG).show();
    }

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

        //TODO: Send team information to the database
        //Saves team data to Firebase
        Firebase firebaseTeamRef = new Firebase(Constants.FIREBASE_URL_TEAM);
        firebaseTeamRef.setValue(mCurrentMatch.toString());

        //TODO: Send user information to the database
//        //Nest user information within the Team child
//        Map<String, Object> userID = new HashMap<String, Object>();
//
//        Map<String, Object> userIDs = new HashMap<String, Object>();
//
//
//        userIDs.put("user/", mCurrentMatch.getCreatorId());
//
//        ArrayList<String> participants = mCurrentMatch.getParticipantIds();
//
//        for(int i = 0; i < participants.size(); i++) {
//            String participant = participants.get(i);
//            userIDs.put("user/", participant);
//        }
//
//        firebaseTeamRef.child(mCurrentMatch.toString()).updateChildren(userIDs);
        //TODO: Create endCampaign method

    }

    //Sets alarm for daily step count
    public void initiateDailyCountResetService() {
        //Bundles the number of steps in the sensor
        Intent intent = new Intent(getBaseContext(), StepResetAlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putInt("endOfDaySteps", stepsInSensor);

        intent.putExtras(bundle);
        //Sets a recurring alarm just before midnight daily to trigger BroadcastReceiver
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        PendingIntent pi = PendingIntent.getBroadcast(getBaseContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);

    }
}