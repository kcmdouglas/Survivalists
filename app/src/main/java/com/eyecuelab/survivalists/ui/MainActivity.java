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

import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.eyecuelab.survivalists.Constants;
import com.eyecuelab.survivalists.R;

import com.eyecuelab.survivalists.adapters.InventoryAdapter;
import com.eyecuelab.survivalists.models.Character;
import com.eyecuelab.survivalists.models.Item;
import com.eyecuelab.survivalists.models.SafeHouse;
import com.eyecuelab.survivalists.models.Weapon;
import com.eyecuelab.survivalists.services.BackgroundStepService;
import com.eyecuelab.survivalists.util.CampaignEndAlarmReceiver;
import com.eyecuelab.survivalists.util.InvitationListener;
import com.eyecuelab.survivalists.util.MatchInitiatedListener;
import com.eyecuelab.survivalists.util.MatchUpdateListener;
import com.eyecuelab.survivalists.util.StepResetAlarmReceiver;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;

import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.google.gson.Gson;


import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends FragmentActivity
        implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "MainActivity";

    @Bind(R.id.tabCampaignButton) Button campaignButton;
    @Bind(R.id.mapTabButton) Button mapButton;
    @Bind(R.id.rightInteractionBUtton) Button rightInteractionButton;
    @Bind(R.id.leftInteractionButton) Button leftInteractionButton;

    private int dailySteps;
    private String mCurrentMatchId;
    private int mMatchDuration;
    private int mDefaultGoal;
    private int mReachedSafeHouseId;
    private int mReachedSafeHousePseudoId;
    private ArrayList<String> invitees;
    private byte[] turnData;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private GoogleApiClient mGoogleApiClient;
    private TurnBasedMatch mCurrentMatch;
    private String mCurrentPlayerId;
    private SafeHouse mReachedSafehouse;
    private Character mCurrentCharacter;
    private Firebase mUserFirebaseRef;

    //Flags to indicate return activity
    private static final int RC_SIGN_IN =  100;
    private static final int RC_SELECT_PLAYERS = 200;
    private static final int RC_WAITING_ROOM = 300;

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
    private boolean reachedDailySafehouse;

    private boolean isRecurringAlarmSet;
    private ArrayList<Character> mCharacters;
    ArrayList<String> mPlayerIDs;
    private long matchInitiatedTime;
    int dailyGoal;

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
        setContentView(R.layout.activity_notebook);

        mContext = this;
        ButterKnife.bind(this);

        //Create Shared Preferences
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mSharedPreferences.edit();

        campaignButton.setOnClickListener(this);
        mapButton.setOnClickListener(this);
        leftInteractionButton.setOnClickListener(this);
        rightInteractionButton.setOnClickListener(this);

        mCurrentMatchId = mSharedPreferences.getString("matchId", null);

        //Set counter text based on current shared preferences--these are updated in the shared preferences onChange listener
        dailySteps = mSharedPreferences.getInt(Constants.PREFERENCES_DAILY_STEPS, 0);

        eventOneInitiated = mSharedPreferences.getBoolean(Constants.PREFERENCES_INITIATE_EVENT_1, false);
        eventTwoInitiated = mSharedPreferences.getBoolean(Constants.PREFERENCES_INITIATE_EVENT_2, false);
        eventThreeInitiated = mSharedPreferences.getBoolean(Constants.PREFERENCES_INITIATE_EVENT_3, false);
        eventFourInitiated = mSharedPreferences.getBoolean(Constants.PREFERENCES_INITIATE_EVENT_4, false);
        eventFiveInitiated = mSharedPreferences.getBoolean(Constants.PREFERENCES_INITIATE_EVENT_5, false);
        reachedDailySafehouse = mSharedPreferences.getBoolean(Constants.PREFERENCES_REACHED_SAFEHOUSE, false);

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

        String safehouseJson = mSharedPreferences.getString(Constants.PREFERENCES_REACHED_SAFEHOUSE, null);
        Gson gson = new Gson();
        mReachedSafehouse = gson.fromJson(safehouseJson, SafeHouse.class);

        String playerIDsString = mSharedPreferences.getString(Constants.PREFERENCES_TEAM_IDs, null);
//        String [] playerIDArray = TextUtils.split(",", playerIDsString);

//        for(int i = 0; i < playerIDArray.length; i++ ) {
//            mPlayerIDs.add(playerIDArray[i]);
//        }

        setupBackpackContent();
        loadCharacter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
        if(mPlayerIDs == null) {
           // characterButton.setEnabled(false);
        } else {
            //characterButton.setEnabled(true);
        }
        if(mCurrentMatch != null && mPlayerIDs == null) {
            instantiatePlayerIDs();
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tabCampaignButton:
                Intent intent  = new Intent(mContext, CharacterDetailActivity.class);
                intent.putExtra("position", 0);
                intent.putExtra("playerIDs", Parcels.wrap(mPlayerIDs));
                mContext.startActivity(intent);
                break;
            case R.id.mapTabButton:
                Toast.makeText(this, "Inflate map here", Toast.LENGTH_SHORT).show();
                break;
            case R.id.rightInteractionBUtton:
                Toast.makeText(this, "Are you encouraged?", Toast.LENGTH_SHORT).show();
                break;
            case R.id.leftInteractionButton:
                Toast.makeText(this, "Item given!", Toast.LENGTH_SHORT).show();
                break;
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

            DialogFragment frag = SafehouseDialogFragment.newInstance(mStackLevel, mReachedSafehouse);
            frag.show(ft, "fragment_safehouse_dialog");
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(Constants.PREFERENCES_STEPS_IN_SENSOR_KEY) && (mCurrentMatch != null)) {
            dailySteps = mSharedPreferences.getInt(Constants.PREFERENCES_DAILY_STEPS, 0);
            dailyGoal = mSharedPreferences.getInt(Constants.PREFERENCES_DAILY_GOAL, 5000);
            if (dailyGoal < dailySteps && !reachedDailySafehouse) {
                saveSafehouse();
            }
            initializeEventDialogFragments();

        }
        if(key.equals(Constants.PREFERENCES_REACHED_SAFEHOUSE)) {
            reachedDailySafehouse = mSharedPreferences.getBoolean(Constants.PREFERENCES_REACHED_SAFEHOUSE, true);
        }

        //TODO: Add listener for isCampaignEnded boolean to trigger end of game screen
    }

    public void saveSafehouse() {
        //Sets the user's own atSafehouse node
        Firebase firebaseAtSafeHouseRef = new Firebase(Constants.FIREBASE_URL_USERS + "/" + mCurrentPlayerId +"/atSafeHouse");
        firebaseAtSafeHouseRef.setValue(true);

        //Gets the pseudo ID of the next safehouse
        Firebase nextTeamSafehouse = new Firebase (Constants.FIREBASE_URL_TEAM+"/"+ mCurrentMatchId +"/");
        nextTeamSafehouse.child("nextSafehousePseudoId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long safehousePseudoIdLong = (long) dataSnapshot.getValue();
                mReachedSafeHousePseudoId = (int) safehousePseudoIdLong;
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        //Gets the actual safehouse ID
        nextTeamSafehouse.child("safehouseIdMap/" + mReachedSafeHousePseudoId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long nextSafehouseId = (long) dataSnapshot.getValue();
                mReachedSafeHouseId = (int) nextSafehouseId;
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        //Creates safehouse object from the actual safehouse ID
        Firebase safehouseFirebaseRef = new Firebase(Constants.FIREBASE_URL_SAFEHOUSES + "/" + mReachedSafeHouseId + "/");
        safehouseFirebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String houseName = dataSnapshot.child("houseName").getValue().toString();
                String description = dataSnapshot.child("description").getValue().toString();

                // Build the next safehouse object and save it to shared preferences
                SafeHouse nextSafeHouse = new SafeHouse(mReachedSafeHouseId, houseName, description);
                Gson gson = new Gson();
                String nextSafehouseJson = gson.toJson(nextSafeHouse);
                mEditor.putString(Constants.PREFERENCES_REACHED_SAFEHOUSE, nextSafehouseJson);
                mEditor.commit();
                String safehouseJson = mSharedPreferences.getString(Constants.PREFERENCES_REACHED_SAFEHOUSE, null);
                Gson safehouseGson = new Gson();
                mReachedSafehouse = safehouseGson.fromJson(safehouseJson, SafeHouse.class);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });

        //Sets boolean for reaching the safehouse so the dialog is only triggered once per day
        mEditor.putBoolean(Constants.PREFERENCES_REACHED_SAFEHOUSE, true).apply();


        //Checks if the rest of the team has made it to the safehouse
        final ArrayList<Boolean> teammatesAtSafehouse = new ArrayList<>();

        for(int i = 0; i < mPlayerIDs.size(); i++) {
            Firebase firebaseUserRef = new Firebase(Constants.FIREBASE_URL_USERS + "/" + mPlayerIDs.get(i)+ "/atSafeHouse" );

            firebaseUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    boolean atSafeHouse = (boolean) dataSnapshot.getValue();

                    if(atSafeHouse) {
                        teammatesAtSafehouse.add(atSafeHouse);
                    }

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }

        //If the rest of the team has made it to the safehouse, this resets the last safehouse pseudo ID and the next safehouse pseudo ID
        if(mPlayerIDs.size() == teammatesAtSafehouse.size()) {
            Firebase firebaseTeam = new Firebase (Constants.FIREBASE_URL_TEAM + "/" + mCurrentMatchId);
            firebaseTeam.child("lastSafehousePseudoId").setValue(mReachedSafeHousePseudoId);
            firebaseTeam.child("nextSafehousePseudoId").setValue((mReachedSafeHouseId + 1));
        }


        showEventDialog(2);
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

    public void instantiatePlayerIDs() {
        mPlayerIDs = new ArrayList<>();
        Firebase playerIDRef = new Firebase(Constants.FIREBASE_URL_TEAM + "/" + mCurrentMatchId + "/players/");
        playerIDRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String playerId = child.getValue().toString();
                    if (!(playerId.equals(mCurrentPlayerId))) {
                        mPlayerIDs.add(playerId);
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        mEditor.putString(Constants.PREFERENCES_TEAM_IDs, TextUtils.join(",", mPlayerIDs));
        mEditor.commit();

    }

    public void setupBackpackContent () {
        //TODO: Remove these fake objects for testing:
        ArrayList<Weapon> weapons = new ArrayList<>();
        weapons.add(new Weapon("Axe!", "This is an axe!", 5));
        ArrayList<Item> items = new ArrayList<>();
//        items.add(new Item("Axe!", "This is an axe!", 5, true, R.drawable.axe_inventory));
//        items.add(new Item("Health Pack", "This is a health pack!", 5, true, R.drawable.firstaid_inventory));
//        items.add(new Item("Flare", "This is a flare!", 5, true, R.drawable.flare_inventory));
//        items.add(new Item("Steak", "This is a steak!", 5, true, R.drawable.steak_inventory));

        try {
            GridView inventoryGridView = (GridView) findViewById(R.id.backpackGridView);
            //TODO: Figure out why android studio thinks this catch is required (and isn't happy)
            inventoryGridView.setAdapter(new InventoryAdapter(this, items, weapons, R.layout.inventory_row_grid));
            inventoryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Toast.makeText(MainActivity.this, "" + position, Toast.LENGTH_SHORT).show();
                }
            });
            //This stops the grid from being scrolled.
            inventoryGridView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        return true;
                    }
                    return false;
                }
            });

        } catch (NullPointerException nullPointer) {
            Log.e(TAG, nullPointer.getMessage());
        }

    }

    public void loadCharacter() {
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
            instantiatePlayerIDs();
            mUserFirebaseRef.child("joinedMatch").setValue(true);
        }
    }
}
