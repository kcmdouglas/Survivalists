package com.eyecuelab.survivalists.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.eyecuelab.survivalists.Constants;
import com.firebase.client.Firebase;
import com.google.android.gms.games.Games;

/**
 * Created by eyecuelab on 5/6/16.
 */
public class StepResetAlarmReceiver extends BroadcastReceiver {

    int stepsInCounter;
    int flag=0;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();



        int totalSteps = bundle.getInt("endOfDaySteps");
        int dailySteps = bundle.getInt("dailySteps");
        String currentPlayerId = bundle.getString("currentPlayerId");


        Firebase firebaseStepsRef = new Firebase(Constants.FIREBASE_URL_STEPS + "/" + currentPlayerId + "/");




        Intent resetIntent = new Intent ("resetBroadcast");
        resetIntent.putExtra("resetDailySteps", 0);
        resetIntent.putExtra("resetPreviousDaySteps", totalSteps);
        context.sendBroadcast(resetIntent);
    }
}
