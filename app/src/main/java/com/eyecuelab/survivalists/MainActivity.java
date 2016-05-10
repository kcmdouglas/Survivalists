package com.eyecuelab.survivalists;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvingResultCallbacks;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.internal.api.TurnBasedMultiplayerImpl;
import com.google.android.gms.games.internal.constants.MatchResult;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.example.games.basegameutils.BaseGameUtils;

import java.util.ArrayList;

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

    private int stepsInSensor = 0;
    private int dailySteps;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private SensorManager sensorManager;
    private GoogleApiClient mGoogleApiClient;

    private static final int RC_SIGN_IN = 9001;
    private static final int RC_SELECT_PLAYERS = 101;
    private static final int RC_WAITING_ROOM = 10002;

    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInCLicked = false;
    private boolean mExplicitSignOut = false;
    private boolean mInSignInFlow = false;

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

        //set content view AFTER ABOVE sequence (to avoid crash)
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API)
                .build();

        mSharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();

        ButterKnife.bind(this);

        signInButton.setOnClickListener(this);
        signOutButton.setOnClickListener(this);
        findPlayersButton.setOnClickListener(this);
        joinMatchButton.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.reconnect();
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        }
        if (mGoogleApiClient.isConnected()) {
            Games.Invitations.registerInvitationListener(mGoogleApiClient, this);
            Games.TurnBasedMultiplayer.registerMatchUpdateListener(mGoogleApiClient, this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        dailySteps++;
        dailyCounter.setText(Integer.toString(dailySteps));
        counter.setText(Integer.toString(Math.round(event.values[0])));
        counter.setText(String.valueOf(event.values[0]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onConnected(Bundle connectionHint) {
        signInButton.setVisibility(View.GONE);
        signOutButton.setVisibility(View.VISIBLE);
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
        }
    }

    public void findPlayers() {
        if (mGoogleApiClient.isConnected()) {
            final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 2;
            Intent intent = Games.TurnBasedMultiplayer.getSelectOpponentsIntent(mGoogleApiClient, MIN_OPPONENTS, MAX_OPPONENTS, true);
            startActivityForResult(intent, RC_WAITING_ROOM);
        } else {
            Toast.makeText(this, "Sign in to find players", Toast.LENGTH_LONG).show();
        }
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
        if (response == RC_SELECT_PLAYERS) {
//            user canceled
            return;
        }
        if (data != null) {
            final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

            if (invitees != null) {
                TurnBasedMatchConfig turnBasedMatchConfig = TurnBasedMatchConfig.builder()
                        .addInvitedPlayers(invitees)
                        .build();

                Games.TurnBasedMultiplayer
                        .createMatch(mGoogleApiClient, turnBasedMatchConfig)
                        .setResultCallback(new ResultCallbacks<TurnBasedMultiplayer.InitiateMatchResult>() {
                            @Override
                            public void onSuccess(TurnBasedMultiplayer.InitiateMatchResult result) {
                                TurnBasedMatch match = result.getMatch();
                                String matchId = match.getMatchId();
                                ArrayList<String> playerIds = match.getParticipantIds();
                                playerIds.add(match.getCreatorId());

                                Firebase firebaseRef = new Firebase(Constants.FIREBASE_URL_TEAM + "/" + "");
                                firebaseListening();
                                firebaseRef.child(matchId).setValue(playerIds);
                            }

                            @Override
                            public void onFailure(Status status) {
                                Log.d("Something Different", status.toString());
                            }
                        });
            }
        }
    }

    public void firebaseListening() {
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
    public void onTurnBasedMatchReceived(TurnBasedMatch turnBasedMatch) {}

    @Override
    public void onTurnBasedMatchRemoved(String s) {}

    public void joinMatch() {
        Intent invitationIntent = Games.TurnBasedMultiplayer.getInboxIntent(mGoogleApiClient);
        startActivityForResult(invitationIntent, 1);
    }
}
