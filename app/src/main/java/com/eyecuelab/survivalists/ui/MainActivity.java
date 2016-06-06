package com.eyecuelab.survivalists.ui;

import android.app.ActivityManager;
import android.app.AlarmManager;
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
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.eyecuelab.survivalists.Constants;
import com.eyecuelab.survivalists.R;

import com.eyecuelab.survivalists.adapters.InventoryAdapter;
import com.eyecuelab.survivalists.models.Character;
import com.eyecuelab.survivalists.models.Event;
import com.eyecuelab.survivalists.models.Item;
import com.eyecuelab.survivalists.models.SafeHouse;
import com.eyecuelab.survivalists.models.Weapon;
import com.eyecuelab.survivalists.services.BackgroundStepService;
import com.eyecuelab.survivalists.util.StepResetAlarmReceiver;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import com.google.android.gms.games.Games;
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
    @Bind(R.id.stepProgressBar) ProgressBar stepProgressBar;
    @Bind(R.id.healthProgressBar) ProgressBar healthProgressBar;
    @Bind(R.id.energyProgressBar) ProgressBar energyProgressBar;
    @Bind(R.id.dailyGoalTextView) TextView dailyGoalTextView;
    @Bind(R.id.healthTextView) TextView healthTextView;
    @Bind(R.id.energyTextView) TextView energyTextView;

    //TODO: Remove after testing
    @Bind(R.id.stepEditText) EditText stepEditText;

    private int dailySteps;
    private String mCurrentMatchId;
    private int mMatchDuration;
    private int mDefaultGoal;
    private int mReachedSafeHouseNodeId;
    private int mReachedSafeHouseId;
    private ArrayList<String> invitees;
    private byte[] turnData;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private String mCurrentPlayerId;
    private SafeHouse mReachedSafehouse;
    private Character mCurrentCharacter;
    private Firebase mUserFirebaseRef;
    private Weapon eventWeapon;
    private Item eventItem;

    //Flags to indicate navigation
    private final int START_CAMPAIGN_INTENT = 2;
    private final int JOIN_CAMPAIGN_INTENT = 3;

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
    private Event event;
    ArrayList<Weapon> allWeapons;
    ArrayList<Item> allItems;
    ArrayList<Weapon> userWeapons;
    ArrayList<Item> userItems;

    private boolean isRecurringAlarmSet;
    private ArrayList<Character> mCharacters;
    ArrayList<String> mPlayerIDs;
    private long matchInitiatedTime;
    int dailyGoal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);

        setFullScreen();

        //set content view AFTER ABOVE sequence (to avoid crash)
        setContentView(R.layout.activity_notebook);

        mContext = this;
        ButterKnife.bind(this);

        allWeapons = new ArrayList<>();
        allItems = new ArrayList<>();
        //Create Shared Preferences
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mSharedPreferences.edit();

        campaignButton.setOnClickListener(this);
        mapButton.setOnClickListener(this);
        rightInteractionButton.setOnClickListener(this);

        mCurrentMatchId = mSharedPreferences.getString(Constants.PREFERENCES_MATCH_ID, null);
        mCurrentPlayerId = mSharedPreferences.getString(Constants.PREFERENCES_GOOGLE_PLAYER_ID, null);
        mUserFirebaseRef = new Firebase (Constants.FIREBASE_URL_USERS + "/" + mCurrentPlayerId);

        //Set counter text based on current shared preferences--these are updated in the shared preferences onChange listener
        dailySteps = mSharedPreferences.getInt(Constants.PREFERENCES_DAILY_STEPS, 0);
        dailyGoal = mSharedPreferences.getInt(Constants.PREFERENCES_DAILY_GOAL, 5000);

        eventOneInitiated = mSharedPreferences.getBoolean(Constants.PREFERENCES_INITIATE_EVENT_1, false);
        eventTwoInitiated = mSharedPreferences.getBoolean(Constants.PREFERENCES_INITIATE_EVENT_2, false);
        eventThreeInitiated = mSharedPreferences.getBoolean(Constants.PREFERENCES_INITIATE_EVENT_3, false);
        eventFourInitiated = mSharedPreferences.getBoolean(Constants.PREFERENCES_INITIATE_EVENT_4, false);
        eventFiveInitiated = mSharedPreferences.getBoolean(Constants.PREFERENCES_INITIATE_EVENT_5, false);
        reachedDailySafehouse = mSharedPreferences.getBoolean(Constants.PREFERENCES_REACHED_SAFEHOUSE_BOOLEAN, false);

        //Set recurring alarm
        isRecurringAlarmSet = mSharedPreferences.getBoolean("recurringAlarmBoolean", false);
        if(!isRecurringAlarmSet) {
            mEditor.putBoolean("recurringAlarmBoolean", true).commit();
            isRecurringAlarmSet = mSharedPreferences.getBoolean("recurringAlarmBoolean", true);
            initiateDailyCountResetService();
        }

        //Initialize BackgroundStepService to run database injections and constant step updates
        mBackgroundStepService = new BackgroundStepService(mContext);
        mBackgroundStepServiceIntent = new Intent(mContext, mBackgroundStepService.getClass());
        if(!isBackgroundStepServiceRunning(mBackgroundStepService.getClass())) {
            startService(mBackgroundStepServiceIntent);
        }

        String safehouseJson = mSharedPreferences.getString(Constants.PREFERENCES_CURRENT_SAFEHOUSE, null);
        Gson gson = new Gson();
        mReachedSafehouse = gson.fromJson(safehouseJson, SafeHouse.class);

        String playerIDsString = mSharedPreferences.getString(Constants.PREFERENCES_TEAM_IDs, null);
        if (playerIDsString != null) {
            String [] playerIDArray = TextUtils.split(",", playerIDsString);
        }

        if(mCurrentMatchId != null && mPlayerIDs == null) {
            instantiatePlayerIDs();
        }

        setupBackpackContent();
        instantiateAllItems();
        loadCharacter();
        checkDailyGoal();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFullScreen();
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
        if(mPlayerIDs == null) {
           // characterButton.setEnabled(false);
        } else {
            //characterButton.setEnabled(true);
        }
        if(mCurrentMatchId != null && mPlayerIDs == null) {
            instantiatePlayerIDs();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        setFullScreen();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        stopService(mBackgroundStepServiceIntent);
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            setFullScreen();
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
                if (mPlayerIDs != null) {
                    Intent intent = new Intent(mContext, CharacterDetailActivity.class);
                    intent.putExtra("position", 0);
                    intent.putExtra("playerIDs", Parcels.wrap(mPlayerIDs));
                    mContext.startActivity(intent);
                } else {
                    Toast.makeText(this, "Players have not yet joined.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.mapTabButton:
                Toast.makeText(this, "Inflate map here", Toast.LENGTH_SHORT).show();
                break;
            case R.id.rightInteractionBUtton:
                String inputtedSteps = stepEditText.getText().toString();
                int steps = Integer.parseInt(inputtedSteps);
                dailySteps = steps;
                mEditor.putInt(Constants.PREFERENCES_DAILY_STEPS, dailySteps).commit();

                if((mCurrentPlayerId != null) && (steps % 10 < 1)) {
                    Firebase firebaseStepsRef = new Firebase(Constants.FIREBASE_URL_USERS + "/" + mCurrentPlayerId + "/");
                    firebaseStepsRef.child("dailySteps").setValue(steps);
                }
                showEventDialog(1);
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
            pickEventItems();

            int eventNumber = (int) Math.floor(Math.random() * 10 + 1);

            //0 is an attack event, 1 is an inspect event
            final int attackOrInspect = (int) (Math.random() +0.5);
            Firebase mFirebaseEventRef = new Firebase(Constants.FIREBASE_URL_EVENTS);

            if(attackOrInspect == 0) {
                mFirebaseEventRef.child("attack").child(Integer.toString(eventNumber)).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                               event = new Event(dataSnapshot.getValue(Event.class));

                                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                                Fragment prev = getSupportFragmentManager().findFragmentByTag("event");
                                if(prev != null) {
                                    ft.remove(prev);
                                }
                                Bundle bundle = new Bundle();
                                bundle.putParcelable("weapon", eventWeapon);
                                bundle.putParcelable("item", eventItem);
                                bundle.putParcelableArrayList("userWeapons", userWeapons);
                                bundle.putParcelable("event", event);
                                bundle.putInt("attackOrInspect", attackOrInspect);


                                ft.addToBackStack(null);
                                DialogFragment frag = EventDialogFragment.newInstance(mStackLevel, userWeapons);
                                frag.setArguments(bundle);
                                frag.show(ft, "fragment_event_dialog");
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });
            } else {
                mFirebaseEventRef.child("inspect").child(Integer.toString(eventNumber)).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                event = new Event(dataSnapshot.getValue(Event.class));
                                event.setGetItemOnFlee((boolean) dataSnapshot.child("getItemOnFlee").getValue());
                                event.setGetItemOnInspect((boolean) dataSnapshot.child("getItemOnInspect").getValue());

                                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                                Fragment prev = getSupportFragmentManager().findFragmentByTag("event");
                                if(prev != null) {
                                    ft.remove(prev);
                                }
                                Bundle bundle = new Bundle();
                                bundle.putParcelable("weapon", eventWeapon);
                                bundle.putParcelable("item", eventItem);
                                bundle.putParcelableArrayList("userWeapons", userWeapons);
                                bundle.putParcelable("event", event);
                                bundle.putInt("attackOrInspect", attackOrInspect);


                                ft.addToBackStack(null);
                                DialogFragment frag = EventDialogFragment.newInstance(mStackLevel, userWeapons);
                                frag.setArguments(bundle);
                                frag.show(ft, "fragment_event_dialog");
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        }
                );
            }




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
        if(key.equals(Constants.PREFERENCES_DAILY_STEPS)) {
            if (dailyGoal < dailySteps && !reachedDailySafehouse) {
                saveSafehouse();
            }
            initializeEventDialogFragments();
        }
        if(key.equals(Constants.PREFERENCES_STEPS_IN_SENSOR_KEY) && (mCurrentMatchId != null)) {
            dailySteps = mSharedPreferences.getInt(Constants.PREFERENCES_DAILY_STEPS, 0);
            dailyGoal = mSharedPreferences.getInt(Constants.PREFERENCES_DAILY_GOAL, 5000);
            if (dailyGoal < dailySteps && !reachedDailySafehouse) {
                saveSafehouse();
            }
            initializeEventDialogFragments();

        }
        if(key.equals(Constants.PREFERENCES_DAILY_STEPS)) {
            if (dailyGoal < dailySteps && !reachedDailySafehouse) {
                saveSafehouse();
            }
            initializeEventDialogFragments();
            updateStepsUi();
        }

        if(key.equals(Constants.PREFERENCES_REACHED_SAFEHOUSE_BOOLEAN)) {
            reachedDailySafehouse = mSharedPreferences.getBoolean(Constants.PREFERENCES_REACHED_SAFEHOUSE_BOOLEAN, true);
        }

        //TODO: Add listener for isCampaignEnded boolean to trigger end of game screen
    }

    public void saveSafehouse() {
        //Sets the user's own atSafehouse node
        Firebase firebaseAtSafeHouseRef = new Firebase(Constants.FIREBASE_URL_USERS + "/" + mCurrentPlayerId);
        firebaseAtSafeHouseRef.child("atSafeHouse").setValue(true);

        //Gets the pseudo ID of the next safehouse
        final Firebase nextTeamSafehouse = new Firebase (Constants.FIREBASE_URL_TEAM +"/"+ mCurrentMatchId +"/");
        nextTeamSafehouse.child("nextSafehouseId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    long safehousePseudoIdLong = (long) dataSnapshot.getValue();
                    mReachedSafeHouseId = (int) safehousePseudoIdLong;
                } else {
                    mReachedSafeHouseId = (int) dataSnapshot.getValue();
                }

                //Gets the SafeHouse Node ID from the Safehouse ID Map
                nextTeamSafehouse.child("safehouseIdMap/" + mReachedSafeHouseId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long nextSafehouseId = (long) dataSnapshot.getValue();
                        mReachedSafeHouseNodeId = (int) nextSafehouseId;

                        //Creates safehouse object from the Safehouse Node ID
                        Firebase safehouseFirebaseRef = new Firebase(Constants.FIREBASE_URL_SAFEHOUSES + "/" + mReachedSafeHouseNodeId + "/");
                        safehouseFirebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String houseName = dataSnapshot.child("houseName").getValue().toString();
                                String description = dataSnapshot.child("description").getValue().toString();

                                // Build the current safehouse object and save it to shared preferences
                                SafeHouse currentSafeHouse = new SafeHouse(mReachedSafeHouseNodeId, houseName, description);
                                Gson gson = new Gson();
                                String currentSafehouseJson = gson.toJson(currentSafeHouse);
                                mEditor.putString(Constants.PREFERENCES_CURRENT_SAFEHOUSE, currentSafehouseJson);
                                mEditor.commit();
                                String safehouseJson = mSharedPreferences.getString(Constants.PREFERENCES_CURRENT_SAFEHOUSE, null);
                                Gson safehouseGson = new Gson();
                                mReachedSafehouse = safehouseGson.fromJson(safehouseJson, SafeHouse.class);
                                showEventDialog(2);
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {}
                        });

                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        //Sets boolean for reaching the safehouse so the dialog is only triggered once per day
        mEditor.putBoolean(Constants.PREFERENCES_REACHED_SAFEHOUSE_BOOLEAN, true).apply();


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

                    //If the rest of the team has made it to the safehouse, this resets the last safehouse pseudo ID and the next safehouse pseudo ID
                    if(mPlayerIDs.size() == teammatesAtSafehouse.size()) {
                        Firebase firebaseTeam = new Firebase (Constants.FIREBASE_URL_TEAM + "/" + mCurrentMatchId);
                        firebaseTeam.child("lastSafehouseId").setValue(mReachedSafeHouseId);
                        firebaseTeam.child("nextSafehouseId").setValue((mReachedSafeHouseId + 1));
                    }

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }

    }

    public void initiateDailyCountResetService() {
        Intent intent = new Intent(this, StepResetAlarmReceiver.class);
        //Sets a recurring alarm just before midnight daily to trigger BroadcastReceiver
        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.HOUR_OF_DAY, 23);
//        calendar.set(Calendar.MINUTE, 59);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//        calendar.add(Calendar.MINUTE, 30);
        PendingIntent pi = PendingIntent.getBroadcast(this, StepResetAlarmReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_HALF_HOUR, pi);
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
        userWeapons = new ArrayList<>();
        userItems = new ArrayList<>();

        mCurrentMatchId = mSharedPreferences.getString(Constants.PREFERENCES_MATCH_ID, null);
        mUserFirebaseRef = new Firebase(Constants.FIREBASE_URL_USERS + "/" + "").child(mCurrentPlayerId);
        Log.v(TAG, "user " + mCurrentMatchId + "");
        Log.v(TAG, "firebase " + mUserFirebaseRef + "");

        if (mCurrentMatchId != null && mUserFirebaseRef != null) {
            mUserFirebaseRef.child("items").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Item currentItem = new Item(child.getValue(Item.class));
                        currentItem.setPushId(child.child("pushId").getValue().toString());
                        long imageId = (long) child.child("imageId").getValue();
                        currentItem.setImageId((int) imageId);
                        userItems.add(currentItem);
                        Log.v(TAG, userItems.size() + "");
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });

            mUserFirebaseRef.child("weapons").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Weapon currentWeapon = new Weapon(child.getValue(Weapon.class));
                        currentWeapon.setPushId(child.child("pushId").getValue().toString());
                        userWeapons.add(currentWeapon);
                        Log.v(TAG, userWeapons.size() + "");
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });

        }
        GridView inventoryGridView = (GridView) findViewById(R.id.backpackGridView);
        inventoryGridView.setAdapter(new InventoryAdapter(this, userItems, userWeapons, R.layout.inventory_row_grid));
        inventoryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                DialogFragment frag = InventoryDetailFragment.newInstance(userItems.get(position), mCurrentCharacter, mCurrentPlayerId);
                frag.show(ft, "fragment_safehouse_dialog");
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
    }

    public void loadCharacter() {
        if(mCurrentMatchId != null && mUserFirebaseRef != null) {
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

                    healthProgressBar.setProgress(health);
                    healthTextView.setText(health + "HP");
                    energyProgressBar.setProgress(fullnessLevel);
                    energyTextView.setText(fullnessLevel + "%");

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

    public void instantiateAllItems() {

        Firebase itemRef = new Firebase(Constants.FIREBASE_URL_ITEMS +"/");

        itemRef.child("weapons").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child: dataSnapshot.getChildren()) {
                    Weapon weapon = child.getValue(Weapon.class);
                    allWeapons.add(weapon);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        itemRef.child("food").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child: dataSnapshot.getChildren()) {
                    Item item = child.getValue(Item.class);
                    allItems.add(item);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        itemRef.child("medicine").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child: dataSnapshot.getChildren()) {
                    Item item = child.getValue(Item.class);
                    allItems.add(item);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    public void pickEventItems() {
        Collections.shuffle(allWeapons);
        Collections.shuffle(allItems);

        eventWeapon = allWeapons.get(0);
        eventItem = allItems.get(0);

    }

    public void updateStepsUi() {
        stepProgressBar.setProgress(dailySteps);
        stepProgressBar.setMax(dailyGoal);
        dailyGoalTextView.setText(dailySteps + "/" + dailyGoal);
    }

    public void checkDailyGoal() {
        mUserFirebaseRef.child("dailyGoal").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dailyGoal = Integer.parseInt(dataSnapshot.getValue().toString());
                mEditor.putInt(Constants.PREFERENCES_DAILY_GOAL, dailyGoal);
                mEditor.apply();
                updateStepsUi();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });
    }
}
