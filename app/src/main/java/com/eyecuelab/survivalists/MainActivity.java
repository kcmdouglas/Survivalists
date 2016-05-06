package com.eyecuelab.survivalists;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.example.games.basegameutils.BaseGameUtils;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, SensorEventListener, GoogleApiClient.ConnectionCallbacks,
                   GoogleApiClient.OnConnectionFailedListener, RealTimeMessageReceivedListener,
                   RoomUpdateListener, OnInvitationReceivedListener, RoomStatusUpdateListener {

    @Bind(R.id.stepTextView) TextView counter;
    @Bind(R.id.dailyStepsTextView) TextView dailyCounter;
    @Bind(R.id.sign_in_button) SignInButton signInButton;
    @Bind(R.id.sign_out_button) Button signOutButton;
    @Bind(R.id.findPlayersButton) Button findPlayersButton;
    @Bind(R.id.playersTextView) TextView playersText;
    @Bind(R.id.roomTextView) TextView roomText;

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.reconnect();
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
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
        if (connectionHint != null) {
            Toast.makeText(this, "Connected to google api", Toast.LENGTH_LONG).show();
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
        }
    }

    public void findPlayers() {
        if (mGoogleApiClient.isConnected()) {
            final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 2;
            Bundle automatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS, MAX_OPPONENTS, 0);

            RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
            rtmConfigBuilder.setMessageReceivedListener(this);
            rtmConfigBuilder.setRoomStatusUpdateListener(this);
            rtmConfigBuilder.setAutoMatchCriteria(automatchCriteria);
            Games.RealTimeMultiplayer.create(mGoogleApiClient, rtmConfigBuilder.build());
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            Toast.makeText(this, "Sign in to find players", Toast.LENGTH_LONG).show();
        }
    }

    public void saveTeamToFirebase(List<String> teamMembers) {
        Firebase createdTeam = new Firebase(Constants.FIREBASE_URL_TEAM);
        createdTeam.setValue(teamMembers);
    }

    @Override
    public void onInvitationReceived(Invitation invitation) {}

    @Override
    public void onInvitationRemoved(String s) {}

    @Override
    public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {}

    @Override
    public void onRoomCreated(int statusCode, Room room) {
        Intent intent = Games.RealTimeMultiplayer.getWaitingRoomIntent(mGoogleApiClient, room, Integer.MAX_VALUE);
        startActivityForResult(intent, RC_WAITING_ROOM);
    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Toast.makeText(this, "You broke it!", Toast.LENGTH_LONG).show();
            return;
        }
    }

    @Override
    public void onLeftRoom(int i, String s) {}

    @Override
    public void onRoomConnected(int i, Room room) {
        Toast.makeText(this, "Room connected", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onRoomConnecting(Room room) {
        Toast.makeText(this, "Room connecting", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRoomAutoMatching(Room room) {
        Toast.makeText(this, "Room auto matching", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> list) {
        Toast.makeText(this, "Kassidy invited", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPeerDeclined(Room room, List<String> list) {}

    @Override
    public void onPeerJoined(Room room, List<String> list) {
        Toast.makeText(this, "Other player joined", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPeerLeft(Room room, List<String> list) {}

    @Override
    public void onConnectedToRoom(Room room) {
        Toast.makeText(this, "connected to room", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisconnectedFromRoom(Room room) {}

    @Override
    public void onPeersConnected(Room room, List<String> list) {
        playersText.setText(room.getParticipantIds().toString());
        roomText.setText(room.getRoomId());
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> list) {}

    @Override
    public void onP2PConnected(String s) {}

    @Override
    public void onP2PDisconnected(String s) {}

    @Override
    public void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
    }
}
