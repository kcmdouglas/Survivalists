package com.eyecuelab.survivalists.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;

import com.eyecuelab.survivalists.Constants;
import com.eyecuelab.survivalists.util.StepResetResultReceiver;

import java.util.ArrayList;

/**
 * Created by eyecuelab on 5/12/16.
 */
public class StepResetIntentService extends IntentService {

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mSharedPreferences.edit();

        int totalSteps = bundle.getInt("endOfDaySteps");
        int dailySteps = bundle.getInt("dailySteps");
        String currentPlayerId = bundle.getString("currentPlayerId");

        mEditor.putInt(Constants.PREFERENCES_PREVIOUS_STEPS_KEY, totalSteps).apply();

        int numberOfEvents = (int) (Math.random() * 5 + 1);
        resetEventCounts(numberOfEvents);


    }

    public void resetEventCounts(int events) {

        int eventOneSteps = 0;
        int eventTwoSteps = 0;
        int eventThreeSteps = 0;
        int eventFourSteps = 0;
        int eventFiveSteps = 0;

        switch(events) {
            case 1:
                eventOneSteps = (int) (Math.random() * 500 + 1);
                break;
            case 2:
                eventOneSteps = (int) (Math.random() * 500 + 1);
                eventTwoSteps = (int) (Math.random() * (750 + 350) +350);
                break;
            case 3:
                eventOneSteps = (int) (Math.random() * 500 + 1);
                eventTwoSteps = (int) (Math.random() * (750 + 350) +350);
                eventThreeSteps = (int) (Math.random() * (1000 + 650) + 650);
                break;
            case 4:
                eventOneSteps = (int) (Math.random() * 500 + 1);
                eventTwoSteps = (int) (Math.random() * (750 + 350) +350);
                eventThreeSteps = (int) (Math.random() * (1000 + 650) + 650);
                eventFourSteps = (int) (Math.random() * (1500 + 1000) + 100);
                break;
            case 5:
                eventOneSteps = (int) (Math.random() * 500 + 1);
                eventTwoSteps = (int) (Math.random() * (750 + 350) +350);
                eventThreeSteps = (int) (Math.random() * (1000 + 650) + 650);
                eventFourSteps = (int) (Math.random() * (1500 + 1000) + 100);
                eventFiveSteps = (int) (Math.random() * (2000 + 1500) + 1500);
                break;
        }

        ArrayList<Integer> eventSteps = new ArrayList<>();
        eventSteps.add(eventOneSteps);
        eventSteps.add(eventTwoSteps);
        eventSteps.add(eventThreeSteps);
        eventSteps.add(eventFourSteps);
        eventSteps.add(eventFiveSteps);

    }

    public StepResetIntentService() {
        super("StepResetIntentService");
    }
}
