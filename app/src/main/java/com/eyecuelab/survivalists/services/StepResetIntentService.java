package com.eyecuelab.survivalists.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.eyecuelab.survivalists.Constants;
import com.firebase.client.Firebase;

/**
 * Created by eyecuelab on 5/12/16.
 */
public class StepResetIntentService extends IntentService implements SharedPreferences.OnSharedPreferenceChangeListener{

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private int defaultDailyGoal;

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mSharedPreferences.edit();

        int totalSteps = mSharedPreferences.getInt(Constants.PREFERENCES_STEPS_IN_SENSOR_KEY, 0);

        mEditor.putInt(Constants.PREFERENCES_PREVIOUS_STEPS_KEY, totalSteps).apply();
        mEditor.putInt(Constants.PREFERENCES_DAILY_STEPS, 0).apply();

        int numberOfEvents = (int) (Math.random() * 5 + 1);
        resetEventCounts(numberOfEvents);

        String playerId = mSharedPreferences.getString(Constants.PREFERENCES_GOOGLE_PLAYER_ID, null);

        mEditor.putBoolean(Constants.PREFERENCES_REACHED_SAFEHOUSE_BOOLEAN, false).apply();

        defaultDailyGoal = mSharedPreferences.getInt(Constants.PREFERENCES_DEFAULT_DAILY_GOAL_SETTING, 5000);

        int randomizedDailyGoal = (int) (Math.random() * ((defaultDailyGoal + 500) + defaultDailyGoal) + defaultDailyGoal);

        Firebase playerRef = new Firebase(Constants.FIREBASE_URL_USERS + "/" + playerId + "/");
        playerRef.child("atSafeHouse").setValue(false);

        playerRef.child("dailyGoal").setValue(randomizedDailyGoal);



    }

    public void resetEventCounts(int events) {

        int eventOneSteps = (int) (Math.random() * 500 + 1);
        int eventTwoSteps = (int) (Math.random() * (1000 + 500) +500);
        int eventThreeSteps = (int) (Math.random() * (1500 + 1000) + 1000);
        int eventFourSteps = (int) (Math.random() * (2000 + 1500) + 1500);
        int eventFiveSteps = (int) (Math.random() * (3000 + 2000) + 2000);

        switch(events) {
            case 1:
                mEditor.putInt(Constants.PREFERENCES_EVENT_1_STEPS, eventOneSteps);
                mEditor.putInt(Constants.PREFERENCES_EVENT_2_STEPS, -1);
                mEditor.putInt(Constants.PREFERENCES_EVENT_3_STEPS, -1);
                mEditor.putInt(Constants.PREFERENCES_EVENT_4_STEPS, -1);
                mEditor.putInt(Constants.PREFERENCES_EVENT_5_STEPS, -1);
                mEditor.commit();
                break;
            case 2:
                mEditor.putInt(Constants.PREFERENCES_EVENT_1_STEPS, eventOneSteps);
                mEditor.putInt(Constants.PREFERENCES_EVENT_2_STEPS, eventTwoSteps);
                mEditor.putInt(Constants.PREFERENCES_EVENT_3_STEPS, -1);
                mEditor.putInt(Constants.PREFERENCES_EVENT_4_STEPS, -1);
                mEditor.putInt(Constants.PREFERENCES_EVENT_5_STEPS, -1);
                mEditor.commit();
                break;
            case 3:
                mEditor.putInt(Constants.PREFERENCES_EVENT_1_STEPS, eventOneSteps);
                mEditor.putInt(Constants.PREFERENCES_EVENT_2_STEPS, eventTwoSteps);
                mEditor.putInt(Constants.PREFERENCES_EVENT_3_STEPS, eventThreeSteps);
                mEditor.putInt(Constants.PREFERENCES_EVENT_4_STEPS, -1);
                mEditor.putInt(Constants.PREFERENCES_EVENT_5_STEPS, -1);
                mEditor.commit();
                break;
            case 4:
                mEditor.putInt(Constants.PREFERENCES_EVENT_1_STEPS, eventOneSteps);
                mEditor.putInt(Constants.PREFERENCES_EVENT_2_STEPS, eventTwoSteps);
                mEditor.putInt(Constants.PREFERENCES_EVENT_3_STEPS, eventThreeSteps);
                mEditor.putInt(Constants.PREFERENCES_EVENT_4_STEPS, eventFourSteps);
                mEditor.putInt(Constants.PREFERENCES_EVENT_5_STEPS, -1);
                mEditor.commit();
                break;
            case 5:
                mEditor.putInt(Constants.PREFERENCES_EVENT_1_STEPS, eventOneSteps);
                mEditor.putInt(Constants.PREFERENCES_EVENT_2_STEPS, eventTwoSteps);
                mEditor.putInt(Constants.PREFERENCES_EVENT_3_STEPS, eventThreeSteps);
                mEditor.putInt(Constants.PREFERENCES_EVENT_4_STEPS, eventFourSteps);
                mEditor.putInt(Constants.PREFERENCES_EVENT_5_STEPS, eventFiveSteps);
                mEditor.commit();
                break;
        }

        mEditor.putBoolean(Constants.PREFERENCES_INITIATE_EVENT_1, false);
        mEditor.putBoolean(Constants.PREFERENCES_INITIATE_EVENT_2, false);
        mEditor.putBoolean(Constants.PREFERENCES_INITIATE_EVENT_3, false);
        mEditor.putBoolean(Constants.PREFERENCES_INITIATE_EVENT_4, false);
        mEditor.putBoolean(Constants.PREFERENCES_INITIATE_EVENT_5, false);
        mEditor.commit();

    }

    public StepResetIntentService() {
        super("StepResetIntentService");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}
