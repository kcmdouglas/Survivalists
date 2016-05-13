package com.eyecuelab.survivalists.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;

import com.eyecuelab.survivalists.Constants;
import com.eyecuelab.survivalists.util.StepResetResultReceiver;

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
        //ResultReceiver receiver = intent.getParcelableExtra("receiver");

        mEditor.putInt(Constants.PREFERENCES_PREVIOUS_STEPS_KEY, totalSteps).apply();

//        Intent resetIntent = new Intent ();
//        resetIntent.putExtra("resetDailySteps", 0);
//        resetIntent.putExtra("resetPreviousDaySteps", totalSteps);
//        Bundle sendBundle = resetIntent.getExtras();





    }

    public StepResetIntentService() {
        super("StepResetIntentService");
    }
}
